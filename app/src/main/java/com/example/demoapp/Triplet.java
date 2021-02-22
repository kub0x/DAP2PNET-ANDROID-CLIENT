package com.example.demoapp;

import android.util.Log;
import java.math.BigInteger;

public class Triplet {

    private String ID, IP;
    private int port;
    private BigInteger parentDistance;

    public Triplet(String ID, String IP, int port, String parentID){
        this.ID = ID;
        this.IP = IP;
        this.port = port;
        this.parentDistance = Distance(ID, parentID);
    }

    public String GetID() { return ID; }
    public int GetPort() { return port; }
    public String GetIP() { return IP; }
    public BigInteger GetParentDistance() { return parentDistance; }

    public BigInteger Distance(String tID){
        return Distance(ID, tID);
    }

    public static BigInteger Distance(String ID1, String ID2){
        //Log.d("app", String.format("%d, %d", ID1.length(), ID2.length()));
        StringBuffer sb = new StringBuffer();
        for (int i=0; i < ID1.length(); i++){
            sb.append(ID1.charAt(i) ^ ID2.charAt(i));
        }
        return new BigInteger(sb.toString(), 2);
    }

    public void PrintTriplet(){
        Log.d("app", String.format("ID=%s, IP=%s, Port=%d, Distance=%d", ID, IP, port, parentDistance));
    }

}
