/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Annotator;

import Annotator.AnnotationRequest;
import Annotator.AnnotationProcess;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil3
 */
public class WriterThread extends Thread{
    private File outputfile;
    private FileOutputStream outputstream;
    private Boolean active = true;
    
    public WriterThread(File outputfile){
        this.outputfile = outputfile;
        try {
            this.outputstream = new FileOutputStream(outputfile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WriterThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void run(){
        
        while (active) {
            if (!AnnotationProcess.complete.isEmpty()){
                try {
                    AnnotationRequest completedrequest = AnnotationProcess.complete.poll();
                    outputstream.write((completedrequest.getResult()+"\n").getBytes());   

                } catch (IOException ex) {
                    Logger.getLogger(WriterThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
    }
        try {
            outputstream.close();
        } catch (IOException ex) {
            Logger.getLogger(WriterThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the outputfile
     */
    public File getOutputfile() {
        return outputfile;
    }

    /**
     * @param outputfile the outputfile to set
     */
    public void setOutputfile(File outputfile) {
        this.outputfile = outputfile;
    }

    /**
     * @return the outputstream
     */
    public FileOutputStream getOutputstream() {
        return outputstream;
    }

    /**
     * @param outputstream the outputstream to set
     */
    public void setOutputstream(FileOutputStream outputstream) {
        this.outputstream = outputstream;
    }

    /**
     * @return the active
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    
}
