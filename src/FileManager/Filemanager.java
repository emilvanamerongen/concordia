/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author emil
 */
public class Filemanager {
    private File projectdirectory;

    /**
     *
     */
    public Blastresultmanager blastresultmanager = new Blastresultmanager();

    /**
     *
     */
    public Annotationmanager annotationmanager = new Annotationmanager();
    //databases
    private File cazyfilelocations;
    private File uniprotfilelocations;
    private File referencedatabaselocations;

    /**
     *
     */
    public ArrayList<File> cazysources = new ArrayList<>();

    /**
     *
     */
    public ArrayList<File> uniprotsources = new ArrayList<>();

    /**
     *
     */
    public ArrayList<File> referencedatabasefiles = new ArrayList<>();
    
    //properties file
    private Properties myproperties = new Properties();
    /**
     */
    public Filemanager(){
        boolean errorinlocation = false;
        try {
        myproperties.load(new FileInputStream("concordia.properties"));
        projectdirectory = new File(myproperties.getProperty("projectfolder"));
        if (! projectdirectory.exists()){
            errorinlocation = true;
        }
        }catch (Exception ex){System.out.println("error");}
        
        if (myproperties.getProperty("projectfolder").length() < 2 || errorinlocation){
           
            OutputStream out = null;
            try {
                String path = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
                String newpath = path+File.separator+File.separator+"concordia";
                System.out.println(newpath);
                projectdirectory = new File(newpath);
                projectdirectory.mkdirs();
                if (!projectdirectory.exists()){System.out.println("ERROR generating project directory at: "+newpath);}
                myproperties.setProperty("projectfolder", projectdirectory.getAbsolutePath());
                out = new FileOutputStream("concordia.properties");
                myproperties.store(out, "This is an optional header comment string");
                out.close();
            } catch (Exception ex) {
                Logger.getLogger(Filemanager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    out.close();
                } catch (IOException ex) {
                    Logger.getLogger(Filemanager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        blastresultmanager.setblastresultdirectory(new File(projectdirectory.getAbsolutePath()+File.separator+"blastresultdata"));
        annotationmanager.setAnnotationdirectory(new File(projectdirectory.getAbsolutePath()+File.separator+"annotation")); 
        updatelocationfiles();
    }
    
    /**
     *
     * @return
     */
    public File getProjectdirectory() {
        return projectdirectory;
    }

    /**
     *
     */
    public void updatelocationfiles(){
        // location files
        cazysources.clear();
        uniprotsources.clear();
        referencedatabasefiles.clear();
        referencedatabaselocations = new File(projectdirectory.getAbsolutePath()+File.separator+"referencedatabaselocations.txt");
        if (referencedatabaselocations.exists()){
            try {
                for (String line : Files.readAllLines(Paths.get(referencedatabaselocations.getAbsolutePath()))){
                    referencedatabasefiles.add(new File(line.replace("\n", "")));
                }     
            } catch (IOException ex) {Logger.getLogger(Filemanager.class.getName()).log(Level.SEVERE, null, ex);}
        } else {
            try {
                referencedatabaselocations.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(Filemanager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        cazyfilelocations = new File(projectdirectory.getAbsolutePath()+File.separator+"cazyfilelocations.txt");
        if (cazyfilelocations.exists()){
            try {
                for (String line : Files.readAllLines(Paths.get(cazyfilelocations.getAbsolutePath()))){
                    cazysources.add(new File(line.replace("\n", "")));
                }     
            } catch (IOException ex) {Logger.getLogger(Filemanager.class.getName()).log(Level.SEVERE, null, ex);}
        } else {
            try {
                cazyfilelocations.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(Filemanager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        uniprotfilelocations = new File(projectdirectory.getAbsolutePath()+File.separator+"uniprotfilelocations.txt");
        if (uniprotfilelocations.exists()){
            try {
                for (String line : Files.readAllLines(Paths.get(uniprotfilelocations.getAbsolutePath()))){
                    uniprotsources.add(new File(line.replace("\n", "")));
                }     
            } catch (IOException ex) {Logger.getLogger(Filemanager.class.getName()).log(Level.SEVERE, null, ex);}
        } else {
            try {
                uniprotfilelocations.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(Filemanager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     *
     * @param newcazylocationfiles
     */
    public void addcazyfilelocations(ArrayList<File> newcazylocationfiles){
        String outputstring = "";
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(cazyfilelocations.getAbsolutePath()));
        } catch (IOException ex) {
            Logger.getLogger(Filemanager.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (File file : newcazylocationfiles){
            try {
                writer.append(file.getAbsolutePath());
                writer.newLine();
            } catch (IOException ex) {
                Logger.getLogger(Filemanager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Filemanager.class.getName()).log(Level.SEVERE, null, ex);
        }
        updatelocationfiles();
    }
    
    /**
     *
     * @param newuniprotlocationfiles
     */
    public void adduniprotfilelocations(ArrayList<File> newuniprotlocationfiles){
        String outputstring = "";
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(uniprotfilelocations.getAbsolutePath()));
        } catch (IOException ex) {
            Logger.getLogger(Filemanager.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (File file : newuniprotlocationfiles){
            try {
                writer.append(file.getAbsolutePath());
                writer.newLine();
            } catch (IOException ex) {
                Logger.getLogger(Filemanager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Filemanager.class.getName()).log(Level.SEVERE, null, ex);
        }
        updatelocationfiles();
    }
    
    /**
     *
     * @param newreferencedatabaselocations
     */
    public void addreferencedatabaselocations(ArrayList<File> newreferencedatabaselocations){
        String outputstring = "";
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(referencedatabaselocations.getAbsolutePath()));
        } catch (IOException ex) {
            Logger.getLogger(Filemanager.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (File file : newreferencedatabaselocations){
            try {
                writer.append(file.getAbsolutePath());
                writer.newLine();
            } catch (IOException ex) {
                Logger.getLogger(Filemanager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Filemanager.class.getName()).log(Level.SEVERE, null, ex);
        }
        updatelocationfiles();
    }
    /**
     * @param projectdirectory the projectdirectory to set
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public void setProjectdirectory(File projectdirectory) throws FileNotFoundException, IOException {
        this.projectdirectory = projectdirectory;
        myproperties.setProperty("projectfolder", projectdirectory.getAbsolutePath());
        OutputStream out = new FileOutputStream("concordia.properties");
        myproperties.store(out, "This is an optional header comment string");
        blastresultmanager.setblastresultdirectory(new File(projectdirectory.getAbsolutePath()+File.separator+"blastresultdata"));
        annotationmanager.setAnnotationdirectory(new File(projectdirectory.getAbsolutePath()+File.separator+"annotation"));
        cazyfilelocations = new File(projectdirectory.getAbsolutePath()+File.separator+"cazyfilelocations.txt");
        uniprotfilelocations = new File(projectdirectory.getAbsolutePath()+File.separator+"uniprotfilelocations.txt");
        referencedatabaselocations = new File(projectdirectory.getAbsolutePath()+File.separator+"referencedatabaselocations.txt");
        updatelocationfiles();
        out.close();
    }  
}