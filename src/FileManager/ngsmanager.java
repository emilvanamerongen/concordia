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
public class ngsmanager {
    private File ngsdirectory;
    private HashMap<String, File> ngsfiles = new HashMap<>();
    private ArrayList<String> filenames = new ArrayList<>();

    /**
     * @return the ngsdirectory
     */
    public File getNgsdirectory() {
        return ngsdirectory;
    }

    /**
     * @param ngsdirectory the ngsdirectory to set
     */
    public void setNgsdirectory(File ngsdirectory) {
        this.ngsdirectory = ngsdirectory;
        ngsdirectory.mkdir();
        updatefiles();
    }

    /**
     * @return the ngsfiles
     */
    public HashMap<String, File> getNgsfiles() {
        return ngsfiles;
    }

    /**
     * @param ngsfiles the ngsfiles to set
     */
    public void setNgsfiles(HashMap<String, File> ngsfiles) {
        this.ngsfiles = ngsfiles;
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
        ngsfiles.clear();
        for (File file : ngsdirectory.listFiles()){
            ngsfiles.put(file.getName(), file);
        }
        filenames.clear();
        for (File file : ngsfiles.values()){
            filenames.add(file.getName());
        }
    }
}
