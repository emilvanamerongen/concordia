/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Neo4jImport;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author emil3
 */
public class NeoRawdataprocesthread extends Thread{

    private ConcurrentLinkedQueue<ArrayList<String>> queue = new ConcurrentLinkedQueue();
    public ConcurrentLinkedQueue<String> rawdataqueue = new ConcurrentLinkedQueue();
    private Boolean active = false;
    private Boolean threadactive = true;
    private ArrayList<String> linetemp = new ArrayList<>();
    private String entrydelimiter ;
    private String entryenddelimiter;
    private ReentrantLock lock = new ReentrantLock();
    private boolean GO = false;
    private boolean done = false;
    private static Long lineposition = 0L;
    private String starttest = "";
    private String endtest = "";
    private int starttestsize=0;
    private int endtestsize=0;
    
    NeoRawdataprocesthread(ConcurrentLinkedQueue<ArrayList<String>> queue, String type){
        this.queue = queue;
        if (type.equals("DO")){ //-----!!!
            this.entrydelimiter = "[Term]";
            this.entryenddelimiter = "[Term]";
        } else if (type.equals("pfam") || type.equals("pfampostionmap")){
            this.entrydelimiter = entrydelimiter;
            this.entryenddelimiter = "//";
        }else{
            System.out.println("no type found!");
        }
        starttestsize = this.entrydelimiter.length();
        endtestsize = this.entryenddelimiter.length();
        setLineposition(0L);
    }
    

    public void run(){
        while (getThreadactive()){
            if (!rawdataqueue.isEmpty()){       
                process(rawdataqueue.poll());
            }
        } 
    }
    
    private void process(String line){
        Thread.currentThread().setName("neorawdataprocessthread");
        try{
        setLineposition(getLineposition()+line.length()+1);
           
        if (active){
            if (line.length()>=endtestsize){
            endtest = line.substring(0,endtestsize);
            
            if (endtest.equals(entryenddelimiter)){
                while (queue.size()>1000){Thread.sleep(10);}
                queue.add(linetemp);
                linetemp = new ArrayList<>();
                linetemp.add(line);
                
            } else {
                linetemp.add(line);
            }} else {
                linetemp.add(line);
            }
        } else {
            if (line.length()>=endtestsize){
            starttest = line.substring(0,starttestsize);
            if (starttest.equals(entrydelimiter)){
                active = true;
                linetemp.add(line);
                
            } 
        }
        }

        }catch (Exception ex){System.out.println(ex);}     
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
        NeoRawdataprocesthread.lineposition = lineposition;
    }

    /**
     * @return the threadactive
     */
    public Boolean getThreadactive() {
        return threadactive;
    }

    /**
     * @param threadactive the threadactive to set
     */
    public synchronized void setThreadactive(Boolean threadactive) {
        this.threadactive = threadactive;
    }
    
}
