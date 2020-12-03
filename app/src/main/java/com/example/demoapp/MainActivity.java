package com.example.demoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcContentSignerBuilder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.bc.BcPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import java.security.cert.X509Certificate;

public class MainActivity extends AppCompatActivity  {

    private SSLSocket sock;
    BufferedReader inStream;
    PrintWriter outStream;
    private String csrStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void GenCSR(){
        X500Name p = new X500Name("CN=User, O=Organization, C=ES");
        try {
            AsymmetricCipherKeyPairGenerator kpg = new RSAKeyPairGenerator();
            RSAKeyGenerationParameters genParam = new RSAKeyGenerationParameters(BigInteger.valueOf(0x1001), new SecureRandom(), 2048, 25);
            kpg.init(genParam);
            AsymmetricCipherKeyPair keyPair = kpg.generateKeyPair();
            PKCS10CertificationRequestBuilder reqBuild = new BcPKCS10CertificationRequestBuilder(p, (AsymmetricKeyParameter) keyPair.getPublic());
            DefaultSignatureAlgorithmIdentifierFinder sigAlgFinder = new DefaultSignatureAlgorithmIdentifierFinder();
            DefaultDigestAlgorithmIdentifierFinder digAlgFinder = new DefaultDigestAlgorithmIdentifierFinder();
            AlgorithmIdentifier sigAlgId = sigAlgFinder.find("SHA256withRSA");
            AlgorithmIdentifier digAlgId = digAlgFinder.find(sigAlgId);
            BcContentSignerBuilder signBuild =  new BcRSAContentSignerBuilder(sigAlgId,digAlgId);
            PKCS10CertificationRequest req = reqBuild.build(signBuild.build((AsymmetricKeyParameter) keyPair.getPrivate()));
            PemObject pObj = new PemObject("CERTIFICATE REQUEST", req.getEncoded());
            StringWriter sw = new StringWriter();
            //FileWriter fw = new FileWriter(new File(MainActivity.this.getFilesDir(), "user.csr"));
            PemWriter pemWriter = new PemWriter(sw);
            pemWriter.writeObject(pObj);
            pemWriter.close();
            //fw.close();
            csrStr = sw.toString();
            sw.close();
        } catch (OperatorCreationException | IOException e) {
            e.printStackTrace();
        }
    }

    private SSLContext GenContext() throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
// From https://www.washington.edu/itconnect/security/ca/load-der.crt
        InputStream caInput = new BufferedInputStream(getResources().openRawResource(R.raw.ca));
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }

// Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

// Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManager[] tmf = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };
        //TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        //tmf.init(keyStore);
// Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf, new SecureRandom());
        return context;
    }
    private void Connect() throws IOException, InterruptedException {
        Log.d("app", "YOOOOOOOOOOO");
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    SSLContext cxt = GenContext();
                    SSLSocketFactory factory= cxt.getSocketFactory();
                    sock = (SSLSocket) factory.createSocket("192.168.1.39", 44444);
                    sock.addHandshakeCompletedListener(new HandshakeCompletedListener() {

                        public void handshakeCompleted(HandshakeCompletedEvent handshakeCompletedEvent) {
                            Certificate[] certs = new Certificate[0];
                            try {
                                certs = handshakeCompletedEvent.getPeerCertificates();
                            } catch (SSLPeerUnverifiedException e) {
                                e.printStackTrace();
                            }
                            if (certs != null) {
                                Log.d("app","handshake returned local certs count: " + certs.length);
                                for (int i = 0; i < certs.length; i++) {
                                    Certificate cert = certs[i];
                                    Log.d("app","cert: " + cert.toString());
                                }
                            } else {
                                Log.d("app","handshake returned no local certs");
                            }
                        }
                    });
                try {
                    sock.getSession();
                    sock.startHandshake();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                //sock = new Socket("192.168.1.39",44444);
                   inStream = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                   outStream = new PrintWriter(sock.getOutputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join();
        Log.d("app", "aaaaaaOOOOOOOOOOO");
        GenCSR();
        Log.d("app", csrStr);
        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    Send(csrStr);
                    sock.shutdownInput();
                    sock.shutdownOutput();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void Send(String buf){
        outStream.println(buf);
        outStream.flush();
    }

    private String Receive() throws IOException {
        return inStream.readLine();
    }

    public void OnClick(View v){
        try {
            Connect();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
       /* Intent intent = new Intent(this, MainActivity2.class);
        intent.putExtra("EXTRA", "hello");
        startActivity(intent);*/
    }
}