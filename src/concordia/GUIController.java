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
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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
import javax.swing.JFileChooser;

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
    
    File[] files;
    ArrayList<String> filenames = new ArrayList<>();
    Properties myproperties = new Properties();
    File selectedDirectory;
    
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
    
    @FXML
    private void setdirectory(ActionEvent event) throws FileNotFoundException, IOException{
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Concordia project folder");
        selectedDirectory = chooser.showDialog(directorybutton.getScene().getWindow());
        if (selectedDirectory != null){
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
    public void updatefilelist(){
        files = selectedDirectory.listFiles();
        for (File file : files){
            filenames.add(file.getName());
        }
        fileslist.setItems(FXCollections.observableArrayList(filenames));
        fileslist.setDisable(false);
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
            System.out.println("project folder not found..");
            fileslist.setItems(FXCollections.observableArrayList("project folder not found"));
            fileslist.setDisable(true);
        } else {
            updatefilelist();
        }
    }    
}
