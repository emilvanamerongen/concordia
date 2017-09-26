/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UniprotModule;

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
public class UniprotParser {
    private HashMap<String,UniprotEntry> data = new HashMap<>();
    private LinkedHashSet<header> headers  = new LinkedHashSet<>();
    public static volatile Integer active = 0;

    
    public LinkedHashSet<header> headerscan(File inputfile){
        System.out.println("generating headers for:"+inputfile.getAbsolutePath());
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(inputfile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UniprotParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        Scanner sc = new Scanner(inputStream, "UTF-8");
        UniprotEntry tempentry = new UniprotEntry();
        Integer linenr = 0;
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.equalsIgnoreCase("//")){

            } else {
                tempentry.addline(line);
                
            }
            if (linenr == 100000){
                break;
            }
            linenr++;
        }
        for (String headerstring : tempentry.getData().keySet()){
                    LinkedHashSet<String> data = tempentry.getData().get(headerstring);
                    header myheader = new header(headerstring,data,true,"0",1,-1,new HashSet());

                    if (tempentry.getIndexable().contains(headerstring)){
                    } else {
                        myheader.setIndexable(false);
                    }
                    headers.add(myheader);
        }
         return headers;
    }
    
    

    
    
}
