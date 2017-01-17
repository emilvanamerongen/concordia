/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ChangeListener;
import javafx.util.Duration;

/**
 *
 * @author emilvanamerongen
 */
public class GUIController implements Initializable {
   //// datasets page
    @FXML
    TabPane filespane;
    @FXML
    ListView datasetlist;
    @FXML
    ListView annotationlist;
    @FXML
    Label ngsdatalabel;
    //buttons
    @FXML
    Button datasetdeletebutton;
    @FXML
    Button adddatabutton;
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
    
    //init
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //TODO - get data from database and load into datasetlist
        
    }    
    
}
