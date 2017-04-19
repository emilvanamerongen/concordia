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
public class Annotationmanager {
    private File annotationdirectory;
    private HashMap<String, File> annotationfiles = new HashMap<>();
    ArrayList<String> filenames = new ArrayList<>();

    /**
     * @return the annotationdirectory
     */
    public File getAnnotationdirectory() {
        return annotationdirectory;
    }

    /**
     * @param annotationdirectory the annotationdirectory to set
     */
    public void setAnnotationdirectory(File annotationdirectory) {
        this.annotationdirectory = annotationdirectory;
        annotationdirectory.mkdir();
        //updatefiles();     
    }

    /**
     * @return the annotationfiles
     */
    public HashMap<String, File> getAnnotationfiles() {
        return annotationfiles;
    }

    /**
     * @param annotationfiles the annotationfiles to set
     */
    public void setAnnotationfiles(HashMap<String, File> annotationfiles) {
        this.annotationfiles = annotationfiles;
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
        annotationfiles.clear();
        try { 
        for (File file : annotationdirectory.listFiles()){
            annotationfiles.put(file.getName(), file);
        }
        filenames.clear();
        for (File file : annotationfiles.values()){
            filenames.add(file.getName());
        }
        }catch (Exception ex){
            System.out.println("error in updatefiles");
        }
    }
}
