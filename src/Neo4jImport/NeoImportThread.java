/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Neo4jImport;

import ElasticImport.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import static org.neo4j.driver.v1.Values.parameters;

/**
 *
 * @author emil3
 */
public class NeoImportThread extends Thread{
    private ConcurrentLinkedQueue<NeoImportRequest> complete = new ConcurrentLinkedQueue();
    private ConcurrentLinkedQueue<ArrayList<String>> relationqueue = new ConcurrentLinkedQueue();
    public Boolean active = true;
    public Boolean stage1 = true;
    public FileWriter outputstream = null;
    private Integer elasticport = 9200;
    private String indexname;
    private Integer id = 0;
    private Driver driver;
    private HashSet<Integer> done = new HashSet<>();
    private HashSet<String> symptomsdone = new HashSet<>();
    private HashSet<String> pubmeddone = new HashSet<>();
    private HashSet<String> locationsdone = new HashSet<>();
    private String neo4juri = "";
    private String neo4juser = "";
    private String neo4jpassword = "";
    
    
    public NeoImportThread(ConcurrentLinkedQueue<NeoImportRequest> complete,ConcurrentLinkedQueue<ArrayList<String>> relationqueue, String indexname, String neo4juri, String neo4juser, String neo4jpassword){
        this.complete = complete;
        this.indexname = indexname;
        this.relationqueue = relationqueue;
        this.neo4juri = neo4juri;
        this.neo4juser = neo4juser;
        this.neo4jpassword = neo4jpassword;
        
        try { 
            driver = GraphDatabase.driver(neo4juri, AuthTokens.basic( neo4juser, neo4jpassword ) );
            System.out.println("connected");
        } catch (Exception ex){
            System.out.println("unable to connect");
            active = false;
            driver = null;
            NeoParser.done = true;         
        }
    }


    
    public void run(){
        Thread.currentThread().setName("neoimportthread");
        //create nodes from complete queue  
        while (stage1){
            if (!complete.isEmpty()){
                NeoImportRequest request = complete.poll();

                if (request != null){
                    importdata(request);

                }} else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(NeoImportThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
                }
        //create relationships from relationqueue
        System.out.println("creating relationships");
        while (active){
            if (!relationqueue.isEmpty()){
                ArrayList<String> request = relationqueue.poll();

                if (request != null){
                    
                    try ( Session session3 = driver.session() ){
                        StatementResult writeTransaction = session3.writeTransaction( tx -> makeRelation( tx, request.get(0),request.get(1),request.get(2),request.get(3),request.get(4),request.get(5), request.get(6) ) );
                        //System.out.println(writeTransaction.consume().toString());
                    } catch (Exception ex){
                        System.out.println(ex);
                    }
                        
                }
            }
            
        }
        
    }

    
    
    private void importdata(NeoImportRequest request) {
        LinkedHashMap<String, ArrayList<String>> data = request.getData();
        String nodetype = request.getNodetype();
        Boolean go = true;
        
        if (nodetype.equals("DOcategory") || nodetype.equals("Disease")){
            String doid = data.get("id").get(0).substring(5);
            Integer idint = Integer.parseInt(doid);
            if (done.contains(idint)){
                go = false;
            } else {
                done.add(idint);
            }
            
        } else if (nodetype.equals("Pubmed")){
            String pmid = data.get("id").get(0);
            if (pubmeddone.contains(pmid)){
                go = false;
            } else {
                pubmeddone.add(pmid);
            }
        } else if (nodetype.equals("Symptom")){
            String symptom = data.get("name").get(0);
            if (symptomsdone.contains(symptom)){
                go = false;
            } else {
                symptomsdone.add(symptom);
            }
        } else if (nodetype.equals("Location")){
            String symptom = data.get("name").get(0);
            if (locationsdone.contains(symptom)){
                go = false;
            } else {
                locationsdone.add(symptom);
            }
        } 
        if (go){
            Map<String, Object> parameters = new HashMap<>();
            for (String key : data.keySet()){
                if (data.get(key).size() > 1){
                    //String[] array = request.get(key).toArray(new String[0]);
                    parameters.put(key, data.get(key));
                } else {
                    parameters.put(key, data.get(key).get(0));
                }
            } 
            buildnode(parameters, nodetype);
        }
    }
    

    
    public void printGreeting( final String message )
    {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("message", "hi");
        
        try ( Session session = driver.session() )
        {
            String greeting = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    StatementResult result = tx.run( "CREATE (a:Greeting) " +
                                                     "SET a.message = $message " +
                                                     "RETURN a.message + ', from node ' + id(a)", parameters);
                    return result.single().get( 0 ).asString();
                }
            } );
            System.out.println( greeting );
        }
    }
    
    public void buildnode(Map<String, Object> parameters, String nodetype){
        //generate setterstring
        String setterstring = "SET ";
        Boolean first = true;
        for (String parameter : parameters.keySet()){
            if (!first){
                setterstring += ", ";
            } else {
                first = false;
            }
            setterstring += "a."+parameter+" = $"+parameter ;
            
        }
        setterstring+="\n";
        createnode(setterstring,parameters,nodetype);
    }
    
    
    public void createnode(final String setterstring, Map<String, Object> parameters, String nodetype){  
        try ( Session session = driver.session() )
        {
            String nodecreation = session.writeTransaction( new TransactionWork<String>()
            
            {
                @Override
                public String execute( Transaction tx )
                {
                    StatementResult result = tx.run( "CREATE  (a:"+nodetype+") " +
                                                     setterstring 
                                                     , parameters);
                    return "done";
                }
            } );
            
        }
    }
    private StatementResult makeRelation(final Transaction tx, String node1type, String node1paramtype, final String node1, String relationtype, String node2type, String node2paramtype, final String node2) {
             return tx.run( "MATCH (a:"+node1type+" {"+node1paramtype+": $"+node1type+"_1}) " +
                    "MATCH (b:"+node2type+" {"+node2paramtype+": $"+node2type+"_2}) " +
                    "MERGE (a)-[:"+relationtype+"]->(b)",
            parameters( node1type+"_1", node1, node2type+"_2", node2 ) );
}
    
}
