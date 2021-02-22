package com.example.demoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.demoapp.ui.IdentityData;
import com.example.demoapp.ui.IdentityDataAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.util.ArrayList;

public class CertificatesFragment extends Fragment {

    IdentityDataAdapter adapter = null;
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
            RecyclerView list = (RecyclerView) v.findViewById(R.id.list_certificates);
            adapter = new IdentityDataAdapter(data);
            adapter.SetFragmentView(this);
            list.setHasFixedSize(true);
            list.setLayoutManager(new LinearLayoutManager(this.getContext()));
            list.setAdapter(adapter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void ParseCertificate(IdentityData identityData) throws IOException, CertificateException {
        InputStream in = new FileInputStream(identitiesPath + File.separator + identityData.GetName() + "_cert.pem");
        String isCN = PKI.GetInstance().GetCertificateIssuerName(new FileInputStream(identitiesPath + File.separator + identityData.GetName() + "_cert.pem"));
        String subjCN = PKI.GetInstance().GetCertificateSubjectCN(new FileInputStream(identitiesPath + File.separator + identityData.GetName() + "_cert.pem"));
        String subjC = PKI.GetInstance().GetCertificateSubjectCountry(new FileInputStream(identitiesPath + File.separator + identityData.GetName() + "_cert.pem"));
        String subjO = PKI.GetInstance().GetCertificateSubjectOrganization(new FileInputStream(identitiesPath + File.separator + identityData.GetName() + "_cert.pem"));
        in.close();
        TextView textView  = (TextView)getView().findViewById(R.id.txt_issName);
        textView.setText(isCN);
        textView  = (TextView)getView().findViewById(R.id.txt_subjCN);
        textView.setText(subjCN);
        textView  = (TextView)getView().findViewById(R.id.txt_country);
        textView.setText(subjC);
        textView  = (TextView)getView().findViewById(R.id.txt_org);
        textView.setText(subjO);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_contacts, container, false);

        String idPath = v.getContext().getFilesDir().getAbsolutePath() + File.separator + "identities";
        File idDir = new File(idPath);
        if (!idDir.exists())
            idDir.mkdirs();
        identitiesPath = idDir.getAbsolutePath();

        ParseIdentities(v);

        return v;
    }

}