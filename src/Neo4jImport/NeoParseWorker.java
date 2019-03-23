/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Neo4jImport;

import ElasticImport.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashSet;


/**
 *
 * @author emil
 */
public class NeoParseWorker extends Thread{
    public ConcurrentLinkedQueue<ArrayList<String>> queue = new ConcurrentLinkedQueue();
    public ConcurrentLinkedQueue<NeoImportRequest> complete = new ConcurrentLinkedQueue();
    public ConcurrentLinkedQueue<ArrayList<String>> relationqueue = new ConcurrentLinkedQueue();
    public Boolean active = true;
    public Boolean mapmode = false;
    private String type = "universal";
    private String subtype;
    private String sourcefile;
    

    NeoParseWorker(ConcurrentLinkedQueue<ArrayList<String>> queue, ConcurrentLinkedQueue<NeoImportRequest> complete, ConcurrentLinkedQueue<ArrayList<String>> relationqueue, Boolean mapmode, File sourcefile,String type) {
        this.queue = queue;
        this.complete = complete;
        this.relationqueue = relationqueue;
        this.mapmode = mapmode;
        this.type = type.trim();
        this.sourcefile = sourcefile.getAbsolutePath();
    }
    
    @Override
    public void run(){
        Thread.currentThread().setName("neoparseworker");
        int counter = 0;
        while (active){
            if (!queue.isEmpty() && complete.size() < 2000){
                
            ArrayList<String> request = queue.poll();
                
            if (request == null){ 
            } else {  
                
                processrequest(request);  
                counter ++;
            }
            } else {
                try {Thread.sleep(10);} catch (InterruptedException ex) {}
            }     
            
        }

    }
    
    private void processrequest(ArrayList<String> request){
        if (null != type) switch (type) {
            case "DO":
                doparse(request);
            case "uniprot":
                break;
            case "pfam":
                break;
            case "pfampositionmap":
                break;
            case "hsdb":
                break;
            default:
                break;
        }
        
    }
    

    private String valuestringtrim(String valuestring){
        valuestring = valuestring.substring(valuestring.indexOf("val=")+4);
        valuestring = valuestring.replace("}", ",");
        valuestring = valuestring.substring(0,valuestring.indexOf(","));
    
        return valuestring;
    }
    
    private void doparse(ArrayList<String> request){
        LinkedHashMap<String,ArrayList<String>> node = new LinkedHashMap<>();
        for (String line : request){
            if (!(line.equals("[term]") || line.equals("")) &&  line.contains(":")){
                String[] linesplit = line.split(":",2);
                String key = linesplit[0];
                String value = linesplit[1].replace("[", "").replace("]", "").replace("\"", "").replace("\\", "").replace("\n", "").trim();
                node.putIfAbsent(key, new ArrayList<>());
                node.get(key).add(value);
            }
        }
        //filter out obsolete nodes
        if (!node.containsKey("is_obsolete")){  
        String nodetype = "Disease";
        if (NeoParser.checklinkto(node.get("id").get(0))){
            nodetype = "DOcategory";
        }
        
        if (node.containsKey("is_a")){
            for (String value : node.get("is_a")){
                try{
                ArrayList<String> relationshipitem = new  ArrayList<>();
                relationshipitem.add(nodetype);
                relationshipitem.add("id");
                relationshipitem.add(node.get("id").get(0));
                relationshipitem.add("is_a");
                relationshipitem.add("DOcategory");
                relationshipitem.add("id");
                String targetid = value.split("!")[0].trim();
                relationshipitem.add(targetid);
                relationqueue.add(relationshipitem);
                NeoParser.putlinkto(targetid);
                } catch (Exception ex){
                    System.out.println("exception when creating relationship item: "+ex);
                }
            } 
        } 
        //subsets as nodes
//        if (node.containsKey("subset")){
//            for (String value : node.get("subset")){
//                try{
//                ArrayList<String> relationshipitem2 = new  ArrayList<>();
//                relationshipitem2.add("subset");
//                relationshipitem2.add("name");
//                String targetname = value;
//                relationshipitem2.add(targetname);
//                relationshipitem2.add("part_of_subset");
//                relationshipitem2.add(nodetype);
//                relationshipitem2.add("id");
//                relationshipitem2.add(node.get("id").get(0));
//                relationqueue.add(relationshipitem2);
//                if (!NeoParser.checksubsets(targetname)){
//                    LinkedHashMap<String,ArrayList<String>> subsetproperties = new LinkedHashMap<>();
//                    subsetproperties.put("name", new ArrayList<>());
//                    subsetproperties.get("name").add(targetname);
//                    complete.add(new NeoImportRequest("subset",subsetproperties));
//                    NeoParser.putsubset(targetname);
//                } 
//                
//                } catch (Exception ex){
//                    System.out.println("exception when creating relationship item: "+ex);
//                }
//            } 
//        }

        if (node.containsKey("def")){
            HashMap<String, String> defitems = new HashMap<>();
            ArrayList<String> pmids = new ArrayList<>();
            String defstring = node.get("def").get(0);
            String[] wordsplit = defstring.split(" ");
            String activetype = "none";
            for (String word : wordsplit){
                if (!activetype.equals("none") && !word.contains("_") && !word.startsWith("url")){
                    word = word.replace(",", "").replace(".","").replace("\"", "");
                    
                    if (!defitems.containsKey(activetype)){
                        defitems.put(activetype, word);
                    } else{
                        defitems.put(activetype, defitems.get(activetype)+" "+word);
                    }
                }
                if (word.contains("_symptom")){
                    activetype = "Symptom";
                } else if (word.contains("pubmed/?term=")){
                    String pmid = word.substring(word.indexOf("pubmed/?term=")+13).replace(",", "").replace("]", "");
                    pmids.add(pmid);
                    activetype = "none";
                } else if (word.contains("located_")){
                    activetype = "Location";
                } else if (word.contains("_") || word.endsWith(".")){
                    activetype = "none";
                }
            }
            
            for (String pmid : pmids){
                ArrayList<String> pmidrelation = new  ArrayList<>();
                pmidrelation.add("Pubmed");
                pmidrelation.add("id");
                String targetname = pmid;
                pmidrelation.add(targetname);
                pmidrelation.add("article_reference");
                pmidrelation.add(nodetype);
                pmidrelation.add("id");
                pmidrelation.add(node.get("id").get(0));
                relationqueue.add(pmidrelation);
                LinkedHashMap<String,ArrayList<String>> subsetproperties = new LinkedHashMap<>();
                subsetproperties.put("id", new ArrayList<>());
                subsetproperties.get("id").add(pmid);
                complete.add(new NeoImportRequest("Pubmed",subsetproperties));
            }
            //symptoms
            for (String symptom : NeoParser.getSymptomlist().keySet()){
                if (defitems.containsKey("Symptom") && defitems.get("Symptom").contains(symptom)){
                    ArrayList<String> relationshipitem2 = new  ArrayList<>();
                    String targetname = NeoParser.getSymptomlist().get(symptom);
                    //from node
                    relationshipitem2.add("symptom");
                    relationshipitem2.add("name");
                    relationshipitem2.add(targetname);
                    
                    //relationship type
                    relationshipitem2.add("symptom_of");
                    
                    //to node
                    relationshipitem2.add(nodetype);
                    relationshipitem2.add("id");
                    relationshipitem2.add(node.get("id").get(0));
                    relationqueue.add(relationshipitem2);
                } else if (defstring.contains(symptom)){
                    ArrayList<String> relationshipitem2 = new  ArrayList<>();
                    String targetname = NeoParser.getSymptomlist().get(symptom);
                    //from node
                    relationshipitem2.add("symptom");
                    relationshipitem2.add("name");
                    relationshipitem2.add(targetname);
                    
                    //relationship type
                    relationshipitem2.add("symptom_in_def");
                    
                    //to node
                    relationshipitem2.add(nodetype);
                    relationshipitem2.add("id");
                    relationshipitem2.add(node.get("id").get(0));
                    
                    relationqueue.add(relationshipitem2);
                }
            }
           //locations
            for (String location : NeoParser.getLocationslist().keySet()){
                if (defitems.containsKey("Location") && defitems.get("Location").contains(location)){
                    ArrayList<String> relationshipitem2 = new  ArrayList<>();
                    String targetname = NeoParser.getLocationslist().get(location);
                    //from node
                    relationshipitem2.add("location");
                    relationshipitem2.add("name");
                    relationshipitem2.add(targetname);
                    
                    //relationship type
                    relationshipitem2.add("location_of");
                    
                    //to node
                    relationshipitem2.add(nodetype);
                    relationshipitem2.add("id");
                    relationshipitem2.add(node.get("id").get(0));
                    relationqueue.add(relationshipitem2);
                } else if (defstring.contains(location)){
                    ArrayList<String> relationshipitem2 = new  ArrayList<>();
                    String targetname = NeoParser.getLocationslist().get(location);
                    //from node
                    relationshipitem2.add("location");
                    relationshipitem2.add("name");
                    relationshipitem2.add(targetname);
                    
                    //relationship type
                    relationshipitem2.add("location_in_def");
                    
                    //to node
                    relationshipitem2.add(nodetype);
                    relationshipitem2.add("id");
                    relationshipitem2.add(node.get("id").get(0));
                    
                    relationqueue.add(relationshipitem2);
                }
            }
           
        }
            complete.add(new NeoImportRequest(nodetype,node));
        }
        
        
//        //test
//        for (String key : node.keySet()){
//            System.out.println(key);
//            try{
//            for (String value : node.get(key)){
//                System.out.println("\t"+value);
//            }
//            } catch (Exception ex){
//                System.out.println("\tnone");
//            }
//        }
//        System.out.println("-------------------");
    }
    

    
    public void addtomap(String key, String value, LinkedHashMap<String, ArrayList<String>> parseresult){
        key = key.trim().replace("\"", "").replace("[", "").replace("]","");
        if (key.substring(key.length()-1).equals("_")){
            key = key.substring(0,key.length()-1);
        }
        
        if (mapmode){
            NeoParser.map.putIfAbsent(key, false);
//            if (!NeoParser.map.get(key)){
//                String valuetrim = value.trim();
//                
//                if (value.length() > 500 && spaceCount(value)>4){
//                    
//                        NeoParser.map.put(key, true);
//                    
//                }
//            }
        } else {
            value = value.replace("[", "").replace("]", "").replace("\'", "").replace("\"","");
            parseresult.get(key).add(value);
        }
    }
    
    public static int spaceCount(String s){ int a=0;
    char ch[]= new char[s.length()];
    for(int i = 0; i < s.length(); i++) 

    {  ch[i]= s.charAt(i);
        if( ch[i]==' ' )
        a++;
            }   
    return a;
}
}
