package com.example.demoapp.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.CertificatesFragment;
import com.example.demoapp.R;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

public class IdentityDataAdapter extends RecyclerView.Adapter<IdentityDataAdapter.ViewHolder> {

    private ArrayList<IdentityData> data = new ArrayList<IdentityData>();
    private int selectedPos = RecyclerView.NO_POSITION;
    public IdentityDataAdapter(ArrayList<IdentityData> data) {
        this.data = data;
    }
    private IdentityData selectedIdentity = null;
    private Fragment fragmentView=null;

    public void SetFragmentView(Fragment f){
        fragmentView=f;
    }

    private void OnSelectedCertificateItem() throws IOException, CertificateException {
        ((CertificatesFragment)fragmentView).ParseCertificate(selectedIdentity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.layout_indentity_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setSelected(selectedPos == position);
        holder.itemView.setBackgroundColor(selectedPos == position ? Color.WHITE : Color.TRANSPARENT);
        final IdentityData tmp = data.get(position);
        holder.textView.setText(tmp.GetName());
        holder.textView3.setText(tmp.GetId());
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                notifyItemChanged(selectedPos);
                selectedIdentity = data.get(selectedPos);
                if (fragmentView!= null) {
                    try {
                        OnSelectedCertificateItem();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (CertificateException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void RemoveSelectedIdentity(String identitiesPath){
        if (data.size() == 0 || selectedPos == RecyclerView.NO_POSITION) return;
        String selectedFileName = data.get(selectedPos).GetName();
        File dir = new File(identitiesPath);
        for (String fileName : dir.list()) {
            if (fileName.contains(selectedFileName)){
                //eliminate all files related to the identity name
                File f = new File(identitiesPath + File.separator + fileName);
                f.delete();
            }
        }
        data.remove(selectedPos);
        notifyItemRemoved(selectedPos);
        notifyItemRangeChanged(selectedPos, data.size());
    }

    public IdentityData GetSelectedIdentity() { return selectedIdentity; }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public TextView textView3;
        public RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = (ImageView) itemView.findViewById(R.id.imageView);
            this.textView = (TextView) itemView.findViewById(R.id.textView);
            this.textView3 = (TextView) itemView.findViewById(R.id.textView3);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);
        }

    }
}
