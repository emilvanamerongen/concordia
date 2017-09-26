/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TabDelimitedModule;

import CazyModule.CazyAnnotator;
import Refdbmanager.header;
import UniprotModule.IDMappingEntry;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil3
 */
public class TabDelimitedParser {   
    private LinkedHashSet<header> headers  = new LinkedHashSet<>();
    private Integer headerindex;
   
    
    public LinkedHashSet<header> headerscan(File inputfile, Boolean blasttemplate){
        System.out.println("generating headers for:"+inputfile.getAbsolutePath());
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(inputfile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TabDelimitedParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        Scanner sc = new Scanner(inputStream, "UTF-8");
        TabDelimitedEntry tempentry = new TabDelimitedEntry();
        Integer linenr = 0;
        LinkedHashMap<Integer,String> headermap = new LinkedHashMap<>();
        Boolean active = false;
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (active){
                tempentry.addline(line, blasttemplate);
                
            }
            if (linenr == 2000){
                Integer tabindex = 0;
                for (String headerstring : headermap.values()){
                    LinkedHashSet<String> data = tempentry.getData().get(headerstring);
                    headers.add(new header(headerstring,data,true,"0",1,tabindex,new HashSet()));
                    tabindex++;
                }
                break;
                
            }
            if (linenr == getHeaderindex()){
                if (blasttemplate){
                    line = line.replace("Subject","gi\tref");
                }
                Integer index = 0;
                for (String headeritem : line.split("\t")){
                    headermap.putIfAbsent(index, headeritem);
                    index++;
                }
                tempentry.setHeadermap(headermap);
                active = true;
            }
           linenr++;
        }
         return headers;
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
}
