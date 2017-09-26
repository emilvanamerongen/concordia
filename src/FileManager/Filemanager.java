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

    private File headerdirectory;
    private File indexdirectory;


    //properties file
    private Properties myproperties = new Properties();
    /**
     */
    public Filemanager(){
        boolean errorinlocation = false;
        try {
        myproperties.load(new FileInputStream("concordia.properties"));
        projectdirectory = new File(myproperties.getProperty("projectfolder"));
        headerdirectory = new File(myproperties.getProperty("projectfolder")+File.separator+"headers");
        indexdirectory = new File(myproperties.getProperty("projectfolder")+File.separator+"indexstorage");
        if (! projectdirectory.exists()){
            errorinlocation = true;
        } else if (!headerdirectory.exists() || !indexdirectory.exists()){
            headerdirectory.mkdir();
            indexdirectory.mkdir();
        }
        
        }catch (Exception ex){System.out.println("error");errorinlocation = true;}
        
        if (errorinlocation){
           
            OutputStream out = null;
            try {
                String path = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
                String newpath = path+File.separator+File.separator+"concordia";
                System.out.println(newpath);
                projectdirectory = new File(newpath);
                projectdirectory.mkdirs();
                headerdirectory = new File(newpath+File.separator+"headers");
                headerdirectory.mkdir();
                indexdirectory = new File(newpath+File.separator+"headers");
                indexdirectory.mkdir();
                
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

        out.close();
    }  

    /**
     * @return the headerdirectory
     */
    public File getHeaderdirectory() {
        return headerdirectory;
    }

    /**
     * @param headerdirectory the headerdirectory to set
     */
    public void setHeaderdirectory(File headerdirectory) {
        this.headerdirectory = headerdirectory;
    }

    /**
     * @return the indexdirectory
     */
    public File getIndexdirectory() {
        return indexdirectory;
    }

    /**
     * @param indexdirectory the indexdirectory to set
     */
    public void setIndexdirectory(File indexdirectory) {
        this.indexdirectory = indexdirectory;
    }
}