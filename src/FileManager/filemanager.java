/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author emil
 */
public class filemanager {
    private File projectdirectory;
    public ngsmanager ngsmanager = new ngsmanager();
    public annotationmanager annotationmanager = new annotationmanager();
    //databases
    private File databaselocationfile;
    private HashMap<String, File> ngsfiles = new HashMap<>();
    private ArrayList<String> filenames = new ArrayList<>();
    //properties file
    private Properties myproperties = new Properties();
    /**
     * @return the projectdirectory
     */
    public filemanager(){
        try {
        myproperties.load(new FileInputStream("concordia.properties"));
        projectdirectory = new File(myproperties.getProperty("projectfolder"));}
        catch (Exception ex){}
        if (myproperties.getProperty("projectfolder").length() < 2){
            OutputStream out = null;
            try {
                String path = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
                projectdirectory = new File(path+File.separator+"Documents"+File.separator+"concordia");
                projectdirectory.mkdir();
                myproperties.setProperty("projectfolder", projectdirectory.getAbsolutePath());
                out = new FileOutputStream("concordia.properties");
                myproperties.store(out, "This is an optional header comment string");
                out.close();
            } catch (Exception ex) {
                Logger.getLogger(filemanager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    out.close();
                } catch (IOException ex) {
                    Logger.getLogger(filemanager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        databaselocationfile = new File(projectdirectory.getAbsolutePath()+File.separator+"databaselocations.txt");
        ngsmanager.setNgsdirectory(new File(projectdirectory.getAbsolutePath()+File.separator+"NGSdata"));
        annotationmanager.setAnnotationdirectory(new File(projectdirectory.getAbsolutePath()+File.separator+"annotation")); 
        // read database location file
        try{
           databaselocationfile.setReadable(true);
           
        } catch (Exception ex){
            
        }
        ngsmanager.updatefiles();
        annotationmanager.updatefiles();
    }
    
    public File getProjectdirectory() {
        return projectdirectory;
    }

    /**
     * @param projectdirectory the projectdirectory to set
     */
    public void setProjectdirectory(File projectdirectory) throws FileNotFoundException, IOException {
        this.projectdirectory = projectdirectory;
        myproperties.setProperty("projectfolder", projectdirectory.getAbsolutePath());
        OutputStream out = new FileOutputStream("concordia.properties");
        myproperties.store(out, "This is an optional header comment string");
        ngsmanager.setNgsdirectory(new File(projectdirectory.getAbsolutePath()+File.separator+"NGSdata"));
        annotationmanager.setAnnotationdirectory(new File(projectdirectory.getAbsolutePath()+File.separator+"annotation"));
        databaselocationfile = new File(projectdirectory.getAbsolutePath()+File.separator+"databaselocations.txt");
        out.close();
    }  
}