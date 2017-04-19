/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CazyModule;

import FileManager.Filemanager;
import UniprotModule.Uniprotmainthread;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil3
 */
public class CazyAnnotator {
    private ArrayList<File> inputfiles;
    private ArrayList<File> outputfiles;
    private ArrayList<File> cazyfiles;
    private File annotationdirectory;
    private HashMap<String, String> cazydata = new HashMap<>();
    private HashMap<String, String> uniprottocazygenbank = new HashMap<>();
    private HashMap<Integer, CazyThread> threads = new HashMap<>();
    private static volatile Long total = 0l;
    private static volatile Long done = 0l;
    private static volatile String process = "";
    private static volatile Boolean complete = false;

    /**
     *
     */
    public int progress = 0;
    private static volatile ReentrantLock lock = new ReentrantLock();
    private static volatile Integer threadsactive = 0;
    private static volatile Integer threadsdone = 0;
    
    //import cazy reference data files

    /**
     *
     * @param inputfiles
     * @param cazyfiles
     * @param annotationdirectory
     */
    public CazyAnnotator(ArrayList<File> inputfiles, ArrayList<File> cazyfiles, File annotationdirectory){
        this.inputfiles = inputfiles;
        this.cazyfiles = cazyfiles;
        this.annotationdirectory = annotationdirectory;
    }
    
    /**
     *
     */
    public void annotate(){
        System.out.println("#CAZYANNOTATOR\tSTART Cazy Annotation..\n");
        System.out.println("#CAZYANNOTATOR\tPARSING Cazy DATA..");
        setProcess("PARSING Cazy DATA");
        importcazyfiles();
        System.out.println("#CAZYANNOTATOR\tPARSING COMPLETE "+cazydata.size()+"\n#CAZYANNOTATOR\tANNOTATING "+inputfiles.size()+" FILES");
        setProcess("Cazy Annotation");
        int counter = 0;
        for (File file : inputfiles){    
            threadsactive++;
            File outputfile = new File(annotationdirectory+File.separator+file.getName()+".ᚒC");
            int version = 2;
//            while (! outputfile.exists()){
//                outputfile = new File(annotationdirectory+File.separator+file.getName()+"ᚒC V"+version);
//                version++;
//            }
            threads.put(counter, new CazyThread(file,outputfile,counter,cazydata));
            threads.get(counter).start();
            System.out.println("#CAZYANNOTATOR\tTHREAD "+counter+" STARTED");
            counter ++;       
        }
        while (threadsactive > threadsdone){ try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {Logger.getLogger(Uniprotmainthread.class.getName()).log(Level.SEVERE, null, ex);}}
        threads.clear();
        setComplete(true);
    }
    
    private void importcazyfiles(){
        for (File file : cazyfiles){
            System.out.println("#CAZYANNOTATOR\timporting: "+file.getName());
            FileInputStream inputStream = null;
            Integer uniprotimports = 0;
            try {
               inputStream = new FileInputStream(file.getAbsolutePath());
            } catch (FileNotFoundException ex) {
               Logger.getLogger(CazyAnnotator.class.getName()).log(Level.SEVERE, null, ex);
            }
            Scanner sc = new Scanner(inputStream, "UTF-8");
            sc = new Scanner(inputStream, "UTF-8");
            sc.nextLine();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String cazyuniprotid = "";
                try{
                String genbankid = line.split("\t")[3]; // POSITIE GENBANK ID
                cazyuniprotid = line.split("\t")[4];
                try{
                genbankid = genbankid.substring(0,genbankid.indexOf("."));
                } catch (Exception ex){}
                cazydata.put(genbankid, line); 
                } catch(Exception ex){System.out.println("cazy parser error on line : "+line);}
                if (cazyuniprotid.length() > 3){
                    uniprotimports++;
                    cazydata.put(cazyuniprotid, line); 
                } 
                
            }       
        }
    }

    /**
     * @return the outputfiles
     */
    public ArrayList<File> getOutputfiles() {
        return outputfiles;
    }
    
    /**
     *
     * @param process
     */
    public static void setProcess(String process) {
    lock.lock();
    try {
        CazyAnnotator.process = process;
    } finally {
        lock.unlock();
    }
    }

    /**
     *
     * @param lines
     */
    public static void setTotal(Long lines) {
    lock.lock();
    try {
        CazyAnnotator.total = lines;
    } finally {
        lock.unlock();
    }
    }

    /**
     *
     * @param lines
     */
    public static void addTotal(Long lines) {
    lock.lock();
    try {
        CazyAnnotator.total += lines;
    } finally {
        lock.unlock();
    }
    }

    /**
     *
     * @param done
     */
    public static void setDone(Long done) {
    lock.lock();
    try {
        CazyAnnotator.done = done;
    } finally {
        lock.unlock();
    }
    }

    /**
     *
     */
    public static void addDone() {
    lock.lock();
    try {
        CazyAnnotator.done++;
    } finally {
        lock.unlock();
    }
    }

    /**
     *
     * @return
     */
    public static Long getDone() {
    Long done = 0l;
    lock.lock();
    try {
        done = CazyAnnotator.done;
    } finally {
        lock.unlock();  
    }
    return done;
    }

    /**
     *
     * @return
     */
    public static Long getTotal() {
    Long total = 0l;
    lock.lock();
    try {
        total = CazyAnnotator.total;
    } finally {
        lock.unlock();  
    }
    return total;
    }

    /**
     *
     * @return
     */
    public static Long getProgress() {
    Long progress = 0l;
    lock.lock();
    try {
        progress = CazyAnnotator.done/CazyAnnotator.total;
    } finally {
        lock.unlock();  
    }
    return progress;
    }

    /**
     *
     * @return
     */
    public static String getProcess() {
    String process = "";
    lock.lock();
    try {
        process = CazyAnnotator.process;
    } finally {
        lock.unlock();  
    }
    return process;
    }

    /**
     *
     * @param complete
     */
    public static void setComplete(Boolean complete) {
    lock.lock();
    try {
        CazyAnnotator.complete = complete;
    } finally {
        lock.unlock();
    }
    }

    /**
     *
     * @return
     */
    public static Boolean getComplete() {
    lock.lock();
    try {
    } finally {
        lock.unlock();
    }
        return CazyAnnotator.complete;
    }

    /**
     *
     */
    public static void myThreaddone() {
    lock.lock();
    try {
        CazyAnnotator.threadsdone ++;;
    } finally {
        lock.unlock();
    }
    }
}
