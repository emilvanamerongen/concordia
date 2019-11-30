/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ElasticImport;

import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 *
 * @author emil3
 */
public class Rawdataprocesthread extends Thread{

    //private ConcurrentLinkedQueue<ArrayList<String>> queue = new ConcurrentLinkedQueue();
    //public ConcurrentLinkedQueue<String> rawdataqueue = new ConcurrentLinkedQueue();
    public ArrayBlockingQueue<ArrayList<String>> queue = new ArrayBlockingQueue<>(100, true);
    public ArrayBlockingQueue<String> rawdataqueue = new ArrayBlockingQueue<>(100, true);
    private Boolean active = false;
    public Boolean threadactive = true;
    private ArrayList<String> linetemp = new ArrayList<>();
    private String entrydelimiter ;
    private String entryenddelimiter;
    private ReentrantLock lock = new ReentrantLock();
    private boolean GO = false;
    private boolean done = true;
    private static Long lineposition = 0L;
    private String starttest = "";
    private String endtest = "";
    private int starttestsize=0;
    private int endtestsize=0;
    private String type;
    
    Rawdataprocesthread(ArrayBlockingQueue<ArrayList<String>> queue, ArrayBlockingQueue<String> rawdataqueue , String entrydelimiter, String type){
        this.queue = queue;
        this.rawdataqueue = rawdataqueue;
        if (type.equals("uniprot")){
            this.entrydelimiter = entrydelimiter;
            this.entryenddelimiter = entrydelimiter;
        } else if (type.equals("pfam") || type.equals("pfampostionmap")){
            this.entrydelimiter = entrydelimiter;
            this.entryenddelimiter = "//";
        }else{
            this.entrydelimiter = entrydelimiter;
            this.entryenddelimiter = entrydelimiter;
        }
        this.type = type;
        starttestsize = this.entrydelimiter.length();
        endtestsize = this.entryenddelimiter.length();
        setLineposition(0L);
    }
    

    public void run(){
        String poll = "";
        while (threadactive){  
            try {
                poll = rawdataqueue.poll(100L,TimeUnit.SECONDS);
                if (!poll.isEmpty()){
                    process(poll);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Rawdataprocesthread.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
    }
    
    private void process(String text){
        try{
        setLineposition(getLineposition()+text.length()+1);
        ArrayList<String> lines = new ArrayList<>();
        String[] split = text.split("\n");
        Boolean firstline = true;
        for (String line : split){
            if (firstline){
               line = entrydelimiter+line;
               firstline = false;
            } 
            lines.add(line);
        }
        if (type.equals("uniprot")){
            lines.set(0, lines.get(0)+"</entry>");
            lines.remove(lines.size()-2);
            lines.remove(lines.size()-1);
        }
        while (!queue.offer(lines)){
            Thread.sleep(1);
        }
            
        
        }catch (Exception ex){}     
        setDone(true); 
            

    }

    /**
     * @return the GO
     */
    public synchronized boolean isGO() {
        return GO;
    }

    /**
     * @param GO the GO to set
     */
    public synchronized void setGO(boolean GO) {
        this.GO = GO;
    }

    /**
     * @return the done
     */
    public synchronized boolean isDone() {
        return done;
    }

    /**
     * @param done the done to set
     */
    public synchronized void setDone(boolean done) {
        this.done = done;
    }
    
    synchronized public static Long getLineposition() {
        return lineposition;
    }

    /**
     * @param lineposition the lineposition to set
     */
    synchronized public static void setLineposition(Long lineposition) {
        Rawdataprocesthread.lineposition = lineposition;
    }
    
    
}
