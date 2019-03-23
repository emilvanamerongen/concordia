/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Refdbmanager;

import FileManager.Filemanager;
import TabDelimitedModule.TabDelimitedIndexer;
import TabDelimitedModule.TabDelimitedParser;
import UniprotModule.IDMappingIndexer;
import UniprotModule.IDMappingParser;
import UniprotModule.UniprotParser;
import UniprotModule.uniprotindexer;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
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
    private SimpleStringProperty priority = new SimpleStringProperty();
    private LinkedHashSet<header> headerset = new LinkedHashSet<>();
    private String fulldbname = "";
    private HashMap<String, HashMap<String, Integer>> indexmaps = new HashMap<>();
    private ConcurrentHashMap<String, uniprotindexer> uniprotindexerthreads = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, IDMappingIndexer> IDmappingindexerthreads = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, TabDelimitedIndexer> tabindexerthreads = new ConcurrentHashMap<>();
    Filemanager filemanager = new Filemanager();
    TransportClient elasticsearchclient = null;
    
    public refdb(){}
    
    public refdb(Integer storefileindex, String fulldbname, String entries,TransportClient elasticsearchclient) throws InterruptedException, ExecutionException{
        this.elasticsearchclient = elasticsearchclient;
        this.storefileindex = storefileindex;
        setFulldbname(fulldbname);
        priority.set("0");
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
        IndexMetaData index = elasticsearchclient.admin().cluster().prepareState().get().getState().getMetaData().getIndices().get(getFulldbname());
        Collection<Object> values = index.getMappings().get("entry").getSourceAsMap().values();
        String valuestring = "";
                for (Object value : values){
            valuestring += value;
        }
        valuestring = valuestring.substring(1);
        String[] valuesplit = valuestring.split("}, "); 

        for (String value : valuesplit){
            value = value.substring(0,value.indexOf("="));
            header header = new header(value,true);
           
            //header.setExamples(retrieveexample(value,getFulldbname()));
            headerset.add(header);
        }
        
    }

    private LinkedHashSet<String> retrieveexample(String field, String dbname){ 
        LinkedHashSet<String> examples = new LinkedHashSet<>();
        //example search
        SearchResponse response = elasticsearchclient.prepareSearch(dbname)
        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
        .setQuery(wildcardQuery(field,"*"))                 // Query
        .setFrom(0).setSize(10).setExplain(false)
        .get();
        // MatchAll on the whole cluster with all default options
        SearchResponse someentries = elasticsearchclient.prepareSearch().get();
        System.out.println(someentries.getHits().internalHits().length);
        for (SearchHit hit : someentries.getHits()){
            try{
                Map<String, Object> thesource = hit.getSource();
                String example = "";
                if (thesource.containsKey(field)){
                    String get = ""+thesource.get(field);
                    if (!get.equals("null")){
                        System.out.println(get);
                    }
                    examples.add(""+thesource.get(field));
                }
                
                

                }catch (Exception ex){System.out.println(ex);}
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
        System.out.println("writing headers");
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
                linestowrite.add(myheader.getHeaderstring()+"\t"+examplestring+"\t"+myheader.getEnabled()+"\t"+myheader.getColorindex().toString()+"\t"+indexable+"\t"+myheader.getTabindex()+"\t"+parameterstring);
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
    public String getPriority() {
        return priority.getValue();
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(String priority) {
        this.priority.setValue(priority);
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
