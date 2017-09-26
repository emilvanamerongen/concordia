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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;

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
    private SimpleStringProperty status = new SimpleStringProperty();
    private SimpleStringProperty priority = new SimpleStringProperty();
    private LinkedHashSet<header> headerset = new LinkedHashSet<>();
    private HashMap<String, HashMap<String, Integer>> indexmaps = new HashMap<>();
    private ConcurrentHashMap<String, uniprotindexer> uniprotindexerthreads = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, IDMappingIndexer> IDmappingindexerthreads = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, TabDelimitedIndexer> tabindexerthreads = new ConcurrentHashMap<>();
    Filemanager filemanager = new Filemanager();
    
    
    public refdb(){}
    
    public refdb(Integer storefileindex, String locationline){
        this.storefileindex = storefileindex;
        this.locationline = locationline;
        priority.set("0");
        String[] linesplit = locationline.split("\t");
        
        int index = 0;
        for (String item : linesplit){
            if (index == 0){      
                type = item;
            } else if (index == 1){
                dbname.set(item);
            } else if (index == 2) {
                headerindex.set(item); 
            } else if (index == 3){
                datafile = (new File(item));
                filepath.set(item);
            } else if (index == 4){
                remotelocation.set(item);
            } else if (index == 5){
                priority.set(item);
            }
            
            index++;
        }
        
        if (!datafile.exists()){
            status.set("FILE unavailable");
        } else {
        status.set("Processing header..");
        headerinit();
        status.set("available");
        }
    }
    
    private void headerinit(){
        File headerfile = new File(filemanager.getHeaderdirectory()+File.separator+dbname.getValue()+".headers.txt");
        Boolean sort = true;
        if (headerfile.exists()){
            try {
                List<String> headerlines = Files.readAllLines(Paths.get(headerfile.getAbsolutePath()));
                for (String line : headerlines){
                    String headerstring = line.split("\t")[0];
                    String examplestring = line.split("\t")[1];
                    String[] examplelist = examplestring.split(";");
                    LinkedHashSet<String> exampleset = new LinkedHashSet<>();
                    for (String example : examplelist){
                        exampleset.add(example);
                    }
                     LinkedHashSet<String> parameterset = new LinkedHashSet<>();
                    try{
                    String parameterstring = line.split("\t")[6];
                    String[] parameterlist = parameterstring.split(";");
                   
                    for (String parameter : parameterlist){
                        parameterset.add(parameter);
                    }} catch (Exception ex){}
                    
                    
                    String enabled = line.split("\t")[2];
                    Boolean enabledbool = true;
                    if (enabled.equals("false")){
                        enabledbool = false;
                    }
                    String linkcolor = "0";
                    Integer indexable = 1;
                    Integer tabindex = -1;
                    try{
                        linkcolor = line.split("\t")[3];
                        indexable = Integer.parseInt(line.split("\t")[4]);
                        tabindex = Integer.parseInt(line.split("\t")[5]);
                    } catch (Exception ex){                
                    }
                     
                    headerset.add(new header(headerstring,exampleset,enabledbool,linkcolor,indexable,tabindex,parameterset));
                }
            } catch (IOException ex) {
                Logger.getLogger(refdb.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            if (type.equals("uniprot")){
                UniprotParser uniprotparser = new UniprotParser();
                headerset.addAll(uniprotparser.headerscan(datafile));
            } else if (type.equals("uniprot ID-mapping")){
                IDMappingParser idmappingparser = new IDMappingParser();
                headerset.addAll(idmappingparser.headerscan(datafile));
            } else if (type.equals("tab-delimited")){
                sort = false;
                TabDelimitedParser tabdelimitedparser = new TabDelimitedParser();
                tabdelimitedparser.setHeaderindex(Integer.parseInt(headerindex.getValue()));
                for (header myheader : tabdelimitedparser.headerscan(datafile,false)){
                    headerset.add(myheader);
                }
                
            } else if (type.equals("template (tab)")){
                sort = false;
                TabDelimitedParser tabdelimitedparser = new TabDelimitedParser();
                tabdelimitedparser.setHeaderindex(Integer.parseInt(headerindex.getValue()));
                for (header myheader : tabdelimitedparser.headerscan(datafile,true)){
                    headerset.add(myheader);
                }
            }
            
            for (header myheader : headerset){
                myheader.setSourcedb(dbname.getValue());
            }
            
            writeheaders(headerfile, sort);

        }

    }
    
    public void headerupdate(){
        File headerfile = new File(filemanager.getHeaderdirectory()+File.separator+dbname.getValue()+".headers.txt");
        writeheaders(headerfile, false);       
    }
    
    public void forceheaderreset(){
        File headerfile = new File(filemanager.getHeaderdirectory()+File.separator+dbname.getValue()+".headers.txt");
        headerfile.delete();
        headerset.clear();
        headerinit();
    }
    
    
    private void writeheaders(File headerfile, Boolean sort){
        headerfile.delete();
        try {
            headerfile.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(dbmanager.class.getName()).log(Level.SEVERE, null, ex);
        }
        try { 
            FileWriter writer = new FileWriter(headerfile);
            ArrayList<String> linestowrite = new ArrayList<String>();
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
     * @return the locationline
     */
    public String getLocationline() {
        return locationline;
    }

    /**
     * @param locationline the locationline to set
     */
    public void setLocationline(String locationline) {
        this.locationline = locationline;
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
        return status.getValue();
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status.set(status);
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
     * @return the indexerprogress
     */

}
