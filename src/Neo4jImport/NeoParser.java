/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Neo4jImport;

import ElasticImport.*;
import UniprotModule.UniprotParser;
import UniprotModule.uniprotindexer;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil
 */
public class NeoParser extends Thread{
    public ConcurrentLinkedQueue<ArrayList<String>> queue = new ConcurrentLinkedQueue();
    public ConcurrentLinkedQueue<ArrayList<String>> relationqueue = new ConcurrentLinkedQueue();
    public ConcurrentLinkedQueue<NeoImportRequest> complete = new ConcurrentLinkedQueue();
    private static HashSet<String> linkto = new HashSet<>();
    private static HashSet<String> subsets = new HashSet<>();
    File dofile;
    File ttdfile;
    Integer num_workers;
    public ArrayList<NeoParseWorker> workers = new ArrayList<>();
    public ArrayList<NeoImportThread> importers = new ArrayList<>();
    public static ConcurrentHashMap<String,Boolean> map = new ConcurrentHashMap<>(); 
    public static Boolean done = false;
    private Long filesize = 0L;
    public NeoRawdataprocesthread rawdataprocessor;

    private static HashMap<String, String> symptomlist = new HashMap<>();
    private static HashMap<String, String> locationlist = new HashMap<>();
    
    private String neo4juri = "";
    private String neo4juser = "";
    private String neo4jpassword = "";
    
    public NeoParser(File dofile, File ttdfile, Integer num_workers, String neo4juri, String neo4juser, String neo4jpassword){
        this.dofile = dofile;
        this.num_workers = num_workers;
        this.ttdfile = ttdfile;
        this.neo4juri = neo4juri;
        this.neo4juser = neo4juser;
        this.neo4jpassword = neo4jpassword;
    }
    

    
    @Override
    public void run(){
        Thread.currentThread().setName("neoparser");
        try {
            //DO layer
            System.out.println("start DiseaseOntology import...");
            doidimport();
            System.out.println("done");
        } catch (IOException ex) {
            Logger.getLogger(NeoParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void doidimport() throws IOException{
        loadtermlists();
        System.out.println("parse start");
        startworkers(num_workers,false,dofile,"DO");
        parse(dofile);
        while (complete.size() > 0 || queue.size() > 0){try {Thread.sleep(10);} catch (InterruptedException ex) {Logger.getLogger(NeoParser.class.getName()).log(Level.SEVERE, null, ex);}}
        stopworkers();
        startrelationcreation();
        while (relationqueue.size() > 0){try {Thread.sleep(10);} catch (InterruptedException ex) {Logger.getLogger(NeoParser.class.getName()).log(Level.SEVERE, null, ex);}}
        stopimporters();
        System.out.println("COMPLETE");
    }
    
    public void parse(File inputfile2){
        BufferedReader br = null;
        try {
            System.out.println("parsing: "+inputfile2.getAbsolutePath());
            try {
                filesize = filesize(inputfile2.getAbsolutePath());
            } catch (IOException ex) {
                Logger.getLogger(NeoParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        br = new BufferedReader(new FileReader(inputfile2.getAbsolutePath()), 1000 * 8192);
        String line;
        while ((line = br.readLine()) != null) {
            while (rawdataprocessor.rawdataqueue.size() > 5000){try {Thread.sleep(10);} catch (InterruptedException ex) {}}
            rawdataprocessor.rawdataqueue.offer(line);
            if (done){
                System.out.println("canceling import");
                break;
            }  
        }
        while (rawdataprocessor.rawdataqueue.size() > 0){}
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NeoParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
         Logger.getLogger(NeoParser.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
        try {
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(NeoParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
            
    }
    
    public void loadtermlists() throws FileNotFoundException, IOException{
        System.out.println("loading term lists");
        File symptomfile = new File("symptoms.txt");
        HashSet<String> symptomset = new HashSet<>();
        if (symptomfile.exists()){
            BufferedReader br = new BufferedReader(new FileReader(symptomfile.getAbsolutePath()), 1000 * 8192);
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\t", "");
                if (line.contains("<")){
                    String[] split = line.split("<");
                    symptomlist.put(split[1].trim(), split[0].trim());
                    symptomset.add(split[0].trim());
                } else {
                    symptomlist.put(line.trim(), line.trim());
                    symptomset.add(line.trim());
                }
            }
            br.close();
        }
        for (String symptom : symptomset){
            LinkedHashMap<String, ArrayList<String>> data = new LinkedHashMap<>();
            data.putIfAbsent("name", new ArrayList<>());
            data.get("name").add(symptom);
            complete.add(new NeoImportRequest("symptom",data));
        }
        File locationsfile = new File("locations.txt");
        HashSet<String> locationset = new HashSet<>();
        if (locationsfile.exists()){
            BufferedReader br = new BufferedReader(new FileReader(locationsfile.getAbsolutePath()), 1000 * 8192);
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\t", "");
                if (line.contains("<")){
                    String[] split = line.split("<");
                    locationlist.put(split[1].trim(), split[0].trim());
                    locationset.add(split[0].trim());
                } else {
                    locationlist.put(line.trim(), line.trim());
                    locationset.add(line.trim());
                }
            }
            br.close();
        }
        for (String location : locationset){
            LinkedHashMap<String, ArrayList<String>> data = new LinkedHashMap<>();
            data.putIfAbsent("name", new ArrayList<>());
            data.get("name").add(location);
            complete.add(new NeoImportRequest("location",data));
        }
    }
    
    public void startworkers(Integer amountofthreads, Boolean mapmode, File sourcefile, String type){
        rawdataprocessor = new NeoRawdataprocesthread(queue,type);
        rawdataprocessor.start();
        Integer counter = 0;
        while (counter < amountofthreads){
            counter++;
            workers.add(new NeoParseWorker(queue,complete,relationqueue,mapmode,sourcefile,type));
        }
        for (NeoParseWorker worker : workers){
            worker.start();
        }
        if (!mapmode){
            Integer importercounter = 0;
            while (importercounter < 1){
                importercounter++;
                importers.add(new NeoImportThread(complete,relationqueue,type,neo4juri,neo4juser,neo4jpassword));
            }
        }
        for (NeoImportThread importer : importers){
            importer.start();
        }  
    }
    
    public void startrelationcreation(){
        for (NeoImportThread importer : importers){
            importer.stage1 = false;
        }  
    }
    
    public void stopworkers(){
        rawdataprocessor.setThreadactive(false);
        for (NeoParseWorker worker : workers){
            worker.active = false;
        }
        workers.clear();
    }
    
    public void stopimporters(){
        for (NeoImportThread importer : importers){
            importer.active = false;
        }  
        importers.clear(); 
    }
    
    public Long filesize(String filename) throws IOException {
        long size = Files.size(new File(filename).toPath());
        return size;
    }

    

    
    synchronized public long getFilesize() {
        return filesize;
    }

    /**
     * @param filesize the filesize to set
     */
    synchronized public void setFilesize(long filesize) {
        this.filesize = filesize;
    }
    
        /**
     * @param id A doid to test
     * @return the linkto
     */
    synchronized public static Boolean checklinkto(String id) {
        return linkto.contains(id);
    }

    /**
     * @param aLinkto the linkto to set
     */
    synchronized public static void putlinkto(String aLinkto) {
        linkto.add(aLinkto);
    }
    
    synchronized public static Boolean checksubsets(String subset) {
        return subsets.contains(subset);
    }

    /**
     * @param aLinkto the linkto to set
     */
    synchronized public static void putsubset(String subset) {
        subsets.add(subset);
    }

    /**
     * @return the symptomlist
     */
    public static synchronized HashMap<String, String> getSymptomlist() {
        return symptomlist;
    }

    /**
     * @return the symptomlist
     */
    public static synchronized HashMap<String, String> getLocationslist() {
        return locationlist;
    }

}
