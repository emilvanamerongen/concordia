/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Annotator;

import FileManager.Filemanager;
import Refdbmanager.header;
import Refdbmanager.refdb;
import concordia.GUIController;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil3
 */
public class AnnotatorThread extends Thread{
    private final int threadnumber;
    private Boolean active = true;
    private ArrayList<refdb> annotationsources = new ArrayList<>();
    private refdb template;
    private HashMap<String, LinkedHashMap<header, LinkedHashSet<String>>> outputheaders = new HashMap<>();
    private ConcurrentHashMap<String, ConcurrentHashMap<String, HashSet<String>>> outputheadersstrings = new ConcurrentHashMap<>();
    private HashMap<Integer, HashMap<String, String>> requestedlinks = new HashMap<>();
    private Set<String> valueset = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private ArrayList<String> dborder = new ArrayList<>();
    private HashMap<String, ArrayList<String>> headerorder = new HashMap<>();
    
    
    public AnnotatorThread(int threadnumber, ArrayList<refdb> annotationsources,refdb template, LinkedHashMap<String, LinkedHashMap<header, LinkedHashSet<String>>> outputheaders,ConcurrentHashMap<String, ConcurrentHashMap<String, HashSet<String>>> outputheadersstrings,HashMap<Integer, HashMap<String, String>> requestedlinks, ArrayList<String> dborder, HashMap<String, ArrayList<String>> headerorder){      
        this.annotationsources = annotationsources;
        this.template = template;
        
        this.outputheaders.putAll(outputheaders);     
        this.requestedlinks = requestedlinks;  
        this.threadnumber = threadnumber;
        this.dborder = dborder;
        this.headerorder = headerorder;
        for (String db : dborder) {  
            this.outputheadersstrings.putIfAbsent(db, new ConcurrentHashMap<>());
            for (String header : headerorder.get(db)) {
                this.outputheadersstrings.get(db).remove(header);
                this.outputheadersstrings.get(db).put(header, new HashSet<>());
            }
        }
    }
    
    public void run(){
        while (active) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
                Logger.getLogger(AnnotatorThread.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (!AnnotationProcess.queue.isEmpty()){
            AnnotationRequest request = AnnotationProcess.queue.poll();
            if (request == null){ 
            } else {
            processrequest(request);
            AnnotationProcess.complete.offer(request);
            }
            }

               
        }
    }
    
    
    
    public void processrequest(AnnotationRequest request){
        String line = request.getQuery();
        Integer requests = 0;
        if (line.contains("|ref|")){
            line = line.replace("|ref|", "\t").replace("gi|", "").replace("|", "");

        }

        StringTokenizer st = new StringTokenizer(line,"\t");

        ArrayList<String> valuetemp = new ArrayList<>();
        while (st.hasMoreTokens()) {

            String value = st.nextToken();
            valuetemp.add(value);
            
        }
        // save template values to outputheaderstrings
        int index = 0;
            for (String header : headerorder.get(template.getDbname())){
                outputheadersstrings.get(template.getDbname()).get(header).add(valuetemp.get(index));
                
                index++;

            }
        Integer tabindex = 0;
        Boolean newdata = true;
        HashSet<String> searches = new HashSet<>();

//            System.out.println("init: "+(System.nanoTime() - startTime));
//            startTime = System.nanoTime();    
        tabindex = 0;
        int run = 0;

        HashSet<Long> log = new HashSet<>();
  
        while (newdata){
//                System.out.println("run:"+run);
            valueset = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
            run++;
            newdata = false;
            tabindex = 0;
            for (Integer color : requestedlinks.keySet()){
                Iterator<String> iter = requestedlinks.get(color).keySet().iterator();
                while(iter.hasNext()){
                    String sourcedb = iter.next();
                valueset.addAll(outputheadersstrings.get(sourcedb).get(requestedlinks.get(color).get(sourcedb)));


                if (!valueset.isEmpty()){
                for (String value : valueset){
                    if (!value.isEmpty()){     
                        for (Searcher refdbsearcher : GUIController.indexstorage.values()){
                               if (!searches.contains(value+"-"+refdbsearcher.getDb().getDbname())){
                                    String searcheddb = refdbsearcher.getDb().getDbname();
                                    AnnotationProcess.readrequests.putIfAbsent(searcheddb, new ConcurrentLinkedQueue<>());
                                    try{
                                    HashMap<String, ArrayList<Long>> resultpositions = refdbsearcher.getgesultpositions(value, color);
                                    for (String sourceheader : resultpositions.keySet()){
                                        for (Long position : resultpositions.get(sourceheader)){
                                            if (!log.contains(position)){
                                            AnnotationProcess.readrequests.get(searcheddb).add(new ReadRequest(position, sourceheader, threadnumber,searcheddb));
                                            requests++;
                                            log.add(position);
                                            } 
                                        }
                                        
                                    }
                                    } catch (Exception ex){
                                        System.out.println("nullpointer");
                                    }
                                    
                                    

                                    searches.add(value+"-"+searcheddb);
                                }
                       }

                    }

                }}

        }}
        while (requests > 0){
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
                Logger.getLogger(AnnotatorThread.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (!AnnotationProcess.completedreadrequests.get(threadnumber).isEmpty()){
                ReadRequest completedrequest = AnnotationProcess.completedreadrequests.get(threadnumber).poll();
                String searcheddb = completedrequest.getSearcheddb();
                
                HashMap<String, LinkedHashSet<String>> headerdata = completedrequest.getResult();
                if (!headerdata.isEmpty()){
                        newdata = true;
                        for (String headerstring : headerdata.keySet()){
                            LinkedHashSet<String> dataset = headerdata.get(headerstring);
                            try{
                                outputheadersstrings.get(searcheddb).get(headerstring).addAll(dataset);
                                
                            } catch (NullPointerException ex){
                                //System.out.println("connection error: "+searcheddb+" - "+headerstring+" - "+dataset);
                            }

                        }
                    }
                requests--;
                    }
            
            }
            
        }
        String result = "";
        
        for (String db : dborder) {
            result+= db+"\t";
            for (String header : headerorder.get(db)) {
                //System.out.println(header);
                try {
                    
                    Set<String> valuedata = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
                    valuedata.addAll(outputheadersstrings.get(db).get(header));
                    boolean first = true;
                    for (String outputvalue : valuedata) {
                        if (first){
                            first = false;
                            result+= outputvalue;
                        } else {
                            result+= ";"+outputvalue;
                        }
                    }
                    result+="\t";
                }catch (NullPointerException ex){}
                outputheadersstrings.get(db).remove(header);
                outputheadersstrings.get(db).put(header, new HashSet<>());
            }
        } 

                

        request.setResult(result);
        request.setDone(true);
        //System.out.println("done--"+threadnumber);
    }


    /**
     * @return the active
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    
}
