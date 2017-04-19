/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author emil
 */
public class Blastresultmanager {
    private File blastresultdirectory;
    private HashMap<String, File> blastresultfiles = new HashMap<>();
    private ArrayList<String> filenames = new ArrayList<>();

    /**
     * @return the blastresultdirectory
     */
    public File getblastresultdirectory() {
        return blastresultdirectory;
    }

    /**
     * @param blastresultdirectory the blastresultdirectory to set
     */
    public void setblastresultdirectory(File blastresultdirectory) {
        this.blastresultdirectory = blastresultdirectory;
        blastresultdirectory.mkdir();
        //updatefiles();
    }

    /**
     * @return the blastresultfiles
     */
    public HashMap<String, File> getblastresultfiles() {
        return blastresultfiles;
    }

    /**
     * @param blastresultfiles the blastresultfiles to set
     */
    public void setblastresultfiles(HashMap<String, File> blastresultfiles) {
        this.blastresultfiles = blastresultfiles;
    }

    /**
     * @return the filenames
     */
    public ArrayList<String> getFilenames() {
        return filenames;
    }

    /**
     * check directory for changes in files
     */
    public void updatefiles() {
        blastresultfiles.clear();
        try{
        for (File file : blastresultdirectory.listFiles()){
            blastresultfiles.put(file.getName(), file);
        }
        
        filenames.clear();
        for (File file : blastresultfiles.values()){
            filenames.add(file.getName());
        }
        }catch (Exception ex){
            System.out.println("error in updatefiles");
        }
    }
}
