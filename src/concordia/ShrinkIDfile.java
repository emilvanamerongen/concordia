/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import UniprotModule.UniprotThread;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil3
 */
public class ShrinkIDfile {
    private File inputfile;
    
    /**
     *
     * @param idfile
     */
    public ShrinkIDfile(File idfile){
        this.inputfile = idfile;
        }
        
    /**
     *
     */
    public void shrink(){
        File outputfile = new File(inputfile.getAbsolutePath()+" shrunk");
        FileInputStream stream = null;
        FileOutputStream outputstream = null;
        try {
            stream = newfilestream();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UniprotThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            outputstream = new FileOutputStream(outputfile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UniprotThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        Scanner sc = new Scanner(stream, "UTF-8");
        while (sc.hasNextLine()) {
            // LOOP over regels van bestand
            String line = sc.nextLine();
            String[] splitline = line.split("\t"); 
            if (splitline[1].equals("GI") || splitline[1].equals("RefSeq")){
            try {
                outputstream.write((line+"\n").getBytes());
                outputstream.flush();
            } catch (IOException ex) {
                Logger.getLogger(UniprotThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
        }
        try {
                outputstream.close();
            } catch (IOException ex) {
                Logger.getLogger(UniprotThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
        
    private FileInputStream newfilestream () throws FileNotFoundException{
        FileInputStream inputStream = null;
        inputStream = new FileInputStream(inputfile.getAbsolutePath());
        return inputStream;
    }
}
