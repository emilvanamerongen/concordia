/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Annotator;

import java.io.File;
import java.util.HashMap;

/**
 *
 * @author emil3
 */
public class AnnotationFileInfo {
    private File file;
    private HashMap<String, Long> annotationcoverage = new HashMap<>();
    private Long totalsize;
    private Long progress;
    private Boolean done = false;

    public AnnotationFileInfo(File file){
        this.file = file;
        totalsize = file.length();
    }
    
    
    /**
     * @return the file
     */
    synchronized public File getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    synchronized public void setFile(File file) {
        this.file = file;
    }

    /**
     * @return the annotationcoverage
     */
    synchronized public HashMap<String, Long> getAnnotationcoverage() {
        return annotationcoverage;
    }

    /**
     * @param annotationcoverage the annotationcoverage to set
     */
    synchronized public void setAnnotationcoverage(HashMap<String, Long> annotationcoverage) {
        this.annotationcoverage = annotationcoverage;
    }

    /**
     * @return the totalsize
     */
    synchronized public Long getTotalsize() {
        return totalsize;
    }

    /**
     * @param totalsize the totalsize to set
     */
    synchronized public void setTotalsize(Long totalsize) {
        this.totalsize = totalsize;
    }

    /**
     * @return the progress
     */
    synchronized public Long getProgress() {
        return progress;
    }

    /**
     * @param progress the progress to set
     */
    synchronized public void setProgress(Long progress) {
        this.progress = progress;
    }

    /**
     * @return the done
     */
    synchronized public Boolean getDone() {
        return done;
    }

    /**
     * @param done the done to set
     */
    synchronized public void setDone(Boolean done) {
        this.done = done;
    }
    
}
