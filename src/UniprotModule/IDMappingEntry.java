/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UniprotModule;

import Refdbmanager.header;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;

/**
 *
 * @author emil3
 */
public class IDMappingEntry {
    private HashMap<String,LinkedHashSet<String>> data = new HashMap<>();
    private String accessionnumber;
    private HashSet<header> headerconfig = new HashSet<>();
    private Boolean useconfig = false;
    
    public IDMappingEntry(){
        getData().putIfAbsent("Accession number", new LinkedHashSet<>());
    }
    public IDMappingEntry(HashSet<header> headerconfig, Boolean useconfig){
        this.headerconfig = headerconfig;
        this.useconfig = useconfig;
        getData().putIfAbsent("Accession number", new LinkedHashSet<>());
    }
    
    public void addline(String line){
        try {
            
            StringTokenizer st = new StringTokenizer(line,"\t");
            String uniprotid = st.nextToken();
            getData().get("Accession number").add(uniprotid.trim());
            String key = st.nextToken();
            String value = st.nextToken();
            getData().putIfAbsent(key, new LinkedHashSet<>());
            getData().get(key).add(value.trim());
        } catch (Exception ex){
            
        }}

    /**
     * @return the data
     */
    public HashMap<String,LinkedHashSet<String>> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(HashMap<String,LinkedHashSet<String>> data) {
        this.data = data;
    }

    /**
     * @return the accessionnumber
     */
    public String getAccessionnumber() {
        return accessionnumber;
    }

    /**
     * @param accessionnumber the accessionnumber to set
     */
    public void setAccessionnumber(String accessionnumber) {
        this.accessionnumber = accessionnumber;
    }
}
