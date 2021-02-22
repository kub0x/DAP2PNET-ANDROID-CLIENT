package com.example.demoapp;

import android.content.res.Resources;
import android.util.Log;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import java.math.BigInteger;

public class P2PNode {

    AsymmetricCipherKeyPair keyPair;
    private String nodeID;
    public BucketManager bucketManager;
    
    public P2PNode(AsymmetricCipherKeyPair keyPair){
        this.keyPair = keyPair;
    }

    void ParsePeerList(String peers){
        String[] sepComma = peers.split(",");
        for (String strComma : sepComma){
            String[] sep = strComma.split(":");
            String peerID = sep[0];
            String peerIP = sep[1];
            //setup binary IDs as Buckets only understand binary IDs
            String binaryParentID = new BigInteger(nodeID, 16).toString(2);
            String binaryPeerID = new BigInteger(peerID, 16).toString(2);
            Log.d("app", "Added new Triplet. ID: " + peerID + " IP: " + peerIP + " parentID: " + nodeID );
            bucketManager.AddTriplet(new Triplet(binaryPeerID, peerIP, 44441, binaryParentID));

        }
    }

    public void GetPeers(String IP, String clientCertificatePath, IdentityFragment identityFragment, Resources res) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, InvalidKeySpecException, UnrecoverableKeyException {
        SecureSocket ssock  = new SecureSocket(IP, 44445, keyPair, clientCertificatePath, res );
        Thread thread_read = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    String peerList = ssock.ReadMessage();
                    if (ssock.IsDisconnected()) return;
                    Log.d("app", peerList);
                    ssock.Close();
                    ParsePeerList(peerList);
                    //identityFragment.OnRegistrationResponse(buffer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Log.d("app", "spawning new thread!");
        thread_read.start();
    }

    public void RequestCertificateFromCSR(String IP, IdentityFragment identityFragment, Resources res, String organization, String country, String identity) throws IOException, InterruptedException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        SecureSocket ssock  = new SecureSocket(IP, 44444, keyPair, res );

        Thread thread_read = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    String signedCert = ssock.ReadMessage();
                    if (ssock.IsDisconnected()) return;
                    InputStream in = new ByteArrayInputStream(signedCert.getBytes(StandardCharsets.UTF_8));
                    nodeID = PKI.GetInstance().GetCertificateSubjectCN(in);
                    in.close();
                    String binaryParentID = new BigInteger(nodeID, 16).toString(2);
                    bucketManager = new BucketManager(binaryParentID);
                    ssock.Close();
                    identityFragment.OnRegistrationResponse(signedCert);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Log.d("app", "spawning new thread!");
        thread_read.start();

        String csrStr = PKI.GetInstance().GenCSR(keyPair, organization, country, identity);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    ssock.WriteMessage(csrStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join();
    }

}

/*sock.addHandshakeCompletedListener(new HandshakeCompletedListener() {

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
        });*/
