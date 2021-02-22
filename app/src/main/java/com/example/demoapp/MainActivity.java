package com.example.demoapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;

public class MainActivity extends Fragment {


    /*private String csrStr;
    private P2PNode node = new P2PNode();
    private BucketManager buckMgr;*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main, container, false);
    }

   /* public static String randomForBitsNonZero(int numBits, Random r) {
        BigInteger candidate = new BigInteger(numBits, r);
        String bitStr = candidate.toString(2);
        while(bitStr.length() < BucketManager.BIT_LENGTH) {
            if (bitStr.length() == BucketManager.BIT_LENGTH-1){
                bitStr = "0" + bitStr;
                break;
            }else{
                candidate = new BigInteger(numBits, r);
                bitStr = candidate.toString(2);
            }
        }
        return bitStr;
    }

    public void OnClick(View v){
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Random r = new Random();
                    buckMgr = new BucketManager(randomForBitsNonZero(BucketManager.BIT_LENGTH, r));
                    HashSet<String> set = new HashSet<String>(25);
                    set.add(buckMgr.GetParentID());
                    Log.d("app", String.format("parentID=%s", buckMgr.GetParentID()));
                    for (int i=0; i < 150000; i++){
                        String id = randomForBitsNonZero(BucketManager.BIT_LENGTH, r);
                        while (set.contains(id)) id = randomForBitsNonZero(BucketManager.BIT_LENGTH, r);
                        set.add(id);
                        Triplet t = new Triplet(id, "", 0, buckMgr.GetParentID());
                        //Log.d("app", String.format("ID=%s",t.GetID()));
                        buckMgr.AddTriplet(t);
                    }
                    buckMgr.PrintTriplets();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }*/
}