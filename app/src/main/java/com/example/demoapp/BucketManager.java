package com.example.demoapp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;

public class BucketManager {

    public static int BIT_LENGTH = 128;
    private String parentID;
    private ArrayList<Bucket> buckets;
    private Hashtable<String, KeyValue> store = new Hashtable<>();

    public BucketManager(String parentID){
        this.parentID = parentID;
        buckets = new ArrayList<Bucket>(BIT_LENGTH);
        for (int i=0;i < BIT_LENGTH; i++){
            buckets.add(new Bucket(parentID, i));
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HandleConnection();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private String ReadPeerData(BufferedReader inStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        int  c=0;
        while ((c = inStream.read()) > 0){
            char ch = (char) c;
            //Log.d("app", "got new char: " + ch);
            sb.append(ch);
        }
        Log.d("app", "peer read!");
        if (c == -1) return "-1"; //hits EOF
        return sb.toString();
    }

    public void HandleConnection() throws IOException, InterruptedException {
        ServerSocket sock = new ServerSocket(44442, 10);
        while (true) {
            Socket client = sock.accept();
            BufferedReader inStream = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter outStream = new PrintWriter(client.getOutputStream());
            String message = ReadPeerData(inStream);
            if (message.compareTo("-1") != 0) {
                if (message.contains("--STORE--")) {
                    String data = message.replace("--STORE--", "");
                    String[] sep = data.split(":");
                    String id = sep[0];
                    String value = sep[1];
                    store.put(id, new KeyValue(id, value.getBytes(), parentID));
                    Log.d("app", "STORED!");
                }
            } else if (message.contains("--FIND--")) {
                String keyID = message.replace("--FIND--", "");
                KeyValue valueToFind = null;
                if (store.contains(keyID))
                    valueToFind = store.get(keyID);
                if (valueToFind != null) {
                    Log.d("app", "Responding!");
                    outStream.write("--VALUE--" + keyID + ":" + new String(valueToFind.GetData(), StandardCharsets.UTF_8));
                    outStream.flush();
                }
            } else if (message.contains("--VALUE--")) {
                Log.d("app", "Received value!");
                String data = message.replace("--VALUE--", "");
                String[] sep = data.split(":");
                String id = sep[0];
                String value = sep[1];
                Log.d("app", value);
                store.put(id, new KeyValue(id, value.getBytes(), parentID));
            }
            Thread.sleep(300);
        }
    }

    private int GetBucketIndex(BigInteger dist){
        return (int) (Math.log10(dist.doubleValue()) / Math.log10(2));
    }

    private Bucket FindBucket(BigInteger dist){
        int buckIndex = GetBucketIndex(dist);
        return buckets.get(buckIndex);
    }

    public void AddTriplet(Triplet triplet){
        Bucket b = FindBucket(triplet.GetParentDistance());
        b.AddTriplet(triplet);
    }

    public void AddKey(KeyValue pair) throws IOException {
        //Simple STORE_RPC of Kad
        //Find closest node to a value
        Triplet closest = FindNode(pair);
        if (closest != null){
            //Connect to closest and store data
            String strData = "--STORE--" + pair.GetKey() + ":" + new String(pair.GetData(), StandardCharsets.UTF_8);
            SecureSocket.SendToPeer(closest.GetIP(), closest.GetPort(), strData);
        }
    }

    public KeyValue QueryKey(String keyID) throws IOException {
        //FIND_VALUE RPC
        //First look at the store, maybe other node stored the data there
        KeyValue valueToFind = null;
        if (store.contains(keyID))
            valueToFind = store.get(keyID);
        Triplet closest = FindNode(new KeyValue(keyID, null, parentID));
        if (closest != null){
            //Ask closest for the value
            String strData = "--FIND--" + keyID;
            SecureSocket.SendToPeer(closest.GetIP(), closest.GetPort(), strData);
        }
        return valueToFind;
    }

    public Triplet FindNode(KeyValue pair){
        //NODE_VALUE RPC
        Bucket b = FindBucket(pair.GetParentDistance());
        Triplet closest = b.ClosestNodeToID(pair.GetKey());
        int buckIndex = GetBucketIndex(pair.GetParentDistance());
        int start = buckIndex;
        boolean ret = false;
        while (closest == null && !ret){
            if (buckIndex==0) buckIndex = BIT_LENGTH - 1;
            buckIndex--;
            if (buckIndex == start) //we searched whole bucket space!
                ret = true;
            b = buckets.get(buckIndex);
            closest = b.ClosestNodeToID(pair.GetKey());
        }
        return closest;
    }

    public void PrintTriplets(){
        int i = 0;
        for (Bucket b : buckets) {
            Log.d("app", String.format("Bucket %d", ++i));
            b.PrintBucket();
        }
    }

    public String GetParentID() { return parentID; }

}
