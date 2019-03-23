/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ElasticImport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

/**
 *
 * @author emil
 */
public class Parser extends Thread{
    //public ConcurrentLinkedQueue<ArrayList<String>> queue = new ConcurrentLinkedQueue();
    //public ConcurrentLinkedQueue<LinkedHashMap<String, ArrayList<String>>> complete = new ConcurrentLinkedQueue();
    public ArrayBlockingQueue<ArrayList<String>> queue = new ArrayBlockingQueue<>(100, true);
    public ArrayBlockingQueue<LinkedHashMap<String, ArrayList<String>>> complete = new ArrayBlockingQueue<>(100, true);
    public ArrayBlockingQueue<String> rawdataqueue = new ArrayBlockingQueue<>(10000, true);
    File inputfile;
    String entrydelimiter;
    String entryenddelimiter;
    Integer num_workers;
    public ArrayList<ParseWorker> workers = new ArrayList<>();
    public ArrayList<Rawdataprocesthread> splitters = new ArrayList<>();
    public ArrayList<ElasticImportThread> importers = new ArrayList<>();
    private ArrayList<String> customtypes = new ArrayList<>();
    public static ConcurrentHashMap<String,Boolean> map = new ConcurrentHashMap<>(); 
    public Boolean done = false;
    Boolean skipone = false;
    public Boolean mappingdone = false;
    private String type = "universal";
    private Long filesize = 0L;
    private String indexname = "";
    private TransportClient client;
    private Long tabheaderlineindex = 0L;
    //resume
    private Boolean resume = false;
    public Boolean pause = false;
    private Long resumelocation = 0L;
    private Long resumelineposition = 0L;
    private Long linesdone = 0L;
    private BulkProcessor bulkProcessor;
    
    public Parser(File inputfile, String indexname, String entrydelimiter, String type, Integer num_workers, String customtypes,TransportClient client){
        this.inputfile = inputfile;
        this.entrydelimiter = entrydelimiter;
        this.num_workers = num_workers;
        this.type = type;
        this.indexname = indexname;
        this.client = client;
        if (this.indexname.isEmpty()){
            this.indexname = inputfile.getName();
        }
        try{
            String[] typelist = customtypes.split(",");
            for (String customtype : typelist){
                this.customtypes.add(customtype);
            }
        } catch (Exception ex){}
    }
    
    
    public Parser(File inputfile, String indexname, String entrydelimiter, String type, Integer num_workers, String customtypes,TransportClient client,Long tabheaderlineindex){
        this.inputfile = inputfile;
        this.entrydelimiter = entrydelimiter;
        this.num_workers = num_workers;
        this.type = type;
        this.indexname = indexname;
        this.client = client;
        this.tabheaderlineindex = tabheaderlineindex;
        if (this.indexname.isEmpty()){
            this.indexname = inputfile.getName();
        }
        try{
            String[] typelist = customtypes.split(",");
            for (String customtype : typelist){
                this.customtypes.add(customtype);
            }
        } catch (Exception ex){}
    }
    
    public void checkentrydelimiter(){
        if (entrydelimiter.equals("")){
            if (type.equals("uniprot")){
                entrydelimiter = "<entry ";
                skipone = true;
            }
            
        }
    }
    
    public void namemodifications(){
        indexname = type+"."+indexname+"."+tabheaderlineindex;
    }
    
    @Override
    public void run(){
        checkentrydelimiter();
        namemodifications();
        
        File mapfile = new File(inputfile.getName()+".MAP");
        File resumefile = new File(inputfile.getName()+".resume");
        File linepositionfile = new File(inputfile.getName()+".lineposition");
        LinkedHashMap fixedmap = new LinkedHashMap<>();
        if (resumefile.exists()){
            resumelocation = ReadResumeLocation(resumefile.getAbsolutePath());
            tabheaderlineindex = resumelocation;
        }
        if (linepositionfile.exists()){
            resumelineposition = ReadResumeLocation(linepositionfile.getAbsolutePath());
        }
        
        if (type.equals("tab")){
            System.out.println("generating header map");
        }

        System.out.println("parse start");
        if (inputfile.isDirectory()){
            for (File subfile : inputfile.listFiles()){
                startworkers(num_workers,false,fixedmap,subfile);
                parse(subfile,tabheaderlineindex);
                //wait for queue to empty
                while (!queue.isEmpty() || !complete.isEmpty()){try {Thread.sleep(10);} catch (InterruptedException ex) {}}
                tabheaderlineindex = 0L;
                stopworkers();    
            }
        } else {
            startworkers(num_workers,false,fixedmap,inputfile);
            parse(inputfile,tabheaderlineindex);
            //wait for queue to empty
            while (!queue.isEmpty() || !complete.isEmpty()){try {Thread.sleep(10);} catch (InterruptedException ex) {}}
            if (pause){
                Long lineposition = Rawdataprocesthread.getLineposition();
                WriteObjectToFile(lineposition, linepositionfile.getAbsolutePath());
                WriteObjectToFile(linesdone, resumefile.getAbsolutePath());
            }
            stopworkers();
        }
        bulkProcessor.close();
        //refresh(true);
        System.out.println("COMPLETE");
        done = true;
        
    }
    
    public void parse(File inputfile2, Long skiplines){
        BufferedReader br = null;
        Boolean started = false;
        if (skiplines.equals(0L)){
            started = true;
        }
        try {
            System.out.println("parsing: "+inputfile2.getAbsolutePath());
        try {
            filesize = filesize(inputfile2.getAbsolutePath());
        } catch (IOException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }

        FileReader myreader = new FileReader(inputfile2.getAbsolutePath());
        br = new BufferedReader(myreader,81920);
        
        
        int i;
        char c;
        int charindex = 0;
        StringBuilder sb = new StringBuilder();
        String text = "";
        int entrydelimiterlastindex = entrydelimiter.length()-1;
        try{
        while ((i = br.read()) != -1) {
            // convert i to char
            linesdone++;
            if (!started){
                skiplines--;
                if (skiplines.equals(0L)){
                    started = true;
            }
            }
            c = (char) i;
            sb.append(c);
            if (c  == entrydelimiter.charAt(charindex)){
                charindex++;
                if (charindex == entrydelimiterlastindex){
                    charindex = 0;
                    text = sb.toString();
                    sb = new StringBuilder();
                    if (pause){
                        System.out.println("trying to stop import");
                        while (!rawdataqueue.offer(text,1000L,TimeUnit.SECONDS)){};
                        break;
                    } else{
                        if (!skipone){
                            while (!rawdataqueue.offer(text,1000L,TimeUnit.SECONDS)){};
                        } else {
                            skipone = false;
                        }
                    }
                }
            } else {
                charindex = 0;
            }
            
        }
        }catch (Exception ex){System.out.println(ex);}

            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
            }
        if (pause){
            System.out.println("stopping import to resume later");
        }

    }   catch (FileNotFoundException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    
    
    public void startworkers(Integer amountofthreads,Boolean mapmode, LinkedHashMap<String, Boolean> fixedmap,File sourcefile){
        Integer rawcounter = 0;
        while (rawcounter < amountofthreads){
            rawcounter++;
            splitters.add(new Rawdataprocesthread(queue,rawdataqueue,entrydelimiter,type));
        }
        for (Rawdataprocesthread splitter : splitters){
            if (!resumelineposition.equals(0L)){
                Rawdataprocesthread.setLineposition(resumelineposition);
            }
            splitter.start();
        }
        
        Integer counter = 0;
        while (counter < amountofthreads){
            counter++;
            workers.add(new ParseWorker(queue,complete,type,customtypes,mapmode,map,fixedmap,sourcefile));
        }
        for (ParseWorker worker : workers){
            worker.start();
        }
        if (!mapmode){
            bulkProcessor = BulkProcessor.builder(
            client,  
            new BulkProcessor.Listener() {
                @Override
                public void beforeBulk(long executionId,
                                       BulkRequest request) {  } 

                @Override
                public void afterBulk(long executionId,
                                      BulkRequest request,
                                      BulkResponse response) {  } 

                @Override
                public void afterBulk(long executionId,
                                      BulkRequest request,
                                      Throwable failure) {  } 
            })
            .setBulkActions(10000) 
            .setBulkSize(new ByteSizeValue(10, ByteSizeUnit.MB)) 
            .setFlushInterval(TimeValue.timeValueSeconds(5)) 
            .setConcurrentRequests(20) 
            .setBackoffPolicy(
            BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3)) 
            .build();
            
            Integer importercounter = 0;
            while (importercounter < (amountofthreads/2)){
                importercounter++;
                importers.add(new ElasticImportThread(complete,indexname,client,this));
                }
        }
        for (ElasticImportThread importer : importers){
            importer.start();
        }  
    }
    
    public void stopworkers(){
        for (ParseWorker worker : workers){
            worker.active = false;
        }
        for (ElasticImportThread importer : importers){
            importer.active = false;
        }  
        for (Rawdataprocesthread splitter : splitters){
            splitter.threadactive = false;
        }
        importers.clear(); 
        workers.clear();
    }
    
    public Long filesize(String filename) throws IOException {
        long size = Files.size(new File(filename).toPath());
        System.out.println(filename+" "+size);
        return size;
    }

    public void WriteObjectToFile(Object serObj, String filepath) {
        try {
            FileOutputStream fileOut = new FileOutputStream(filepath);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(serObj);
            objectOut.close();
            System.out.println("The Map was succesfully written to a file");
 
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public LinkedHashMap ReadMapFromFile(String filepath) {
        try {
            FileInputStream fileIn = new FileInputStream(filepath);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
 
            LinkedHashMap obj = (LinkedHashMap) objectIn.readObject();
            System.out.println("The Object has been read from the file");
            objectIn.close();
            return obj;
 
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public Long ReadResumeLocation(String filepath) {
        try {
            FileInputStream fileIn = new FileInputStream(filepath);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
 
            Long obj = (Long) objectIn.readObject();
            System.out.println("The Object has been read from the file");
            objectIn.close();
            return obj;
 
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public void refresh(Boolean yes){
        if (yes){
            //re-enable refresh
            client.admin().indices().prepareUpdateSettings(indexname)   
            .setSettings(Settings.builder()                     
                    .put("index.refresh_interval", 1)
            ).get();
        } else {
            //disable refresh for import speed
            client.admin().indices().prepareUpdateSettings(indexname)   
            .setSettings(Settings.builder()                     
                    .put("index.refresh_interval", -1)
            ).get();
        }
    }
    /**
     * @return the bulkProcessor
     */
    public synchronized BulkProcessor getBulkProcessor() {
        return bulkProcessor;
    }

    /**
     * @param bulkProcessor the bulkProcessor to set
     */
    public void setBulkProcessor(BulkProcessor bulkProcessor) {
        this.bulkProcessor = bulkProcessor;
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
}
