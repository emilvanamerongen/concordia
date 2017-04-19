/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UniprotModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil3
 */
public class Requiredgithread extends Thread{
    File inputfile;
    
    Requiredgithread(File inputfile){
        this.inputfile = inputfile;
    }
    public void run(){
        HashSet<Integer> requiredgis = new HashSet<>();
        HashSet<String> requiredgenbank = new HashSet<>();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(inputfile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Requiredgithread.class.getName()).log(Level.SEVERE, null, ex);
        }
        Scanner sc = new Scanner(inputStream, "UTF-8");
        sc = new Scanner(inputStream, "UTF-8");
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            try {
                String giitem = line.split("\t")[1];
                String gi = giitem.substring(3, giitem.indexOf("ref")-1); 
                String genbank = ""; 
                genbank = giitem.substring(giitem.indexOf("ref")+4, giitem.length()-1);
                int giint = Integer.parseInt(gi);
                requiredgis.add(giint);
                requiredgenbank.add(genbank);
            } catch(Exception ex){
                
            }
        }
        sc.close();
        try {
            inputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(Requiredgithread.class.getName()).log(Level.SEVERE, null, ex);
        }
        Uniprotmainthread.addRequiredgis(requiredgis);
        Uniprotmainthread.addRequiredgenbank(requiredgenbank);
        requiredgis.clear();
        requiredgenbank.clear();
        Uniprotmainthread.myThreaddone();
    }
}
