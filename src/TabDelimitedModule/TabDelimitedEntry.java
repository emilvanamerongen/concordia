/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TabDelimitedModule;

import Refdbmanager.header;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 *
 * @author emil3
 */
public class TabDelimitedEntry {
    private HashMap<String,LinkedHashSet<String>> data = new HashMap<>();
    private LinkedHashMap<Integer,String> headermap = new LinkedHashMap<>();
    private String accessionnumber;
    private HashSet<String> enabledheaders = new HashSet<>();
    private Boolean useconfig = false;
    
    public TabDelimitedEntry(){
        
    }
    public TabDelimitedEntry(HashSet<String> enabledheaders){
        this.enabledheaders = enabledheaders;
    }
    
    public void addline(String line, Boolean blasttemplate){
        if (blasttemplate){
            try{
            line = line.replace("Subject","gi\tref");
            line = line.replace("|ref|", "\t").replace("gi|", "").replace("|", "");

            }catch (Exception ex){System.out.println("Error splitting Subject");}
        }
        String[] splitline = line.split("\t");
        int index = 0;
        for (String item : splitline){
            String headerstring = getHeadermap().get(index);
            data.putIfAbsent(headerstring, new LinkedHashSet<>());
            data.get(headerstring).add(item);

            index ++;
        }
    }

    /**
     * @return the headermap
     */

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
     * @return the headermap
     */
    public LinkedHashMap<Integer,String> getHeadermap() {
        return headermap;
    }

    /**
     * @param headermap the headermap to set
     */
    public void setHeadermap(LinkedHashMap<Integer,String> headermap) {
        this.headermap = headermap;
    }


    
    
}
