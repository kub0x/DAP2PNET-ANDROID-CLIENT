package com.example.demoapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Utils {

    private static Utils myInstance = new Utils();

    private Utils(){}

    public static Utils GetInstance() { return myInstance; }

    public void WriteToFile(String path, String fileName, String data) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(path  + File.separator + fileName));
        fos.write(data.getBytes());
        fos.flush();
        fos.close();
    }

    public String ReadFile(String path, String fileName) throws IOException {
        String data = "";
        BufferedReader br  = new BufferedReader(new InputStreamReader(new FileInputStream( new File(path  + File.separator + fileName)))); //BLOAT? NAH
        String line=null;
        while( (line=br.readLine()) != null )
        {
            data+=line;
        }
        return data;
    }

}
