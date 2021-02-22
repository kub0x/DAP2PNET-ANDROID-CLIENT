package com.example.demoapp;

import android.content.Context;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.signers.RSADigestSigner;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

public class PKI {

    private static PKI myInstance = new PKI();

    private PKI(){}

    public static PKI GetInstance() { return myInstance; }

    public String GenCSR(AsymmetricCipherKeyPair keyPair, String organization, String country, String identity){
        String csrStr="";
        String name = "CN=" + identity +  "," + "O=" + organization + ",C=" + country;
        X500Name csr = new X500Name(name);
        try {
            PKCS10CertificationRequestBuilder reqBuild = new BcPKCS10CertificationRequestBuilder(csr, keyPair.getPublic());
            DefaultSignatureAlgorithmIdentifierFinder sigAlgFinder = new DefaultSignatureAlgorithmIdentifierFinder();
            DefaultDigestAlgorithmIdentifierFinder digAlgFinder = new DefaultDigestAlgorithmIdentifierFinder();
            AlgorithmIdentifier sigAlgId = sigAlgFinder.find("SHA256withRSA");
            AlgorithmIdentifier digAlgId = digAlgFinder.find(sigAlgId);
            BcContentSignerBuilder signBuild =  new BcRSAContentSignerBuilder(sigAlgId,digAlgId);
            PKCS10CertificationRequest req = reqBuild.build(signBuild.build(keyPair.getPrivate()));
            csrStr=WritePEMToStr("CERTIFICATE REQUEST", req.getEncoded());
        } catch (OperatorCreationException | IOException e) {
            e.printStackTrace();
        }
        return csrStr;
    }

    public String GetCertificateSubjectOrganization(InputStream in) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) factory.generateCertificate(in);

        X500Principal subjectX500Principal = cert.getSubjectX500Principal();
        System.out.println(subjectX500Principal.getName());
        System.out.println(subjectX500Principal.getName(X500Principal.RFC1779));
        System.out.println(subjectX500Principal.getName(X500Principal.CANONICAL));

        X500Name x500name = new X500Name( subjectX500Principal.getName(X500Principal.RFC1779) );
        RDN cn = x500name.getRDNs(BCStyle.O)[0];
        return  IETFUtils.valueToString(cn.getFirst().getValue());
    }

    public String GetCertificateSubjectCountry(InputStream in) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) factory.generateCertificate(in);

        X500Principal subjectX500Principal = cert.getSubjectX500Principal();
        System.out.println(subjectX500Principal.getName());
        System.out.println(subjectX500Principal.getName(X500Principal.RFC1779));
        System.out.println(subjectX500Principal.getName(X500Principal.CANONICAL));

        X500Name x500name = new X500Name( subjectX500Principal.getName(X500Principal.RFC1779) );
        RDN cn = x500name.getRDNs(BCStyle.C)[0];
        return  IETFUtils.valueToString(cn.getFirst().getValue());
    }

    public String GetCertificateIssuerName(InputStream in)throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) factory.generateCertificate(in);

        X500Principal subjectX500Principal = cert.getIssuerX500Principal();
        System.out.println(subjectX500Principal.getName());
        System.out.println(subjectX500Principal.getName(X500Principal.RFC1779));
        System.out.println(subjectX500Principal.getName(X500Principal.CANONICAL));

        X500Name x500name = new X500Name( subjectX500Principal.getName(X500Principal.RFC1779) );
        RDN cn = x500name.getRDNs(BCStyle.CN)[0];
        return  IETFUtils.valueToString(cn.getFirst().getValue());
    }

    public String GetCertificateSubjectCN(InputStream in) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) factory.generateCertificate(in);

        X500Principal subjectX500Principal = cert.getSubjectX500Principal();
        System.out.println(subjectX500Principal.getName());
        System.out.println(subjectX500Principal.getName(X500Principal.RFC1779));
        System.out.println(subjectX500Principal.getName(X500Principal.CANONICAL));

        X500Name x500name = new X500Name( subjectX500Principal.getName(X500Principal.RFC1779) );
        RDN cn = x500name.getRDNs(BCStyle.CN)[0];
        return  IETFUtils.valueToString(cn.getFirst().getValue());
    }

    public String WritePEMToStr(String type, byte[] material) throws IOException {
        String strPEM="";
        PemObject pObj = new PemObject(type, material);
        StringWriter sw = new StringWriter();
        PemWriter pemWriter = new PemWriter(sw);
        pemWriter.writeObject(pObj);
        pemWriter.close();
        strPEM = sw.toString();
        sw.close();
        return strPEM;
    }

    public AsymmetricCipherKeyPair GenRSAKey(){
        AsymmetricCipherKeyPairGenerator kpg = new RSAKeyPairGenerator();
        RSAKeyGenerationParameters genParam = new RSAKeyGenerationParameters(BigInteger.valueOf(0x1001), new SecureRandom(), 2048, 25);
        kpg.init(genParam);
        AsymmetricCipherKeyPair keyPair = kpg.generateKeyPair();
        return keyPair;
    }

    public byte[] SignMessage(AsymmetricCipherKeyPair keyPair, byte[] message) throws OperatorCreationException, CryptoException {
        RSADigestSigner dsign = new RSADigestSigner(new SHA512Digest());
        dsign.init(true, keyPair.getPrivate());
        dsign.update(message,0,message.length);
        byte[] signature = dsign.generateSignature();
        return signature;
    }

}
