/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ElasticImport;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.json.XML;

/**
 *
 * @author emil
 */
public class ParseWorker extends Thread{
    public ArrayBlockingQueue<ArrayList<String>> queue = new ArrayBlockingQueue<>(100, true);
    public ArrayBlockingQueue<LinkedHashMap<String, ArrayList<String>>> complete = new ArrayBlockingQueue<>(100, true);
    public ArrayList<String> customtypes = new ArrayList<>();
    public Boolean active = true;
    public Boolean mapmode = false;
    private String type = "universal";
    private LinkedHashMap<String, ArrayList<String>> fixedtemplate = new LinkedHashMap<>();
    private HashMap<Integer, String> tabtemplate = new HashMap<>();
    private String subtype;
    private String sourcefile;
    
    public ParseWorker(ArrayBlockingQueue<ArrayList<String>> queue,ArrayBlockingQueue<LinkedHashMap<String, ArrayList<String>>> complete, String type, ArrayList<String> customtypes, Boolean mapmode, ConcurrentHashMap<String,Boolean> template, LinkedHashMap<String,Boolean> fixedtemplate,File sourcefile){
        this.queue = queue;
        this.complete = complete;
        this.customtypes = customtypes;
        this.mapmode = mapmode;
        this.type = type;
        for (String mapitem : fixedtemplate.keySet()){
            this.fixedtemplate.put(mapitem, new ArrayList<>());
        }
        this.sourcefile = sourcefile.getAbsolutePath();
    }
    
    public void run(){
        type = type.trim();
        ArrayList<String> poll = new ArrayList<>();
        while (active){
             try {
                poll = queue.poll(100L,TimeUnit.SECONDS);
                if (!poll.isEmpty()){
                    processrequest(poll);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Rawdataprocesthread.class.getName()).log(Level.SEVERE, null, ex);
            }     
        }
    }
    
    private void processrequest(ArrayList<String> request){
        if ("universal".equals(type)){
            universalparse(request);
        } else if ("uniprot".equals(type)){
            LinkedHashMap<String, ArrayList<String>> parseresult = new LinkedHashMap<>(uniprotparse(request));
            if (!mapmode){
                while (!complete.offer(parseresult)){
                }
            }
        }
        else if ("pfam".equals(type)){
            LinkedHashMap<String, ArrayList<String>> parseresult = new LinkedHashMap<>(pfamparse(request));
            if (!mapmode){
                while (!complete.offer(parseresult)){
                }
        }
        }else if ("pfampositionmap".equals(type)){
            pfampositionmap(request);
        
        }else if ("hsdb".equals(type)){
            LinkedHashMap<String, ArrayList<String>> parseresult = new LinkedHashMap<>(hsdbparse(request));
            if (!mapmode){
                while (!complete.offer(parseresult)){
                }
        }
        }else if ("tab-delimited".equals(type)){
            
            LinkedHashMap<String, ArrayList<String>> parseresult = new LinkedHashMap<>(tabparse(request));
            if (request.get(0).substring(1).contains("\t")){
            if (!mapmode){
                while (!complete.offer(parseresult)){
                }
            }
        }
        } else {
            System.out.println("TYPE NOT RECOGNIZED");
        }
        
    }
    
    
    
    private LinkedHashMap<String, ArrayList<String>> uniprotparse(ArrayList<String> request){
        ArrayList<String> lines = request;
        LinkedHashMap<String, ArrayList<String>> parseresult = new LinkedHashMap<>();
        //generate new map from template
        parseresult.keySet().stream().forEach((key) -> {
            parseresult.put(key, new ArrayList<>());
        });      
        LinkedList<String> levels = new LinkedList<>();
        for (String line : lines){
            //System.out.println(line);
            line = line.replace(">",">$");
            String[] pretagsplit = line.split(">");
            ArrayList<String> tagsplit = new ArrayList<>();
            for (String tagsplititem : pretagsplit){
                for (String splitsplititem : tagsplititem.split("<")){
                    if (!splitsplititem.equals("") && !splitsplititem.equals("$")){
                        tagsplit.add(splitsplititem);
                        //System.out.print(splitsplititem+"  |  ");
                    }
                }
            }
            String datatype = "";
            String idvalue = "";
            for (String tag : tagsplit){
                if (tag.startsWith("$")){
                    String mapkey = "";
                    tag = tag.substring(1);
                    mapkey = levels.stream().map((level) -> (level+"_")).reduce(mapkey, String::concat);
                    if (!datatype.trim().equals("")){
                    mapkey+=(datatype);
                    }
                    addtomap(mapkey,tag,parseresult);            
                } else if (tag.startsWith("/")){
                    Object level = levels.pollFirst();
                } else {
                    //not selfclose tag
                    if (!tag.endsWith("/")){
                        try {
                        String[] tagdata = tag.split(" ");
                        levels.push(tagdata[0]);
                        } catch (Exception ex){}
                    } 
                    if (!tag.trim().equals("") ){
                        String[] tagdatapre = tag.split(" ");
                        ArrayList<String> tagdata = new ArrayList<>();
                        for (String tagdatapreitem : tagdatapre){
                            tagdata.addAll(Arrays.asList(tagdatapreitem.split("\" ")));
                        }
                        
                        //tagdata 
                        Integer counter = 0;
                        for (String dataitem : tagdata){
                            if (counter != 0){
                                try {
                                String[] dataitemsplit = dataitem.split("=");
                                String dataintemtype = dataitemsplit[0];
                                String dataintemvalue = dataitemsplit[1];
                                //System.out.println(dataintemtype+"\t"+dataintemvalue);
                                switch (dataintemtype) {
                                    case "type":
                                        datatype = dataintemvalue.replace("\"", "").replace("/", "");
                                        break;
                                    case "id":
                                        idvalue = dataintemvalue.replace("\"", "");
                                        break;
                                    case "value":
                                        idvalue = dataintemvalue.replace("\"", "");
                                        break;
                                    case "description":
                                        idvalue = dataintemvalue.replace("\"", "");
                                        break;
                                    default:
                                        break;
                                }
                                } catch (Exception ex){}
                            }
                            counter++;
                        }
                        //System.out.println(tagdata.get(0));
                        if (tagdata.get(0).equals("entry")){
                            counter = 0;
                            for (String dataitem : tagdata){
                                if (counter > 1){
                                        String[] dataitemsplit = dataitem.split("=");
                                        String dataintemtype = dataitemsplit[0].trim();
                                        String dataintemvalue = dataitemsplit[1];
                                        if (!dataintemtype.equals("xmlns") && !dataintemtype.equals("dataset")){
                                            addtomap("entry_"+dataintemtype,dataintemvalue,parseresult);
                                    }
                                } 
                                counter++;
                        }}
                            
                    }  
                }
            }
            if (!datatype.equals("")&&!idvalue.equals("")){
                if (idvalue.endsWith("/")){
                    idvalue = idvalue.substring(0, idvalue.length()-1);
                }
                String mapkey = "";
                mapkey = levels.stream().map((level) -> (level+"_")).reduce(mapkey, String::concat);
                mapkey+=(datatype);
                addtomap(mapkey,idvalue,parseresult);
            }
            //System.out.print("\n");

        }
        //System.out.println("\n\n\n\n");
        return parseresult;
    }
    
    private LinkedHashMap<String, ArrayList<String>> pfamparse(ArrayList<String> request){
        
        ArrayList<String> lines = request;
       
        LinkedHashMap<String, ArrayList<String>> parseresult = new LinkedHashMap<>(fixedtemplate);
        //generate new map from template
        String datatype = "";
        String text = "";
        for (String key : parseresult.keySet()){
            parseresult.put(key, new ArrayList<>());
        }
        for (String line : lines){
            try {
            if (line.startsWith("#=GF ")){
                line = line.substring(5);
                datatype  = line.substring(0,2);
                text = line.substring(5);
                addtomap(datatype,text,parseresult);
            } 
            
        
        }catch (Exception ex){
                
                }
        }
        return parseresult;
    }
    
    private void pfampositionmap(ArrayList<String> request){

    ArrayList<String> lines = request;
    String pfamid = "";

    //generate new map from template
    String datatype = "";
    String[] text;

    for (String line : lines){
        try {
        if (line.startsWith("#=GF AC")){
            pfamid = line.substring(8);
        } else if (!line.startsWith("#")){       
            line = line.substring(5);
            LinkedHashMap<String, ArrayList<String>> parseresult = new LinkedHashMap<>(fixedtemplate);
            for (String key : parseresult.keySet()){
                parseresult.put(key, new ArrayList<>());
            }
            text = line.substring(0,line.indexOf(" ")).split("/");
            String refseq = text[0];
            String[] position = text[1].split("-");
            String start = position[0];
            String stop = position[1];
            addtomap("pfamAC",pfamid,parseresult);
            addtomap("ID",refseq,parseresult);
            addtomap("start",start,parseresult);
            addtomap("stop",stop,parseresult);
            //System.out.println(pfamid+refseq+start+stop);
            if (!mapmode){
                while (complete.size()>500){}
                    complete.offer(parseresult);
                
            }

        }

    }catch (Exception ex){

            }
    }

    }
    
    private LinkedHashMap<String, ArrayList<String>> hsdbparse(ArrayList<String> request){
        ArrayList<String> lines = request;
        LinkedHashMap<String, ArrayList<String>> parseresult = new LinkedHashMap<>(fixedtemplate);
        //generate new map from template
        String datatype = "";
        String text = "";
        for (String key : parseresult.keySet()){
            parseresult.put(key, new ArrayList<>());
        }
        for (String line : lines){
            try{
            line = line.substring(1);
            int endtagindex = line.indexOf(">");
            
            datatype  = line.substring(0,endtagindex);
            text = line.substring(endtagindex+1,line.length()-endtagindex-3);
            if (datatype.equals("NameOfSubstance") || datatype.equals("ArticleTitle")){
                text = text.toLowerCase();
            }
            if (datatype.equals("htox")){
                subtype = "";
                //System.out.println(line);
                if (text.startsWith("/")){
                
                    text = text.substring(1).trim().toLowerCase();
                if (!text.startsWith("it") && !text.startsWith("the") && !text.startsWith("there")){
                    int subtypeindex = text.indexOf("/");
                    if (subtypeindex != -1){
                        String presubtype = text.substring(0, subtypeindex).replace(" ", "_").replace(".","");

                        subtype=presubtype;

                        

                        if (subtype != "" && subtype.length() < 70 && subtype.length() > 3){
                            if (subtype.contains("symptoms") || subtype.contains("signs")){
                                subtype = "signs_and_symptoms";
                                datatype = datatype+"_"+subtype;
                                
                            } else if (subtype.contains("toxci") || subtype.contains("poison")){
                                subtype = "toxicity";
                                datatype = datatype+"_"+subtype;
                                
                            } else if (subtype.contains("case")){
                                subtype = "case";
                                datatype = datatype+"_"+subtype;
                                //text = text.substring(subtypeindex+2);
                            } else if (subtype.contains("human_exposure")){
                                subtype = "human_exposure";
                                datatype = datatype+"_"+subtype;
                                //text = text.substring(subtypeindex+2);
                            }
                            
                        }
                }
                }}
            }
            
            if (!text.startsWith("$null")){
                addtomap(datatype,text,parseresult);
            }
            }catch (Exception ex){}
        }
        return parseresult;
    }
    
    private void universalparse(ArrayList<String> request){
        //System.out.println("------------------------------");
        ArrayList<String> requestlist = request;
        //System.out.println(requestlist.length);
        String type = "";
        String id = "";
        String text = "";
        Boolean found = false;
        for (String line : requestlist){
            found = false;
            type = "";
            id = "";
            text="";
            //type based
            if (line.contains("type=\"")){
                type = line.split("type=\"", 2)[1].split("\"",2)[0];
                if (type.startsWith("ECO")){
                    type = "";
                }
                found = true;
                if (line.contains("id=")){
                    id = line.split("id=\"", 2)[1].split("\"",2)[0];
                    //System.out.println("ID\t"+type+"\t\t"+id);

                } 
            try{
            text = line.split(">",2)[1];
            if (text.contains("</")){
                text = text.split("</",2)[0];
                //System.out.println("TEXT+\t"+type+"\t\t"+text);
                text = "";
            }
            } catch (Exception ex){   
            }}
            //custom id
            if (!found){
                for (String customtype : customtypes){
                    if (line.contains(customtype)){
                        type = customtype;
                        found = true;
                        try{
                        text = line.split(">",2)[1];
                        if (text.contains("</")){
                            text = text.split("</",2)[0];
                            //System.out.println("CUSTOM\t"+type+"\t\t"+text);
                            text = "";
                        }
                        } catch (Exception ex){   
                            found = false;
                        }     
                        break;
                    }
                }
            }
            //text
            if (!found && !mapmode){
                try{
                text = line.split(">",2)[1];
                type = line.split(">",2)[0].split("<",2)[1];
                if (text.contains("</")){
                    text = text.split("</",2)[0];
                    //System.out.println("TEXT\t"+type+"\t\t"+text);
                    text = "";
                    found = true;
                }
                } catch (Exception ex){   
                }
            }
            if (!found && !mapmode){
                //System.out.println("JUNK\t"+""+"\t\t"+line);
            }    

            if (mapmode){
                
            }
        }
    }
    
    private LinkedHashMap<String, ArrayList<String>> tabparse(ArrayList<String> request){
        ArrayList<String> lines = request;
        String line = request.get(0).substring(1);
        LinkedHashMap<String, ArrayList<String>> parseresult = new LinkedHashMap<>();
        //generate new map from template
        String datatype = "";
        String text = "";
        try{
        String[] linesplit = line.split("\t");
        int index = 0;
        for (String item : linesplit){
            String key = tabtemplate.get(index);
            parseresult.putIfAbsent(key, new ArrayList<>());
            parseresult.get(key).add(item);  
            index++;
        }
        
        }catch (Exception ex){
                
        }
        
        return parseresult;
    }
    
    public void addtomap(String key, String value, LinkedHashMap<String, ArrayList<String>> parseresult){
        key = key.trim().replace("\"", "").replace("[", "").replace("]","");
        if (key.substring(key.length()-1).equals("_")){
            key = key.substring(0,key.length()-1);
        }
        
        if (mapmode){
            Parser.map.putIfAbsent(key, false);
//            if (!Parser.map.get(key)){
//                String valuetrim = value.trim();
//                
//                if (value.length() > 500 && spaceCount(value)>4){
//                    
//                        Parser.map.put(key, true);
//                    
//                }
//            }
        } else {
            value = value.replace("[", "").replace("]", "").replace("\'", "").replace("\"","");
            parseresult.putIfAbsent(key, new ArrayList<>());
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
    
    
    
        /**
     * @return the tabtemplate
     */
    public HashMap<Integer, String> getTabtemplate() {
        return tabtemplate;
    }

    /**
     * @param tabtemplate the tabtemplate to set
     */
    public void setTabtemplate(HashMap<Integer, String> tabtemplate) {
        this.tabtemplate = tabtemplate;
    }
}
