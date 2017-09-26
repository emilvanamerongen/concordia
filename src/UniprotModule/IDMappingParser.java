/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UniprotModule;

import CazyModule.CazyAnnotator;
import Refdbmanager.header;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil3
 */
public class IDMappingParser {
    
    private LinkedHashSet<header> headers  = new LinkedHashSet<>();
    
   
    
    public LinkedHashSet<header> headerscan(File inputfile){
        System.out.println("generating headers for:"+inputfile.getAbsolutePath());
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(inputfile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IDMappingParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        Scanner sc = new Scanner(inputStream, "UTF-8");
        IDMappingEntry tempentry = new IDMappingEntry();
        Integer linenr = 0;
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.equalsIgnoreCase("//")){

            } else {
                tempentry.addline(line);
                
            }
            if (linenr == 3000){
                for (String headerstring : tempentry.getData().keySet()){
                    LinkedHashSet<String> data = tempentry.getData().get(headerstring);
                    headers.add(new header(headerstring,data,true,"0",1,-1,new HashSet()));
                }
                break;
            }
            linenr++;
        }
         return headers;
    }
    
    public void indexheaders(String dbname, HashSet<header> requestedheaders){
        
    }
}
