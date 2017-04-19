/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Refdbmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;

/**
 *
 * @author emil3
 */
public class refdb {
    private ObservableValue<Boolean> select;
    private String type;
    private Integer storefileindex;
    private Integer headerindex;
    private String locationline;
    private String dbname;
    private String filepath;
    private File datafile;
    private String remotelocation; 
    
    public refdb(Integer storefileindex, String locationline){
        this.storefileindex = storefileindex;
        this.locationline = locationline;
        
        String[] linesplit = locationline.split("\t");
        
        int index = 0;
        for (String item : linesplit){
            if (index == 0){      
                type = item;
            } else if (index == 1){
                dbname = item;
            } else if (index == 2) {
                headerindex = Integer.parseInt(item); 
            } else if (index == 3){
                datafile = (new File(item));
                filepath = item;
            } else if (index == 4){
                remotelocation = item;
            }
            
            index++;
        }             
    }
    public ArrayList<String> gettabledata(){
        ArrayList<String> data = new ArrayList<>();
        data.add("UniProt db");
        data.add(getDbname());
        data.add("1");
        data.add("2");
        data.add(getDatafile().getAbsolutePath());
        data.add(getRemotelocation());
        return(data);
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
     * @return the headerindex
     */
    public Integer getHeaderindex() {
        return headerindex;
    }

    /**
     * @param headerindex the headerindex to set
     */
    public void setHeaderindex(Integer headerindex) {
        this.headerindex = headerindex;
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
     * @return the dbname
     */
    public String getDbname() {
        return dbname;
    }

    /**
     * @param dbname the dbname to set
     */
    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    /**
     * @return the filepath
     */
    public String getFilepath() {
        return filepath;
    }

    /**
     * @param filepath the filepath to set
     */
    public void setFilepath(String filepath) {
        this.filepath = filepath;
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
        return remotelocation;
    }

    /**
     * @param remotelocation the remotelocation to set
     */
    public void setRemotelocation(String remotelocation) {
        this.remotelocation = remotelocation;
    }

    /**
     * @return the select
     */
    public ObservableValue<Boolean> getSelect() {
        return select;
    }
}
