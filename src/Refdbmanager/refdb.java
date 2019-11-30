/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Refdbmanager;

import FileManager.Filemanager;
import TabDelimitedModule.TabDelimitedIndexer;
import UniprotModule.IDMappingIndexer;
import UniprotModule.uniprotindexer;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

/**
 *
 * @author emil3
 */
public class refdb {

    private ObservableValue<Boolean> select = new SimpleBooleanProperty();
    private String type;
    private Integer storefileindex;
    private SimpleStringProperty headerindex = new SimpleStringProperty();;
    private String locationline;
    private SimpleStringProperty dbname = new SimpleStringProperty();
    private SimpleStringProperty filepath = new SimpleStringProperty();
    private File datafile;
    private SimpleStringProperty remotelocation = new SimpleStringProperty();
    private SimpleStringProperty entries = new SimpleStringProperty();
    private SimpleIntegerProperty priority = new SimpleIntegerProperty();
    private LinkedHashSet<header> headerset = new LinkedHashSet<>();
    private String fulldbname = "";
    private HashMap<String, HashMap<String, Integer>> indexmaps = new HashMap<>();
    private ConcurrentHashMap<String, uniprotindexer> uniprotindexerthreads = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, IDMappingIndexer> IDmappingindexerthreads = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, TabDelimitedIndexer> tabindexerthreads = new ConcurrentHashMap<>();
    Filemanager filemanager = new Filemanager();
    RestHighLevelClient elasticsearchclient = null;
    
    public refdb(){}
    
    public refdb(Integer storefileindex, String fulldbname, String entries,RestHighLevelClient elasticsearchclient) throws InterruptedException, ExecutionException{
        this.elasticsearchclient = elasticsearchclient;
        this.storefileindex = storefileindex;
        setFulldbname(fulldbname);
        priority.set(0);
        String[] split = fulldbname.split("\\.");

        int index = 0;
        for (String item : split){
            switch (index) {
                case 0:
                    type = item;
                    break;
                case 1:
                    dbname.set(item); 
                    break;
                case 2:
                    headerindex.set(item);
                    break;
                default:
                    break;
            }
            index++;
        }

        this.entries.set(entries);
        setheaders();
 
    }
    
    public void setheaders() throws InterruptedException, ExecutionException{
        try {
            GetMappingsRequest request = new GetMappingsRequest(); 
            request.indices(getFulldbname()); 
            
            GetMappingsResponse getMappingResponse = elasticsearchclient.indices().getMapping(request, RequestOptions.DEFAULT);
            ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> allMappings = getMappingResponse.mappings();
            

            ImmutableOpenMap<String, MappingMetaData> indexMapping = allMappings.get(getFulldbname()); 
            
            ArrayList<String> values = new ArrayList<>();
            for (ObjectCursor<MappingMetaData> amap : indexMapping.values()){
      
                Map<String, Object> map = amap.value.getSourceAsMap();
                
                for (Object key : map.values()){
                    
                    String keystring = key.toString();
                    try{
                    keystring = keystring.substring(1);
                    for (String field : keystring.split("}}, ")){
                        values.add(field);
                    }
                    } catch (Exception ex){
                        System.out.println(ex);
                    }
                }
            }


            
            for (String value : values){
                value = value.substring(0,value.indexOf("="));
                header header = new header(value,true);
                
                //header.setExamples(retrieveexample(value,getFulldbname()));
                headerset.add(header);
            }
            File headerfile = new File(filemanager.getHeaderdirectory()+File.separator+dbname.getValue()+".headers.txt");
            if (headerfile.exists()){
                HashMap<String, ArrayList<String>> fileheaders = new HashMap<>();
                try (BufferedReader br = new BufferedReader(new FileReader(headerfile))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] splitline = line.split("\t");
                        ArrayList<String> data = new ArrayList<>();
                        data.add(splitline[1]);
                        data.add(splitline[2]);
                        data.add(splitline[3]);
                        data.add(splitline[4]);
                        data.add(splitline[5]);
                        data.add(splitline[6]);
                        fileheaders.put(splitline[0], data);
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(refdb.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(refdb.class.getName()).log(Level.SEVERE, null, ex);
                }
                Boolean applied = false;
                for (header aheader : headerset){
                    try{
                        ArrayList<String> get = fileheaders.get(aheader.getHeaderstring());
                        aheader.setColorindex(Integer.parseInt(get.get(2)));
                        if (!applied){
                            setPriority(Integer.parseInt(get.get(5)));
                            applied = true;
                        }
                    } catch (Exception ex){}
                }
            } else {
                System.out.println("no file found");
            }
            headerupdate();
        } catch (IOException ex) {
            Logger.getLogger(refdb.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public LinkedHashSet<String> retrieveexample(String field){ 
        LinkedHashSet<String> examples = new LinkedHashSet<>();
        //example search
        SearchRequest searchRequest = new SearchRequest(); 
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); 
        searchSourceBuilder.query(QueryBuilders.existsQuery(field)); 
        searchSourceBuilder.size(20);
        searchRequest.indices(getFulldbname());
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = elasticsearchclient.search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit hit : searchResponse.getHits()){
                Map<String, Object> hits = hit.getSourceAsMap();
                if (hits.containsKey(field)){
                    String get = ""+hits.get(field);
                    if (!get.equals("null")){
                    }
                    examples.add(get);
                }
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(refdb.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (examples.size()  < 2){
            System.out.println(field);
        }
        return examples;
    }
        
    
    public void headerupdate(){
        File headerfile = new File(filemanager.getHeaderdirectory()+File.separator+dbname.getValue()+".headers.txt");
        writeheaders(headerfile, false);       
    }
    
    public void forceheaderreset(){
        File headerfile = new File(filemanager.getHeaderdirectory()+File.separator+dbname.getValue()+".headers.txt");
        headerfile.delete();
        headerset.clear();

    }
    
    
    private void writeheaders(File headerfile, Boolean sort){
        //System.out.println("writing headers");
        headerfile.delete();
        try {
            headerfile.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(dbmanager.class.getName()).log(Level.SEVERE, null, ex);
        }
        try { 
            FileWriter writer = new FileWriter(headerfile);
            ArrayList<String> linestowrite = new ArrayList<>();
            for (header myheader : headerset){            
                String examplestring = "";
                for (String example : myheader.getExamples()){
                    examplestring += example;
                    if (!example.endsWith(";")){
                        examplestring += ";";
                    }
                }
                String parameterstring = "";
                for (String parameter : myheader.getParameters()){
                    parameterstring += parameter;
                    if (!parameter.endsWith(";")){
                        parameterstring += ";";
                    }
                }
                String indexable = "1";
                if (!myheader.getIndexable()){
                    indexable = "0";
                }

               
                
                linestowrite.add(myheader.getHeaderstring()+"\t"+examplestring+"\t"+myheader.getEnabled()+"\t"+myheader.getColorindex().toString()+"\t"+indexable+"\t"+myheader.getTabindex()+"\t"+priority.getValue()+"\t"+parameterstring);
            }
            if (sort){
                Collections.sort(linestowrite);
            }

            for (String line : linestowrite){
                writer.write(line);
                writer.write("\n");
            }
            writer.close();
            
        } catch (IOException ex) {
            Logger.getLogger(dbmanager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // indexing
    
    
    public void generateindex(ArrayList<header> headers){
        if (type.equals("uniprot")){
            uniprotindexer newindexer = new uniprotindexer(headers, datafile, this);
            getUniprotindexerthreads().put(newindexer.getIndexername(), newindexer);
                    
        } else if (type.equals("uniprot ID-mapping")){
           IDMappingIndexer newindexer = new IDMappingIndexer(headers, datafile, this);
           getIDmappingindexerthreads().put(newindexer.getIndexername(), newindexer);
        } else if (type.equals("tab-delimited")){
            TabDelimitedIndexer newindexer = new TabDelimitedIndexer(headers, datafile, this);
            getTabindexerthreads().put(newindexer.getIndexername(), newindexer);
        }
        
        for (uniprotindexer uniprotindexerthread : getUniprotindexerthreads().values()){
            uniprotindexerthread.start();
        }
        for (TabDelimitedIndexer tabindexerthread : getTabindexerthreads().values()){
            tabindexerthread.start();
        }
        for (IDMappingIndexer idmappingindexerthread : getIDmappingindexerthreads().values()){
            idmappingindexerthread.start();
        }
        
        
    }

    
    public void scanindexrequests(){
        ArrayList<header> requestedheaders = new ArrayList<>();
        for (header myheader : headerset){   
            if (! myheader.getColorindex().equals(0)){
                myheader.checkifindexed();
                if (! myheader.getIndexed()){
                    requestedheaders.add(myheader);
                }
            }

        } 
        if (! requestedheaders.isEmpty()){
        generateindex(requestedheaders);}
    }
    
    public void loadindex(header myheader){
        
    }
    
    public void loadalllinked(){
        
    }
    
    public void unloadindex(header myheader){
        
    }
    
    public void unloadallindices(){
        
    }
    
    
    
    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the storefileindex
     */
    public Integer getStorefileindex() {
        return storefileindex;
    }

    /**
     * @param storefileindex the storefileindex to set
     */
    public void setStorefileindex(Integer storefileindex) {
        this.storefileindex = storefileindex;
    }




    /**
     * @return the select
     */
    public ObservableValue<Boolean> getSelect() {
        return select;
    }

    /**
     * @param select the select to set
     */
    public void setSelect(ObservableValue<Boolean> select) {
        this.select = select;
    }

    /**
     * @return the headerindex
     */
    public String getHeaderindex() {
        return headerindex.getValue();
    }

    /**
     * @param headerindex the headerindex to set
     */
    public void setHeaderindex(String headerindex) {
        this.headerindex.set(headerindex);
    }

    /**
     * @return the dbname
     */
    public String getDbname() {
        return dbname.getValue();
    }

    /**
     * @param dbname the dbname to set
     */
    public void setDbname(String dbname) {
        this.dbname.set(dbname);
    }

    /**
     * @return the filepath
     */
    public String getFilepath() {
        return filepath.get();
    }

    /**
     * @param filepath the filepath to set
     */
    public void setFilepath(String filepath) {
        this.filepath.set(filepath);
    }

    /**
     * @return the datafile
     */
    public File getDatafile() {
        return datafile;
    }

    /**
     * @param datafile the datafile to set
     */
    public void setDatafile(File datafile) {
        this.datafile = datafile;
    }

    /**
     * @return the remotelocation
     */
    public String getRemotelocation() {
        return remotelocation.getValue();
    }

    /**
     * @param remotelocation the remotelocation to set
     */
    public void setRemotelocation(String remotelocation) {
        this.remotelocation.set(remotelocation);
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return entries.getValue();
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.entries.set(status);
    }

    /**
     * @return the headerset
     */
    public LinkedHashSet<header> getHeaderset() {
        return headerset;
    }

    /**
     * @param headerset the headerset to set
     */
    public void setHeaderset(LinkedHashSet<header> headerset) {
        this.headerset = headerset;
    }

    /**
     * @return the priority
     */
    public Integer getPriority() {
        return priority.getValue();
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(Integer priority) {
        this.priority.setValue(priority);
        headerupdate();
    }

    /**
     * @return the uniprotindexerthreads
     */
    public ConcurrentHashMap<String, uniprotindexer> getUniprotindexerthreads() {
        return uniprotindexerthreads;
    }

    /**
     * @param uniprotindexerthreads the uniprotindexerthreads to set
     */
    public void setUniprotindexerthreads(ConcurrentHashMap<String, uniprotindexer> uniprotindexerthreads) {
        this.uniprotindexerthreads = uniprotindexerthreads;
    }

    /**
     * @return the IDmappingindexerthreads
     */
    public ConcurrentHashMap<String, IDMappingIndexer> getIDmappingindexerthreads() {
        return IDmappingindexerthreads;
    }

    /**
     * @param IDmappingindexerthreads the IDmappingindexerthreads to set
     */
    public void setIDmappingindexerthreads(ConcurrentHashMap<String, IDMappingIndexer> IDmappingindexerthreads) {
        this.IDmappingindexerthreads = IDmappingindexerthreads;
    }

    /**
     * @return the tabindexerthreads
     */
    public ConcurrentHashMap<String, TabDelimitedIndexer> getTabindexerthreads() {
        return tabindexerthreads;
    }

    /**
     * @param tabindexerthreads the tabindexerthreads to set
     */
    public void setTabindexerthreads(ConcurrentHashMap<String, TabDelimitedIndexer> tabindexerthreads) {
        this.tabindexerthreads = tabindexerthreads;
    }

        /**
     * @return the fulldbname
     */
    public String getFulldbname() {
        return fulldbname;
    }

    /**
     * @param fulldbname the fulldbname to set
     */
    public void setFulldbname(String fulldbname) {
        this.fulldbname = fulldbname;
    }
}
