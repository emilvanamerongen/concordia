package server;

import ElasticImport.Rawdataprocesthread;
import ElasticImport.Parser;
import Neo4jImport.NeoParser;
import concordia.Task;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author emil
 */
public class ConcordiaServer extends Thread{
    public Boolean active = true;
    public static ServerProperties serverproperties = new ServerProperties();
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String BLUE_BOLD = "\033[1;34m"; 
    public static final String YELLOW_BOLD = "\033[1;33m"; 
    public static final String YELLOW = "\033[0;33m";
    private long startTime = System.nanoTime();
    private Long previousposition = 0L;
    private String minutestring;
    public static ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    private InputListener listener = new InputListener();
    private String neo4juri = "";
    private String neo4juser = "";
    private String neo4jpassword = "";
    
    public void run(){
        System.out.println("server thread started");
        Thread.currentThread().setName("ConcordiaServer");
        //start listener
        listener.start();
        
        //load properties from server.properties file
        serverproperties.loadproperties();
        
        //prepare elasticsearch client
        System.out.println(ANSI_BLUE+"preparing elasticsearch client..");
        
        Settings settings = Settings.builder().put("cluster.name", serverproperties.getElasticCLUSTERNAME()).build();
        TransportClient client = new PreBuiltTransportClient(settings);
        try {
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(serverproperties.getElasticIP()), serverproperties.getElasticPORT()));   
            System.out.println("connected to: "+client.transportAddresses().get(0).getHost());
        } catch (Exception ex){
            System.out.println("ERROR while preparing elasticsearch client: "+ex);
            
        }
        //prepare neo4j client
        neo4juri = serverproperties.getNeo4jURI();
        neo4juser = serverproperties.getNeo4jUSER();
        neo4jpassword = serverproperties.getNeo4jPASSWORD();
        
        Boolean clear = true;
        while (active){
            if (clear){
                System.out.print(ANSI_GREEN +serverproperties.getUsername()+ANSI_RESET+"$ ");
                clear = false;
            }
            try {Thread.sleep(100);} catch (InterruptedException ex) {Logger.getLogger(ConcordiaServer.class.getName()).log(Level.SEVERE, null, ex);}
            if (! queue.isEmpty()){
                String task = queue.poll();
                process(task, client);
                clear = true;
            }
            
            
        }
        System.exit(-1);
        
    }
    
    
    private void process(String task, TransportClient client){
        String input = task;
            if (input.equals("quit")){
                active = false;

            } else if (input.equals("help")){
                System.out.println(BLUE_BOLD +"help"+ANSI_RESET+"\t\t\tThis dialog.");
                System.out.println(BLUE_BOLD +"elasticimport -inputfile -entrydelimiter -type (default: uniprot) -indexname  (default: filename) -num_workers (default: 1) -custom_types (default: none)"+ANSI_RESET+"\tImport a file into the ElasticSearch Database");
                System.out.println(BLUE_BOLD +"neo4jbuild -dofile [-ttdfile] [-num_workers] (default: 1)"+ANSI_RESET+"\tImport a file into a Neo4j Database");
                System.out.println();
                System.out.println(BLUE_BOLD +"startremoteserver"+ANSI_RESET+"\tstart listening for 'concordia-remote' connections");
                System.out.println(BLUE_BOLD +"quit"+ANSI_RESET+"\t\t\tCloses the application.");
            } else if (input.startsWith("elasticimport")){
                // <editor-fold>
                String parameters = input.replace("elasticimport", "").trim();           
                String[] parameterlist = parameters.split(" -"); 
                File inputfile = null;   
                String entrydelimiter = null;
                String type = "uniprot";
                Integer num_workers = 1;
                String custom_types = "";
                String indexname = "";
                
                for (String parameter : parameterlist){
                    try {
                    String paramtype = parameter.split(" ")[0];
                    String param = parameter.substring(parameter.indexOf(" "));
                    if (paramtype.startsWith("-")){
                        paramtype = paramtype.replaceFirst("-","");
                    }
                    param = param.trim();
                    if ("inputfile".equals(paramtype)){
                        System.out.println(param);
                        inputfile = new File(param.replace("\"", "").replace("\'", ""));
                    } else if ("entrydelimiter".equals(paramtype)){
                        entrydelimiter = param;
                    } else if ("indexname".equals(paramtype)){
                        indexname = param;
                    } else if ("type".equals(paramtype)){
                        type = param;     
                    } else if ("num_workers".equals(paramtype)){
                        num_workers = Integer.parseInt(param);
                    } else if ("custom_types".equals(paramtype)){
                        custom_types = param;
                    }
                    } catch (Exception ex){
                        System.out.println("parameter ERROR: "+ex);
                    }
                }
                if (inputfile != null && entrydelimiter != null){
                    Parser parser = new Parser(inputfile,indexname,entrydelimiter,type,num_workers,custom_types,client);
                    System.out.println("Starting parser for: ");
                    System.out.println(BLUE_BOLD +"inputfile: \t"+ANSI_RESET+inputfile.getAbsolutePath());
                    System.out.println(BLUE_BOLD +"indexname: \t"+ANSI_RESET+indexname);
                    System.out.println(BLUE_BOLD +"entrydelimiter: \t"+ANSI_RESET+entrydelimiter);
                    System.out.println(BLUE_BOLD +"type: \t"+ANSI_RESET+type);
                    System.out.println(BLUE_BOLD +"num_workers: \t"+ANSI_RESET+num_workers);
                    System.out.println(BLUE_BOLD +"custom_types: \t"+ANSI_RESET+custom_types);
                    parser.start();
                while (parser.isAlive()){
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ConcordiaServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Long position = Rawdataprocesthread.getLineposition();
                    Long filesize = parser.getFilesize();
                    double progress = position.doubleValue()/filesize.doubleValue();
                    Long estimatedTime = System.nanoTime() - startTime;
                    double estimatedTimedouble = estimatedTime;
                    Long bytesincycle = position-previousposition; 
                    Long bytestodo = filesize-position;
                    double timeremaining = (estimatedTimedouble/bytesincycle.doubleValue())*bytestodo.doubleValue();
                    Double minutes = timeremaining / 60000000000.0;
                    minutestring = minutes.toString();
                        try{
                    minutestring = minutestring.substring(0,minutestring.indexOf("."));
                    } catch (Exception ex){}
                    minutestring = minutestring+" minutes remaining..";
                    minutestring = minutestring+"\trawdataqueue size: "+parser.rawdataqueue.size()+"\tqueue size: "+parser.queue.size();
                    startTime = System.nanoTime();
                    previousposition = position;

                    System.out.print("\r                                                                                                                   ");
                    System.out.print("\r"+minutestring);

                }
                
                
                } else {
                    System.out.println(BLUE_BOLD +"elasticimport -inputfile -entrydelimiter -type (default: uniprot) -indexname  (default: filename) -num_workers (default: 1) -custom_types (default: none)"+ANSI_RESET+"\tImport a file into the ElasticSearch Database");
                }   
                // </editor-fold>
            } else if (input.startsWith("neo4jbuild --help") || input.startsWith("neo4jbuild --help")){
                System.out.println(BLUE_BOLD +"neo4jbuild parameters:"+ANSI_RESET);
                System.out.println(BLUE_BOLD +"-dofile"+ANSI_RESET+"\t\tpath to disease ontology obo file ");
                System.out.println(BLUE_BOLD +"-ttdfile"+ANSI_RESET+"\tpath to Theraputic Target Database 'Drug to disease mapping with ICD identifiers' file ");
                System.out.println(BLUE_BOLD +"-num_workers"+ANSI_RESET+"\tnumber of workers to use for parsing (use a number similar to the number of available threads on your computer for the best results)");
            }
            else if (input.startsWith("neo4jbuild")){
                // <editor-fold>
                String parameters = input.replace("neo4jbuild", "").trim();           
                String[] parameterlist = parameters.split(" -"); 
                File inputfile = null;   
                File ttdfile = null;
                Integer num_workers = 1;
                
                for (String parameter : parameterlist){
                    try {
                    String paramtype = parameter.split(" ")[0];
                    String param = parameter.substring(parameter.indexOf(" "));
                    if (paramtype.startsWith("-")){
                        paramtype = paramtype.replaceFirst("-","");
                    }
                    param = param.trim();
                    if ("dofile".equals(paramtype)){
                        System.out.println(param);
                        inputfile = new File(param.replace("\"", "").replace("\'", ""));     
                    if ("ttdfile".equals(paramtype)){
                        System.out.println(param);
                        ttdfile = new File(param.replace("\"", "").replace("\'", ""));     
                    }
                    } else if ("num_workers".equals(paramtype)){
                        num_workers = Integer.parseInt(param);
                    }
                    } catch (Exception ex){
                        System.out.println("parameter ERROR: "+ex);
                    }
                }
                if (inputfile != null){
                    NeoParser parser = new NeoParser(inputfile,ttdfile,num_workers,neo4juri,neo4juser,neo4jpassword);
                    System.out.println("Starting parser for: ");
                    System.out.println(BLUE_BOLD +"inputfile: \t"+ANSI_RESET+inputfile.getAbsolutePath());
                    try {System.out.println(BLUE_BOLD +"ttdfile: \t"+ANSI_RESET+ttdfile.getAbsolutePath());}catch (Exception ex){}
                    System.out.println(BLUE_BOLD +"num_workers: \t"+ANSI_RESET+num_workers);
                    parser.start();
                while (parser.isAlive()){
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ConcordiaServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Long position = Rawdataprocesthread.getLineposition();
                    Long filesize = parser.getFilesize();
                    double progress = position.doubleValue()/filesize.doubleValue();
                    Long estimatedTime = System.nanoTime() - startTime;
                    double estimatedTimedouble = estimatedTime;
                    Long bytesincycle = position-previousposition; 
                    Long bytestodo = filesize-position;
                    double timeremaining = (estimatedTimedouble/bytesincycle.doubleValue())*bytestodo.doubleValue();
                    Double minutes = timeremaining / 60000000000.0;
                    minutestring = minutes.toString();
                        try{
                    minutestring = minutestring.substring(0,minutestring.indexOf("."));
                    } catch (Exception ex){}
                    minutestring = minutestring+" minutes remaining..";
                    minutestring = minutestring+"\trawdataqueue size: "+parser.rawdataprocessor.rawdataqueue.size()+"\tqueue size: "+parser.queue.size()+"relationqueue size: "+parser.relationqueue.size();
                            
                    startTime = System.nanoTime();
                    previousposition = position;

                    System.out.print("\r                                                                                                                   ");
                    System.out.print("\r"+minutestring);

                }
                
                
                } else {
                    System.out.println("invalid parameters!");
                    System.out.println("use:neo");
                    System.out.println(BLUE_BOLD +"neo4jbuild -dofile [-ttdfile] [-num_workers] (default: 1)"+ANSI_RESET+"\tImport a file into a Neo4j Database");
                }   
                // </editor-fold>
            } else if (input.startsWith("startremoteserver")){
                startremoteserver(client);
            }

            if (input.startsWith("importstatus")){
                //System.out.println("completed parses: "+Parser.isDone());
                for (String key : Parser.map.keySet()){
                    System.out.println(key+"\t"+Parser.map.get(key));
                }
            }
    }
    
    
    public void startremoteserver(TransportClient client){
        //start socket listener  
        System.out.println(ANSI_RESET+"-------------------------");
        ListenSocket serverlistener;
        try {
            serverlistener = new ListenSocket(serverproperties.getSocketport(),serverproperties.getNum_requestworkers(),client);
            serverlistener.start();
        } catch (IOException ex) {
            Logger.getLogger(ConcordiaServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println();
    }

    
}