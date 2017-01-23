/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

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
    //// files
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
    @FXML
    Button annotationremovebutton;
    //add dataset tab
    @FXML
    TextField datasettitle;
    //add data tab
    @FXML
    TextArea addngstext;
    @FXML
    Label ngsfilelabel;
    @FXML 
    RadioButton fastaradiobutton;
    @FXML
    RadioButton fastqradiobutton;
    @FXML
    CheckBox directioncheckbox;
    @FXML
    ChoiceBox adddatachoicebox;
    @FXML
    TextField forwardfield;
    @FXML
    Label forwardtext;
    @FXML
    TextField reversefield;
    @FXML
    Label reversetext;
    @FXML
    TextField headidentifierfield;
    @FXML 
    ProgressBar adddataprogressbar;
    @FXML
    Label adddataprogresslabel;
    
    HashMap<String, File> files = new HashMap<>();
    ArrayList<String> filenames = new ArrayList<>();
    Properties myproperties = new Properties();
    File selectedDirectory;
    
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
        File newselectedDirectory = chooser.showDialog(directorybutton.getScene().getWindow());
        if (newselectedDirectory != null){
            selectedDirectory = newselectedDirectory;
        }      
        if (newselectedDirectory != null){
        myproperties.setProperty("projectfolder", selectedDirectory.getAbsolutePath());
        OutputStream out = new FileOutputStream("concordia.properties");
        myproperties.store(out, "This is an optional header comment string");
        out.close();
        updatefilelist();
        }
    }
    @FXML
    private void openexplorer(ActionEvent event) throws IOException{
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("nautilus "+selectedDirectory.getAbsolutePath());
    }
    @FXML
    public void updatefilelist(){
        files.clear();
        for (File file : selectedDirectory.listFiles()){
            files.put(file.getName(), file);
        }
        filenames.clear();
        for (File file : files.values()){
            filenames.add(file.getName());
        }
        addfilesbutton.setDisable(false);
        deletefilebutton.setDisable(false);
        fileslist.setItems(FXCollections.observableArrayList(filenames));
        fileslist.setDisable(false);
    }
    @FXML
    public void addfiles(ActionEvent event){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Concordia project folder");
        List<File> newfiles = chooser.showOpenMultipleDialog(directorybutton.getScene().getWindow());
        if (newfiles != null){
            for (File file : newfiles){
                file.renameTo(new File(selectedDirectory.getAbsolutePath()+File.separator+file.getName()));
            }
            updatefilelist();
        }      
    }
    @FXML
    public void deletefile(){
        ObservableList selectedfile = fileslist.getSelectionModel().getSelectedItems();
        try{
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete file");
        alert.setHeaderText(selectedfile.get(0)+" wil be permanently deleted");
        alert.setContentText("Are you sure want to permanently delete this file?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            File markedfordelete = files.get(selectedfile.get(0));
            Files.delete(markedfordelete.toPath());
            updatefilelist();
        } else {
            System.out.println("deletion canceled");
        }
        } catch (Exception ex){
            System.out.println("no files deleted");
        }
    }
    //init
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            //TODO - get data from database and load into datasetlist
            myproperties.load(new FileInputStream("concordia.properties"));
            selectedDirectory = new File(myproperties.getProperty("projectfolder"));
        } catch (Exception ex) {}
        System.out.println(myproperties.getProperty("projectfolder"));
        if (myproperties.getProperty("projectfolder").length() < 2){
            fileslist.setItems(FXCollections.observableArrayList("project folder not found"));
            fileslist.setDisable(true);
        } else {
            updatefilelist();
        }
    }    
}
