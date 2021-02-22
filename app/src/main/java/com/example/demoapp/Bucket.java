package com.example.demoapp;

import android.util.Log;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class Bucket {

    private String parentID;
    public static final int MAX_NODES=20;
    private int dimension;
    private ArrayList<Triplet> nodes;

    public Bucket(String parentID, int dimension){
        this.parentID = parentID;
        int logMAX = (int) (Math.log10(MAX_NODES) / Math.log10(2));
        if (dimension < logMAX)
            this.dimension = (int) Math.pow(2, dimension);
        else
            this.dimension = MAX_NODES;
        nodes = new ArrayList<Triplet>(dimension);
    }

    private boolean CheckRepeated(String id){
        boolean ret = false;
        Iterator<Triplet> it = nodes.iterator();
        while (it.hasNext() && !ret){
            if (it.next().GetID().compareToIgnoreCase(id)==0)
                ret=true;
        }
        return ret;
    }

    public Triplet ClosestNodeToID(String id){
        if (nodes.isEmpty()) return null; //return null as no resource is at zero distance
        BigInteger min = new BigInteger("2");
        min = min.pow(129); //assure that it's the highest value
        Triplet closest = null;
        Iterator<Triplet> it = nodes.iterator();
        while (it.hasNext()){
            Triplet t = it.next();
            BigInteger dist = t.GetParentDistance();
            if (dist.compareTo(min)==-1) { //dist < min
                min = dist;
                closest = t;
            }
        }
        return closest;
    }

    public void AddTriplet(Triplet triplet){
        if (!CheckRepeated(triplet.GetID())) return; //it's repeated rendez trying to fool us!
        if (nodes.size() < dimension){ //TODO IMPLEMENT PING/PONG FOR REMOVING INACTIVE TRIPLETS
            nodes.add(triplet);
            Collections.sort(nodes, (o1, o2) -> {
                BigInteger d1 = o1.GetParentDistance();
                BigInteger d2 = o2.GetParentDistance();
                return d1.compareTo(d2);
            });
        }
    }

    public void PrintBucket(){
        if (nodes.isEmpty()){Log.d("app", "EMPTY BUCKET!!\n\n"); return;}
        int i = 0;
        for (Triplet t : nodes){
            Log.d("app", String.format("Node %d", ++i));
            t.PrintTriplet();
        }
    }

}
