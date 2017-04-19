/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UniprotModule;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author emil3
 */
public class UniprotAnnotator {
    private final ArrayList<File> inputfiles;
    private final ArrayList<File> uniprotfiles;
    private final File uniprotidfile;
    private final File annotationdirectory;
    private final HashMap<Integer, ArrayList<String>> uniprotdata = new HashMap<>();

    /**
     *
     */
    public Uniprotmainthread mainthread;
    private final Boolean lowrammode;
    

    
    //import cazy reference data files

    /**
     *
     * @param inputfiles
     * @param uniprotidfile
     * @param uniprotfiles
     * @param annotationdirectory
     * @param lowrammode
     */
    public UniprotAnnotator(ArrayList<File> inputfiles,File uniprotidfile , ArrayList<File> uniprotfiles, File annotationdirectory, Boolean lowrammode){
        this.inputfiles = inputfiles;
        this.uniprotfiles = uniprotfiles;
        this.uniprotidfile = uniprotidfile;
        this.annotationdirectory = annotationdirectory;
        this.lowrammode = lowrammode;
    }
    
    /**
     *
     */
    public void annotate(){
        System.out.println("\n\tUNIPROT ANNOTATION MODULE\n\tby: Emil van Amerongen\n");
        mainthread = new Uniprotmainthread(uniprotfiles, uniprotidfile , inputfiles, annotationdirectory, lowrammode);
        mainthread.start();   
    }
    
    
     

}
