/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UniprotModule;

import concordia.GUIController;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil3
 */
public class UniprotThread extends Thread {
    private final File inputfile;
    private final File outputfile;
    private int threadnr;
    private HashMap<Integer, String> uniprotdata;

    /**
     *
     */
    public int progress;
    
    /**
     *
     * @param inputfile
     * @param outputfile
     * @param threadnr
     * @param uniprotdata
     */
    public UniprotThread(File inputfile, File outputfile, int threadnr, HashMap<Integer, String> uniprotdata){
        this.inputfile = inputfile;
        this.outputfile = outputfile;
        this.threadnr = threadnr;
        this.uniprotdata = uniprotdata;
    }
    
    public void run(){
        if (! inputfile.exists()){
            System.out.println(inputfile.getAbsolutePath()+" DOES NOT EXIST");
        } else {
            System.out.println("#UNIPROT-ANNOTATOR\tprocessing: "+inputfile.getName());
            FileInputStream stream = null;
            FileOutputStream outputstream = null;
            try {
                stream = newfilestream();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(UniprotThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                outputstream = new FileOutputStream(outputfile.getAbsolutePath());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(UniprotThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            Scanner sc = new Scanner(stream, "UTF-8");
            LineNumberReader lnr;
            Integer maxlines = 0;
            try {
                lnr = new LineNumberReader(new FileReader(inputfile));
                lnr.skip(Long.MAX_VALUE);
                maxlines = lnr.getLineNumber();
                
            } catch (IOException ex) {
                Logger.getLogger(UniprotThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            //GUIController.parserloadbar.addtotal(maxlines);
            System.out.println("added "+maxlines+" to remaining progress");
            //GUIController.parserloadbar.setcurrentprocess("UniProt annotation");
            Integer annotatednr = 0;
            Integer totalnr = 0;
        while (sc.hasNextLine()) {
            // LOOP over regels van bestand
            String line = sc.nextLine();
            totalnr++;
            try {
                String giitem = line.split("\t")[1];
                String gi = giitem.substring(3, giitem.indexOf("ref")-1); 
                int giint = Integer.parseInt(gi);   
                if (uniprotdata.containsKey(giint)){
                String uniprotline = uniprotdata.get(giint);
                annotatednr++;
                line = line + "\t" + uniprotline; 
                } else {
                    line = line + "\t \t \t \t \t \t \t \t \t \t \t \t \t \t \t \t \t \t";
                }
                
            } catch (Exception ex){
                        line = line + "\t \t \t \t \t \t \t \t \t \t \t \t \t \t \t \t \t \t";
            }
   

            line = line + "\n";
            try {
                outputstream.write(line.getBytes());
                outputstream.flush();
            } catch (IOException ex) {
                Logger.getLogger(UniprotThread.class.getName()).log(Level.SEVERE, null, ex);
            }

            //GUIController.parserloadbar.adddone(1);
            }
            try {
                outputstream.close();
            } catch (IOException ex) {
                Logger.getLogger(UniprotThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("annotated: "+annotatednr+" out of: "+totalnr);
    }
    }
    private FileInputStream newfilestream () throws FileNotFoundException{
        FileInputStream inputStream = null;
        inputStream = new FileInputStream(inputfile.getAbsolutePath());
        return inputStream;
    }
    
   
}
