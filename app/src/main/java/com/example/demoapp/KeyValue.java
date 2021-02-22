package com.example.demoapp;

import java.math.BigInteger;

public class KeyValue {

    private String key;
    private byte[] data;
    private BigInteger parentDistance;

    public KeyValue(String key, byte[] data, String parentID){
        this.key = key;
        this.data = data;
        parentDistance = Triplet.Distance(key, parentID);
    }

    public String GetKey() { return key; }
    public byte[] GetData(){ return data; }
    public BigInteger GetParentDistance() { return parentDistance; }

}
