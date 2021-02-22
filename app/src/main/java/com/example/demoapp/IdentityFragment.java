package com.example.demoapp;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.demoapp.ui.IdentityData;
import com.example.demoapp.ui.IdentityDataAdapter;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;

public class IdentityFragment extends Fragment {

    IdentityDataAdapter adapter = null;
    P2PNode node = null;
    String identitiesPath;

    private void ParseIdentities(View v){
        try {
            ArrayList<IdentityData> data = new ArrayList<IdentityData>();
            File dir = new File(identitiesPath);
            for (String fileName : dir.list()) {
                if (fileName.contains("_cert.pem")) {
                    InputStream in = new FileInputStream(identitiesPath + File.separator +  fileName);
                    String id = PKI.GetInstance().GetCertificateSubjectCN(in);
                    in.close();
                    data.add(new IdentityData(fileName.replace("_cert.pem", ""), id)); //save only identity name .pem
                }
            }
            RecyclerView list = (RecyclerView) v.findViewById(R.id.list_identities);
            adapter = new IdentityDataAdapter(data);
            list.setHasFixedSize(true);
            list.setLayoutManager(new LinearLayoutManager(this.getContext()));
            list.setAdapter(adapter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.activity_identity, container, false);
        final Button btn_genId = v.findViewById(R.id.btn_genId);
        btn_genId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                OnClick_genId(v);
            }
        });
        final Button btn_login = v.findViewById(R.id.btn_register);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                OnClick_login(v);
            }
        });

        final Button btn_remove = v.findViewById(R.id.btn_removeId);
        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                OnClick_removeId(v);
            }
        });

        final Button btn_test = v.findViewById(R.id.btn_test);
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                try {
                    OnClick_test(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        final Button btn_test2 = v.findViewById(R.id.btn_test2);
        btn_test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                try {
                    OnClick_test2(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        String idPath = v.getContext().getFilesDir().getAbsolutePath() + File.separator + "identities";
        File idDir = new File(idPath);
        if (!idDir.exists())
            idDir.mkdirs();
        identitiesPath = idDir.getAbsolutePath();

        ParseIdentities(v);

        return v;
    }

    public void OnClick_test(View v) throws IOException {
        String binaryKey = new BigInteger("85a48f75a48ea92fa8507ca423f379", 16).toString(2);
        KeyValue kv = new KeyValue(binaryKey, "HOLA".getBytes(), node.bucketManager.GetParentID());
        node.bucketManager.AddKey(kv);
    }

    public void OnClick_test2(View v) throws IOException {
        String binaryKey = new BigInteger("85a48f75a48ea92fa8507ca423f379", 16).toString(2);
        KeyValue kv = node.bucketManager.QueryKey(binaryKey);
        Log.d("app", "Querying for 85a48f75a48ea92fa8507ca423f379: ");
    }

    public void OnRegistrationResponse(String buffer){
        IdentityFragment th = this;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (!buffer.contains("Error:")){ //we got a valid PEM certificate store it!
                    TextView txt = th.getView().findViewById(R.id.txt_idName);
                    try {
                        //stores .pem file in /data/data/com.example.demoapp/files/
                        Utils.GetInstance().WriteToFile(identitiesPath, txt.getText().toString()  + "_cert.pem", buffer);
                        ParseIdentities(th.getView());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                LayoutInflater inflater= LayoutInflater.from(th.getContext());
                View view=inflater.inflate(R.layout.alert_dialog_layout, null);
                TextView textView  = (TextView)view.findViewById(R.id.txt_dialog);
                textView.setText(buffer);
                AlertDialog.Builder builder1 = new AlertDialog.Builder(th.getContext());
                //builder1.setMessage(buffer);
                if(textView.getParent()!=null)
                    ((ViewGroup)textView.getParent()).removeView(textView);
                builder1.setView(textView);
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Gotcha!",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });
    }

    public void OnClick_genId(View v){
        TextView textView  = (TextView)getView().findViewById(R.id.txt_idName);
        try {
            //store pubkey and privkey in PEM format. This is, bytes-to-pem
            AsymmetricCipherKeyPair keyPair = PKI.GetInstance().GenRSAKey();
            node = new P2PNode(keyPair);
            PrivateKeyInfo privInfo = PrivateKeyInfoFactory.createPrivateKeyInfo(keyPair.getPrivate());
            SubjectPublicKeyInfo pubInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(keyPair.getPublic());
            String strPub = PKI.GetInstance().WritePEMToStr("PUBLIC KEY", pubInfo.getEncoded());
            String strPriv = PKI.GetInstance().WritePEMToStr("PRIVATE KEY", privInfo.getEncoded());
            String idName = textView.getText().toString();
            Utils.GetInstance().WriteToFile(identitiesPath,  idName + "_pub.pem", strPub);
            Utils.GetInstance().WriteToFile(identitiesPath, idName  + "_priv.pem", strPriv);

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            IdentityFragment th = this;
            TextView txt1  = (TextView)getView().findViewById(R.id.txt_setCountry);
            TextView txt2  = (TextView)getView().findViewById(R.id.txt_setOrg);
            TextView txt3  = (TextView)getView().findViewById(R.id.txt_idName);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        node.RequestCertificateFromCSR("192.168.1.39", th, v.getResources(),txt2.getText().toString(), txt1.getText().toString(), txt3.getText().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void OnClick_login(View v){
        try {
            IdentityFragment th = this;
            TextView textView  = (TextView)getView().findViewById(R.id.txt_idName);
            String idName = textView.getText().toString();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        node.GetPeers("192.168.1.39", identitiesPath + "/" + idName , th, v.getResources());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void OnClick_removeId(View v){
        adapter.RemoveSelectedIdentity(identitiesPath);
    }

}