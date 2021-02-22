package com.example.demoapp.ui;

public class IdentityData {

    private String name;
    private String id;

    public IdentityData(String name, String id){
        this.name=name;
        this.id=id;
    }

    public void SetName(String name){ this.name=name; }
    public void SetId(String id) { this.id=id; }
    public String GetName() { return name; }
    public String GetId() { return id; }
}
