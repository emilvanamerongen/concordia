/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONObject;

/**
 *
 * @author emil3
 */
public class RequestWorker extends Thread{
    private Boolean active = true;
    private ConcurrentLinkedQueue<ElasticRequest> definitionqueue = new ConcurrentLinkedQueue();
    private ConcurrentLinkedQueue<ElasticRequest> requestqueue = new ConcurrentLinkedQueue();
    private HashMap<String,ArrayList<HashMap<String,String[]>>> instructionsets = new HashMap<>();
    private File instructionsetfolder = new File("instructionsets"+File.separator);
    private RestHighLevelClient client;
    private JSONObject jsonoutput = new JSONObject();
    
    public RequestWorker(ConcurrentLinkedQueue<ElasticRequest> requestqueue, RestHighLevelClient client){
        this.requestqueue = requestqueue;
        this.client = client;
    }
    
    public void run(){
        File serverproperties = new File("server.properties");
        Thread.currentThread().setName("requestworker "+Thread.activeCount());
        
        List<String> lines;
        try {
            lines = Files.readAllLines(serverproperties.toPath());
            for (String line : lines){
                if (line.startsWith("instructionsetfolder=")){
                    String instructionsetfolderlocation = line.split("=")[1];
                    instructionsetfolder = new File(instructionsetfolderlocation);
                    if (!instructionsetfolder.getName().equals("") && !instructionsetfolder.exists()){
                        instructionsetfolder.mkdir();
                    }
                }
            } 
        } catch (Exception ex) {
            System.out.println("server properties error");
            Logger.getLogger(RequestWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        loadinstructions();
        while (getActive()){
            if (!requestqueue.isEmpty()){
                ElasticRequest request = requestqueue.poll();
            if (!(request == null)){ 
                processrequest(request);
            }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(ListenSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
         
    private void loadinstructions(){
        File[] instructionsetfiles = instructionsetfolder.listFiles();
        for (File instructionsetfile : instructionsetfiles){
            try {
                String instructionsetfilename = instructionsetfile.getName();
                instructionsets.put(instructionsetfilename, new ArrayList<>());
                for (String line : Files.readAllLines(Paths.get(instructionsetfile.getAbsolutePath()))){
                    String[] splitline = line.split("\t");
                    instructionsets.get(instructionsetfilename).add(new HashMap<>());
                    int lastindex = instructionsets.get(instructionsetfilename).size()-1; 
                    for (String instruction : splitline){
                        String[] instructionsplit = instruction.split(":");
                        if (instructionsplit.length > 1){
                            instructionsets.get(instructionsetfilename).get(lastindex).put(instructionsplit[0], instructionsplit[1].split(","));
                        }
                    }
                    
                }
            } catch (IOException ex) {
                Logger.getLogger(RequestWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void processrequest(ElasticRequest request){       
        jsonoutput = new JSONObject();
        ArrayList<String> columns = new ArrayList<>();
        HashSet<String> whitelist = new HashSet<>();
        String querystring = request.getQuerystring();
        
        if (request.getQuery().get("drug").size() >= 2){
            
            if (querystring.contains("")){
                
            }
        } 

        else if (request.getQuery().get("drug").size() == 1){
            //drug side-effects
            //System.out.println("DRUG");
            //System.out.println(querystring);
            if ((querystring.contains("side") && querystring.contains("effect")) || querystring.contains("reaction") || querystring.contains("aftereffect")){
                //System.out.println("sideeffect");
                columns.clear();
                columns.add("ArticleTitle");
                columns.add("NameOfSubstance");
                whitelist.clear();
                whitelist.add("htox_signs_and_symptoms");
                whitelist.add("htox");
//                jsonoutput = search(jsonoutput, "hsdb", request.getQuery().get("drug"),columns,whitelist);
                
            } else if (querystring.contains("overdose")){
                System.out.println("OD");
                columns.clear();
                columns.add("ArticleTitle");
                columns.add("NameOfSubstance");
                whitelist.clear();
                whitelist.add("actn");
                //whitelist.add("htox_signs_and_symptoms");
                //whitelist.add("htox");
//                jsonoutput = search(jsonoutput, "hsdb", request.getQuery().get("drug"),columns,whitelist);
                
              
            }
        }
        
        
        
        System.out.println(jsonoutput.toString());
        
        try (FileWriter file = new FileWriter(request.getOutputfile())) {
            file.write(jsonoutput.toString(2));
            file.close();
        } catch (IOException ex) {
            Logger.getLogger(RequestWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    

//    private JSONObject search(JSONObject jsonoutput, String db, ArrayList<String> values, ArrayList<String> columns, HashSet<String> whitelist){
//        String[] columnsarr = new String[columns.size()];
//        columnsarr = columns.toArray(columnsarr);
//        String valuesstring = values.toString().replace("[","").replace("]", "").replace(",", "");
//        SearchRequestBuilder prepareSearch = client.prepareSearch(db);  
//        prepareSearch.setQuery(QueryBuilders.multiMatchQuery(valuesstring,columnsarr));
//        SearchResponse response = prepareSearch.get();  
//        //System.out.println("searching: values:"+valuesstring+"columns: "+columnsarr.toString());
//        if (response!=null){
//            
//                if (response.getHits().totalHits > 0){  
//                    
//                    Map<String, Object> firsthit = response.getHits().getAt(0).getSource();
//                    for (String field : firsthit.keySet()){
//                        try{
//                            System.out.println("found something");
//                        if (whitelist.isEmpty() || whitelist.contains(field)){
//                        if (!firsthit.get(field).toString().equals("null")){
//                           ArrayList<String> data =   (ArrayList<String>) firsthit.get(field);
//                           ArrayList<String> datalist = new ArrayList<>();
//                           for (String dataitem : data){
//                               datalist.add(dataitem.replace("&amp;", ""));
//                           }
//                           jsonoutput.put(field, datalist);
//                           
//                        }
//                        }
//                        }catch (Exception ex){System.out.println(ex);}
//                    }
//                }
//        } 
//        return jsonoutput;
//    }
    
    
    
  
    
    
    
    
    
    
//    
//    private void processrequest(ElasticRequest request){
//        try{         
//            Integer attempt = 0;
//            Object[] values = request.getQuery().values().toArray();
//            String value = (String) values[0];
//            
//            ArrayList<HashMap<String, String[]>> instructionset = instructionsets.get(value);
//
//            for (HashMap<String, String[]> instruction : instructionset){
//                attempt++;
//                try{
//                String[] searchindices = instruction.get("index");
//                SearchRequestBuilder prepareSearch = client.prepareSearch("hsdb");
//                String querytype = "multiMatchQuery";
//                //build query
//                if (instruction.containsKey("querytype")){
//                    querytype = instruction.get("querytype")[0];
//                }
//                if (instruction.containsKey("type")){
//                    prepareSearch.setTypes(instruction.get("type"));
//                }
//                if (instruction.containsKey("searchtype")){
//                    prepareSearch.setSearchType(instruction.get("searchtype")[0]);
//                }
//                if (instruction.containsKey("from")){
//                    prepareSearch.setFrom(Integer.parseInt(instruction.get("from")[0]));
//                }
//                if (instruction.containsKey("size")){
//                    prepareSearch.setSize(Integer.parseInt(instruction.get("size")[0]));
//                }
//                if (instruction.containsKey("explain")){
//                    if (instruction.get("explain")[0].toLowerCase().contains("true")){
//                        prepareSearch.setExplain(true);
//                    }
//                } 
//                
//                SearchResponse response = null;
//                System.out.println("search "+request.getQuery().keySet().toArray()[0]+"\t"+instruction.get("fields")[0]);
//                if (querytype.equals("multiMatchQuery")){
//                    prepareSearch.setQuery(QueryBuilders.termsQuery(instruction.get("fields")[0],request.getQuery().keySet().toArray()[0]));
//                    response = prepareSearch.get();
//                } else if (querytype.equals("REGEX")){
//                    //TODO
//                }  
//                if (response!=null){
//                if (response.getHits().totalHits > 0){
//                    JSONObject jsonoutput = new JSONObject();
//                    Map<String, Object> firsthit = response.getHits().getAt(0).getSource();
//                    for (String field : firsthit.keySet()){
//                        try{
//                        if (!firsthit.get(field).toString().equals("null")){
//                           jsonoutput.put(field, firsthit.get(field).toString());
//                        }
//                        }catch (Exception ex){}
//                    }
//                    
//                    try (FileWriter file = new FileWriter(request.getOutputfile())) {
//                    file.write(jsonoutput.toString(2));
//                    file.close();
//                    }
//                    
//                    System.out.println("search attempt "+attempt+" succesfull");    
//                    request.getOut().println("search attempt "+attempt+" succesfull");
//                    request.getOut().flush();
//                    break;
//                } 
//                }
//                } catch (Exception ex){
//                     Logger.getLogger(RequestWorker.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            System.out.println("search attempts "+attempt);
//            
//        } catch (Exception ex){
//             Logger.getLogger(RequestWorker.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

    /**
     * @return the active
     */
    public synchronized Boolean getActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public synchronized void setActive(Boolean active) {
        this.active = active;
    }
    
}
