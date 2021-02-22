package com.example.demoapp;

import android.content.res.Resources;
import android.util.Log;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SecureSocket {

    private AsymmetricCipherKeyPair keyPair;
    private Resources res; //resources context of the application
    private BufferedReader inStream;
    private PrintWriter outStream;
    private Socket sock;
    private ServerSocket serverSocket;
    private boolean disconnected = false;
    private boolean callbacks = false;

    public SecureSocket(String IP, int port, AsymmetricCipherKeyPair keyPair, Resources res){ //One-way
        this.keyPair = keyPair;
        this.res = res;
        try{
            SSLContext ctx = GenOneWayContext();
            SSLSocketFactory factory = ctx.getSocketFactory();
            this.sock = factory.createSocket(IP, port);
            inStream = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            outStream = new PrintWriter(sock.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SecureSocket(String IP, int port, AsymmetricCipherKeyPair keyPair, String clientCertificatePath, Resources res){ //Two-way
        this.keyPair = keyPair;
        this.res = res;
        try {
            SSLContext ctx = GenTwoWayContext(clientCertificatePath);
            SSLSocketFactory factory = ctx.getSocketFactory();
            this.sock = factory.createSocket(IP, port);
            inStream = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            outStream = new PrintWriter(sock.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SendToPeer(String IP, int port, String data) throws IOException {
        //just sends data
        Socket sock = new Socket(IP, port);
        PrintWriter out = new PrintWriter(sock.getOutputStream());
        out.write(data);
        sock.close();
    }

    public String ReadMessage() throws IOException {
        //reads and stop if remote shutdown (EOF) or hits a null-terminated string ending in '\0'
        Log.d("app", "attemtping to read!");
        StringBuilder sb = new StringBuilder();
        int  c=0;
        while ((c = inStream.read()) > 0){
            char ch = (char) c;
            //Log.d("app", "got new char: " + ch);
            sb.append(ch);
        }
        Log.d("app", "finishing read!");
        if (c == -1) disconnected = true; //hits EOF
        return sb.toString();
    }

    public boolean IsDisconnected() { return disconnected; }

    public void WriteMessage(String message) {
        outStream.println(message);
        outStream.flush();
    }

    public void Close() throws IOException {
        sock.shutdownInput();
        sock.shutdownOutput();
    }

    private SSLContext GenOneWayContext() throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = new BufferedInputStream(res.openRawResource(R.raw.ca));
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }

        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), new SecureRandom());
        return context;
    }

    private SSLContext GenTwoWayContext(String clientCertificatePath) throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, InvalidKeySpecException, UnrecoverableKeyException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = new BufferedInputStream(res.openRawResource(R.raw.ca));
        InputStream clientInput = new BufferedInputStream(new FileInputStream(new File(clientCertificatePath + "_cert.pem")));
        InputStream keyInput = new BufferedInputStream(new FileInputStream(new File(clientCertificatePath + "_priv.pem")));
        Certificate ca, clientCert;
        try {
            ca = cf.generateCertificate(caInput);
            clientCert = cf.generateCertificate(clientInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            System.out.println("client=" + ((X509Certificate) clientCert).getSubjectDN());
        } finally {
            caInput.close();
            clientInput.close();
        }

        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        PrivateKeyInfo privInfo = PrivateKeyInfoFactory.createPrivateKeyInfo(keyPair.getPrivate());
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privInfo.getEncoded()));
        Log.d("app", privateKey.getAlgorithm());
        Certificate[] certChain = new Certificate[1];
        certChain[0] = clientCert;
        //certChain[1] = ca;
        keyStore.setCertificateEntry("ca", ca);
        keyStore.setKeyEntry("clientKey", privateKey, null, certChain);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        KeyManagerFactory kmf = KeyManagerFactory
                .getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, null);
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        return context;
    }
}