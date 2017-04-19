/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CazyModule;

import UniprotModule.UniprotThread;
import concordia.GUIController;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil3
 */
public class CazyThread extends Thread {
    private final File inputfile;
    private final File outputfile;
    private int threadnr;
    private HashMap<String, String> cazydata;

    /**
     *
     */
    public int progress;
    
    /**
     *
     * @param inputfile
     * @param outputfile
     * @param threadnr
     * @param cazydata
     */
    public CazyThread(File inputfile, File outputfile, int threadnr, HashMap<String, String> cazydata){
        this.inputfile = inputfile;
        this.outputfile = outputfile;
        this.threadnr = threadnr;
        this.cazydata = cazydata;
    }
    
    public void run(){
        
        if (! inputfile.exists()){
            System.out.println(inputfile.getAbsolutePath()+" DOES NOT EXIST");
        } else {
            System.out.println("#CAZYANNOTATOR\tprocessing: "+inputfile.getName());
            FileInputStream stream = null;
            FileOutputStream outputstream = null;
            try {
                stream = newfilestream();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CazyThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                outputstream = new FileOutputStream(outputfile.getAbsolutePath());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CazyThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            Scanner sc = new Scanner(stream, "UTF-8");
                   LineNumberReader lnr;
            Long maxlines = 0L;
            try {
                lnr = new LineNumberReader(new FileReader(inputfile));
                lnr.skip(Long.MAX_VALUE);
                maxlines = Long.parseLong(Integer.toString(lnr.getLineNumber()));   
            } catch (IOException ex) {
                Logger.getLogger(UniprotThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            CazyAnnotator.addTotal(maxlines);
            System.out.println("added "+maxlines+" to remaining progress");
            Integer found = 0;
            Integer uniprotfound = 0;
            //GUIController.parserloadbar.setcurrentprocess("Cazy annotation");
        while (sc.hasNextLine()) {
            // LOOP over regels van bestand
            String line = sc.nextLine();
            CazyAnnotator.addDone();
            String uniprotid = "";
            String EMBLprotein = "";
            String ecs = "";
            String locustags = "";
            String families = "";
            String organisms = "";
            
            ArrayList<String> genbankids = new ArrayList<>();
            try{
            EMBLprotein = line.split("\t")[22];
            uniprotid = line.split("\t")[12];
            } catch (Exception ex){}
            
            if (EMBLprotein.length() > 1){
            if (EMBLprotein.contains(";")){
                for (String id : EMBLprotein.split(";")){
                    id = id.trim();
                    try{
                    genbankids.add(id.substring(0,id.indexOf(".")));
                    } catch (Exception ex){
                    genbankids.add(id);
                }
                }
            } else {
                EMBLprotein = EMBLprotein.trim();
                try{
                    genbankids.add(EMBLprotein.substring(0,EMBLprotein.indexOf(".")));
                } catch (Exception ex){
                    genbankids.add(EMBLprotein);
                }
                
            }

            
            boolean foundgenbank = false;
            for (String genbankid : genbankids){
                if (cazydata.containsKey(genbankid) && genbankid.length()>4){
                    foundgenbank = true;
                    found++;
                    String cazyline = cazydata.get(genbankid);
                    String[] cazysplit = cazyline.split("\t");
                    if (! ecs.isEmpty()){ 
                    ecs +="; "+cazysplit[2];
                    } else {ecs+=cazysplit[2];}
                    
                    if (! locustags.isEmpty()){
                    locustags+="; "+cazysplit[0];;
                    } else {locustags+=cazysplit[0];}
                    
                    if (! families.isEmpty()){
                    families +="; "+cazysplit[1];
                    } else {families+=cazysplit[1];}
                    
                    if (! organisms.isEmpty()){
                        organisms+="; "+cazysplit[5];
                    } else {organisms+=cazysplit[5];}
                }  
            }        
            if (cazydata.containsKey(uniprotid) && uniprotid.length()>4 && foundgenbank == false){
                uniprotfound++;
                String cazyline = cazydata.get(uniprotid);
                String[] cazysplit = cazyline.split("\t");
                if (! ecs.isEmpty()){ 
                ecs +="; "+cazysplit[2];
                } else {ecs+=cazysplit[2];}

                if (! locustags.isEmpty()){
                locustags+="; "+cazysplit[0];;
                } else {locustags+=cazysplit[0];}

                if (! families.isEmpty()){
                families +="; "+cazysplit[1];
                } else {families+=cazysplit[1];}

                if (! organisms.isEmpty()){
                    organisms+="; "+cazysplit[5];
                } else {organisms+=cazysplit[5];}
            }  
            }
                line = line+"\t"+ecs+"\t"+locustags+"\t"+families+"\t"+organisms;

            line = line+"\n";
            line = line.replace("\uFFFD", "");
            try {
                outputstream.write(line.getBytes());
                outputstream.flush();
            } catch (IOException ex) {
                Logger.getLogger(CazyThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //GUIController.parserloadbar.adddone(1);
    }
            try {
                outputstream.close();
            } catch (IOException ex) {
                Logger.getLogger(CazyThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("linked: "+found+" using genbank id's");
            System.out.println("linked: "+uniprotfound+" using uniprot id's");
            CazyAnnotator.myThreaddone();
            
    }
    }
    private FileInputStream newfilestream () throws FileNotFoundException{
        FileInputStream inputStream = null;
        inputStream = new FileInputStream(inputfile.getAbsolutePath());
        return inputStream;
    }
    
   
}
