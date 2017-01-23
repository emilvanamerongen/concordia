/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
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
    private void setdirectory(ActionEvent event){
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Concordia project folder");
        File selectedDirectory = chooser.showDialog(directorybutton.getScene().getWindow());
    }
    //init
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //TODO - get data from database and load into datasetlist
        
    }    
    
}
