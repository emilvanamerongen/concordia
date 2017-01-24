/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import filemanager.filemanager;
import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author emilvanamerongen
 */
public class GUIController implements Initializable {
    // files
    @FXML
    TabPane filespane;
    @FXML
    ListView fileslist;
    @FXML
    ListView annotationlist;
    @FXML
    Button directorybutton;
    @FXML
    Label ngsdatalabel;
    //buttons
    @FXML
    Button deletefilebutton;
    @FXML
    Button addfilesbutton;
    @FXML
    Button blastbutton;
    @FXML
    Button annotationaddbutton;
    
    Properties myproperties = new Properties();
    filemanager filemanager = new filemanager();
    
    //gui manager
    @FXML
    private void switchtab(ActionEvent event){
        String source = event.getSource().toString();
        if (source.contains("new")){
            filespane.getSelectionModel().select(1);
        }
        else  if (source.contains("add")){
            filespane.getSelectionModel().select(2);
        }
        else if (source.contains("")){
            
        }
        else if (source.contains("")){
            
        }
    }  
    // file manager
    @FXML
    private void setdirectory(ActionEvent event) throws FileNotFoundException, IOException{
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Concordia project folder");
        File newselectedDirectory = chooser.showDialog(filespane.getScene().getWindow());
        if (newselectedDirectory != null){
            filemanager.setProjectdirectory(newselectedDirectory);
        }      
        if (newselectedDirectory != null){
        myproperties.setProperty("projectfolder", filemanager.getProjectdirectory().getAbsolutePath());
        OutputStream out = new FileOutputStream("concordia.properties");
        myproperties.store(out, "This is an optional header comment string");
        out.close();
        filemanager.annotationmanager.updatefiles();
        filemanager.ngsmanager.updatefiles();
        }
    }
    @FXML
    private void openexplorer(ActionEvent event) throws IOException{
        Runtime rt = Runtime.getRuntime();
        try {
            Process pr = rt.exec("nautilus "+filemanager.getProjectdirectory().getAbsolutePath());
        } catch (Exception ex){
            Process pr = rt.exec("explorer "+filemanager.getProjectdirectory().getAbsolutePath());
        }
    }
    @FXML
    public void updatengsfilelist(){
        fileslist.setItems(FXCollections.observableArrayList(filemanager.ngsmanager.getFilenames()));
        fileslist.setDisable(false);
    }
    @FXML
    public void updateannotationfilelist(){
        fileslist.setItems(FXCollections.observableArrayList(filemanager.annotationmanager.getFilenames()));
        fileslist.setDisable(false);
    }  
    @FXML
    public void addngsfiles(ActionEvent event){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("move files to ngs data in project folder");
        List<File> newfiles = chooser.showOpenMultipleDialog(filespane.getScene().getWindow());
        if (newfiles != null){
            for (File file : newfiles){
                file.renameTo(new File(filemanager.ngsmanager.getNgsdirectory().getAbsolutePath()+File.separator+file.getName()));
            }
            filemanager.ngsmanager.updatefiles();
        }
        updatengsfilelist();
    }
    @FXML
    public void addannotationfiles(ActionEvent event){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("move files to annotation in project folder");
        List<File> newfiles = chooser.showOpenMultipleDialog(filespane.getScene().getWindow());
        if (newfiles != null){
            for (File file : newfiles){
                file.renameTo(new File(filemanager.annotationmanager.getAnnotationdirectory().getAbsolutePath()+File.separator+file.getName()));
            }
            filemanager.annotationmanager.updatefiles();
        }
        updateannotationfilelist();
    }
    @FXML
    public void deletengsfile(){
        ObservableList selectedfile = fileslist.getSelectionModel().getSelectedItems();
        try{
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete file");
        alert.setHeaderText(selectedfile.get(0)+" wil be permanently deleted");
        alert.setContentText("Are you sure want to permanently delete this file?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            File markedfordelete = filemanager.ngsmanager.getNgsfiles().get(selectedfile.get(0));
            Files.delete(markedfordelete.toPath());
            filemanager.ngsmanager.updatefiles();
        } else {
            System.out.println("deletion canceled");
        }
        } catch (Exception ex){
            System.out.println("no files deleted");
        }
        updatengsfilelist();
    }
    @FXML
    public void deleteannotationfile(){
        ObservableList selectedfile = fileslist.getSelectionModel().getSelectedItems();
        try{
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete file");
        alert.setHeaderText(selectedfile.get(0)+" wil be permanently deleted");
        alert.setContentText("Are you sure want to permanently delete this file?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            File markedfordelete = filemanager.annotationmanager.getAnnotationfiles().get(selectedfile.get(0));
            Files.delete(markedfordelete.toPath());
            filemanager.annotationmanager.updatefiles();
        } else {
            System.out.println("deletion canceled");
        }
        } catch (Exception ex){
            System.out.println("no files deleted");
        }
        updateannotationfilelist();
    }
    
    //init
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    
}
