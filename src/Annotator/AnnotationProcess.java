/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Annotator;

import FileManager.Filemanager;
import Refdbmanager.header;
import Refdbmanager.refdb;
import TabDelimitedModule.TabDelimitedEntry;
import Tools.ByteArrayWrapper;
import UniprotModule.IDMappingEntry;
import UniprotModule.UniprotEntry;
import UniprotModule.UniprotParser;
import UniprotModule.uniprotindexer;
import concordia.GUIController;

import gnu.trove.map.hash.THashMap;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.lang.instrument.Instrumentation;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author emil3
 */
public class AnnotationProcess extends Thread{
    private ArrayList<File> files;
    private ArrayList<File> outputfiles;
    private ArrayList<refdb> annotationsources = new ArrayList<>();
    private LinkedHashMap<String,LinkedHashSet<header>> links = new LinkedHashMap<>();
    private refdb template;
    private Filemanager filemanager = new Filemanager();
    private LinkedHashMap<String, LinkedHashMap<header, LinkedHashSet<String>>> outputheaders = new LinkedHashMap<>();
    private ConcurrentHashMap<String, ConcurrentHashMap<String, HashSet<String>>> outputheadersstrings = new ConcurrentHashMap<>();
    private HashMap<String, Integer> dbstartindex = new HashMap<>();
    private HashMap<Integer, HashMap<String, String>> requestedlinks = new HashMap<>();
    private Boolean firstrun = true;
    private LinkedHashMap<Integer,String> templateheadermap = new LinkedHashMap<>();
    private LinkedHashMap<String, AnnotationFileInfo> progressset = new LinkedHashMap<>();
    private Boolean done = false;
    public static ConcurrentLinkedQueue<AnnotationRequest> queue = new ConcurrentLinkedQueue();
    public static ConcurrentLinkedQueue<AnnotationRequest> complete = new ConcurrentLinkedQueue();
    private ArrayList<AnnotatorThread> annotatorthreads = new ArrayList<>();
    private ArrayList<ReaderThread> readerthreads = new ArrayList<>();
    private ArrayList<String> dborder = new ArrayList<>();
    private HashMap<String, ArrayList<String>> headerorder = new HashMap<>();
    public static ConcurrentHashMap<String, ConcurrentLinkedQueue<ReadRequest>> readrequests = new ConcurrentHashMap();
    public static ConcurrentHashMap<Integer, ConcurrentLinkedQueue<ReadRequest>> completedreadrequests = new ConcurrentHashMap();
    
    
    public AnnotationProcess(ArrayList files, refdb template, ArrayList<refdb> annotationsources){
        this.annotationsources = annotationsources;
        this.template = template;  
        this.files = files;
    }
    
        public void run(){
            System.out.println("annotation started: ");
            String filestring = "";
            for (File myfile: files){
                filestring += myfile.getName()+"\t";   
                
            }
            
            String sourcestring = "";
            for (refdb mydb: annotationsources){
                sourcestring += mydb.getDbname()+"\t";                
            }
                    
                    
            System.out.println("files:\t"+filestring);
            System.out.println("annotation sources:\t"+sourcestring);
            outputheaders.put("template", new LinkedHashMap<>());
            for (header templateheader : template.getHeaderset()){
                outputheaders.get("template").put(templateheader, new LinkedHashSet<>());
            }

            for (refdb mydb: annotationsources){
                outputheaders.put(mydb.getDbname(), new LinkedHashMap<>());
                for (header aheader : mydb.getHeaderset()){
                outputheaders.get(mydb.getDbname()).put(aheader, new LinkedHashSet<>());
                }
            }


            outputheadersstrings.put(template.getDbname(), new ConcurrentHashMap<>());
            dborder.add(template.getDbname());
            for (header templateheader : template.getHeaderset()){
                headerorder.putIfAbsent(template.getDbname(), new ArrayList<>());
                headerorder.get(template.getDbname()).add(templateheader.getHeaderstring());
                outputheadersstrings.get(template.getDbname()).put(templateheader.getHeaderstring(), new HashSet<>());
            }

            for (refdb mydb: annotationsources){
                outputheadersstrings.put(mydb.getDbname(), new ConcurrentHashMap<>());
                dborder.add(mydb.getDbname());
                for (header aheader : mydb.getHeaderset()){
                    headerorder.putIfAbsent(mydb.getDbname(), new ArrayList<>());
                    headerorder.get(mydb.getDbname()).add(aheader.getHeaderstring());
                    outputheadersstrings.get(mydb.getDbname()).put(aheader.getHeaderstring(), new HashSet<>());
                }
            }

                
            Integer tabindex = 0;
            for (LinkedHashMap<header, LinkedHashSet<String>> headerset : outputheaders.values()){
                for (header myheader : headerset.keySet()){
                    if (myheader.getEnabled() && !myheader.getColorindex().equals(0)){
                        requestedlinks.putIfAbsent(myheader.getColorindex(), new HashMap<>());
                        requestedlinks.get(myheader.getColorindex()).put(myheader.getSourcedb(),myheader.getHeaderstring());
                        System.out.println("requested link: "+myheader.getSourcedb()+" "+myheader.getHeaderstring()+" color: "+myheader.getColorindex());
                    }
                    tabindex++;
                }
            }
            startannotators(120);
            startreaders(30);
 
            try {
            annotationrun();
            } catch (IOException ex) {
                Logger.getLogger(AnnotationProcess.class.getName()).log(Level.SEVERE, null, ex);
            }

            stopannotators();
            stopreaders();
        }

        public void startannotators(Integer amount){
            int currentamount = 0;
            while (currentamount < amount){
                annotatorthreads.add(new AnnotatorThread(currentamount,annotationsources,template,outputheaders,outputheadersstrings,requestedlinks,dborder,headerorder));
                completedreadrequests.putIfAbsent(currentamount, new ConcurrentLinkedQueue<>());
                currentamount++;
            }
            for (AnnotatorThread annotatorthread : annotatorthreads){
                annotatorthread.start();
            }
        }
        
        public void stopannotators(){
            for (AnnotatorThread annotatorthread : annotatorthreads){
                annotatorthread.setActive(false);
            }
            
        }
        
        public void startreaders(Integer amount){
            for (refdb sourcedb : annotationsources){
                int currentamount = 0;
                while (currentamount < amount){
                    readerthreads.add(new ReaderThread(sourcedb));
                    currentamount++;
                }
            }
            for (ReaderThread readerthread : readerthreads){
                readerthread.start();
            }
        }
        
        public void stopreaders(){
            for (ReaderThread readerthread : readerthreads){
                readerthread.setActive(false);
            }
        }
        
        
        public void annotationrun() throws IOException{
            for (File myfile : files){
                File outputfile = new File(filemanager.annotationmanager.getAnnotationdirectory()+File.separator+myfile.getName());
                getProgressset().put(myfile.getAbsolutePath(), new AnnotationFileInfo(myfile));   
                WriterThread writer = new WriterThread(outputfile);
                writer.start();
                processfile(myfile,outputfile.getAbsoluteFile(),progressset.get(myfile.getAbsolutePath()));
                while (!complete.isEmpty()){
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(AnnotationProcess.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                writer.setActive(false);
            }
        }
        
        public void processfile(File inputfile, File outputfile, AnnotationFileInfo progresssetitem){
            RandomAccessFile stream = null;
            try {
                stream = new RandomAccessFile(inputfile, "r");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(uniprotindexer.class.getName()).log(Level.SEVERE, null, ex);
            }
                
                try {
                String line = "";
                Integer linenr = 0;
                FileChannel fileChannel = stream.getChannel();
                ByteBuffer buffer = ByteBuffer.allocate(600);
                int bytes = fileChannel.read(buffer);
                int headerindex = Integer.parseInt(template.getHeaderindex());
                bytes = fileChannel.read(buffer);
                Boolean process = false;
                
                while (bytes != -1) {
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            char character = (char) buffer.get();
                            if (character=='\n'){
                                if (!process && linenr.equals(headerindex)){
                                    process = true;
                                } else if (process){
                                    progresssetitem.setProgress(fileChannel.position());
                                    Boolean queued = false;
                                    //System.out.println(queue.size()+"-"+complete.size()+"-"+readrequests.size()+"-"+completedreadrequests.size());
                                    while (!queued){
                                        if (queue.size() < 300 && complete.size() < 300){
                                            queue.add(new AnnotationRequest(line));
                                            queued = true;                                           
                                        }
                                    }
                                }
                                line = "";
                                linenr++;
                            } else {
                            line += character;
                            }
                        }

                        buffer.clear();
                        bytes = fileChannel.read(buffer);

                    }
                    } catch (IOException ex) {
                        System.out.println("IOException");
                    } catch (IndexOutOfBoundsException ex) {
                        System.out.println("index out of bounds");
                    } finally {
                        try {
                            stream.close();
                            System.out.println("done");
                        } catch (IOException ioe) {
                            System.out.println("IOException");
                        }
                    }
        }
        
        
        
        public void loadindex(header myheader, refdb db){ 
            THashMap<ByteArrayWrapper, long[]> index = new THashMap<>(1,0.75f);
                System.out.println("loading: "+filemanager.getIndexdirectory()+File.separator+myheader.getSourcedb()+"."+myheader.getHeaderstring()+".ser");
                try
                {
                   FileInputStream fis = new FileInputStream(filemanager.getIndexdirectory()+File.separator+myheader.getSourcedb()+"."+myheader.getHeaderstring()+".ser");
                   ObjectInput ois = new ObjectInputStream(fis);

                   index = (THashMap) ois.readObject();
                   System.out.println(index.size());

                   GUIController.indexstorage.putIfAbsent(myheader.getSourcedb(), new Searcher(db));
                   GUIController.indexstorage.get(myheader.getSourcedb()).adddata(index, myheader);
                   ois.close();
                   fis.close();
                   
                }catch(IOException ioe)
                {
                   ioe.printStackTrace();
                   return;
                }catch(ClassNotFoundException c)
                {
                   System.out.println("Class not found");
                   c.printStackTrace();
                   return;
                }
                System.out.println("Index loaded");
                
                
        }
        
        public void unloadindex(header myheader){
            GUIController.indexstorage.get(myheader.getSourcedb()).removedata(myheader);
            System.out.println("Index unloaded..");
            System.gc();
        }

    /**
     * @return the progressset
     */
    synchronized public LinkedHashMap<String, AnnotationFileInfo> getProgressset() {
        return progressset;
    }

    /**
     * @param progressset the progressset to set
     */
    synchronized public void setProgressset(LinkedHashMap<String, AnnotationFileInfo> progressset) {
        this.progressset = progressset;
    }

    /**
     * @return the done
     */
    synchronized public Boolean getDone() {
        return done;
    }

    /**
     * @param done the done to set
     */
    synchronized public void setDone(Boolean done) {
        this.done = done;
    }
        
        
        
    }
    
    
    
    
    
    

