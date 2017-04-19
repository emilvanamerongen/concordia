/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Refdbmanager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author emil3
 */
public class dbmanager {


    private File locationstorage;
    private ObservableList<refdb> referencedatabases = FXCollections.observableArrayList();
    private List<String> locationstoragelines = new ArrayList<>();
    
    public dbmanager(){
        read();
    }
    
    public void read(){
        locationstorage = new File("locationstorage.txt");
        System.out.println("loading saved reference database locations from: ");
        System.out.println(locationstorage.getAbsolutePath());
        referencedatabases.clear();
        if (! locationstorage.exists()){
            try {
                locationstorage.createNewFile();
            } catch (IOException ex) {
                System.out.println("ERROR creating db location file!");
            }     
        } else {
            try {
                locationstoragelines = Files.readAllLines(Paths.get(locationstorage.getAbsolutePath()));
                Integer index = 0;
                for (String line : locationstoragelines){
                    referencedatabases.add(new refdb(index,line));
                    index ++; 
                    };        
            } catch (IOException ex) {
                Logger.getLogger(dbmanager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void rewrite(){
        if (! locationstorage.canWrite()){
            System.out.println("ERROR can't write db location file");
        }
        locationstorage.delete();
        try {
            locationstorage.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(dbmanager.class.getName()).log(Level.SEVERE, null, ex);
        }
        try { 
            FileWriter writer = new FileWriter(locationstorage);
            for (String line : locationstoragelines){
                writer.write(line);
            }
            writer.close();
            
        } catch (IOException ex) {
            Logger.getLogger(dbmanager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void adddb(String type, String name, String headerindex, String filelocation, String remoteurl){
        locationstoragelines.add(type+"\t"+name+"\t"+headerindex+"\t"+filelocation+"\t"+remoteurl);  
        rewrite();
        read();
    }
    
        /**
     * @return the referencedatabases
     */
    public ObservableList<refdb> getReferencedatabases() {
        return referencedatabases;
    }

}
