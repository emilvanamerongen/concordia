/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import Objects.dataset;
import Objects.loadbar;
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
    TabPane datasetstabpane;
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
    

            
    //parser variables
    Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1000), ae -> parserloop()));
    public static Boolean timelineactive = true;
    public static String parserloadlabel;
    public static loadbar parserloadbar = new loadbar();
    public static String parserexample1;
    public static String parserexample2;
    public File importfiletemp; 
    
    
    
    HashMap<String, dataset> datasets = new HashMap<>();
    CreateDatabase newdatabase = new CreateDatabase();
    
    @FXML
    private void databaseButton(ActionEvent event) throws SQLException{
        newdatabase.createDataTables();
    }
    
    @FXML
    private void datasetswitchtab(ActionEvent event){
        String source = event.getSource().toString();
        if (source.contains("new")){
            datasetstabpane.getSelectionModel().select(1);
        }
        else  if (source.contains("add")){
            datasetstabpane.getSelectionModel().select(2);
        }
        else if (source.contains("")){
            
        }
        else if (source.contains("")){
            
        }
    }
    
    @FXML
    private void adddataset(ActionEvent event){
        String newsettitle = datasettitle.getText();
        if (!"".equals(newsettitle)){
            datasets.put(newsettitle, new dataset());
            datasettitle.setText("");
            updateguilistviews();
            updateguibuttons();
            datasetstabpane.getSelectionModel().select(0);
        }
    }   
    @FXML
    private void deletedataset(ActionEvent event){
        String removetitle = (String) datasetlist.getSelectionModel().getSelectedItem();
        datasets.remove(removetitle);
        updateguilistviews();
        updateguibuttons();
        datasetstabpane.getSelectionModel().select(0);
    } 
    public void updateguilistviews(){
        ObservableList<String> datasetlistitems =FXCollections.observableArrayList(datasets.keySet());
        datasetlist.setItems(datasetlistitems);
    }
    @FXML
    public void updateguibuttons(){
        if (datasetlist.getItems().size() == 0){
            datasetdeletebutton.setDisable(true);
            adddatabutton.setDisable(true);
            blastbutton.setDisable(true);
            annotationaddbutton.setDisable(true);
            annotationremovebutton.setDisable(true);
        }   else {
            datasetdeletebutton.setDisable(false);
        }
        if (datasetlist.getSelectionModel().getSelectedItem() != null){
            adddatabutton.setDisable(false);
        } else {
            adddatabutton.setDisable(true);
        }
        updateguilabels();
    }
    @FXML
    public void updateguilabels(){
        ngsdatalabel.setText((String) datasetlist.getSelectionModel().getSelectedItem());
    }
    
    // add data functions:
    @FXML
    public void openNGSfile(ActionEvent event) throws IOException{
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open NGS data File");
                    fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("FASTQ", "*.fastq"),
                new FileChooser.ExtensionFilter("FASTA", "*.fasta")
            );
        importfiletemp = fileChooser.showOpenDialog(stage);
        if (importfiletemp != null){
        ngsfilelabel.setText(importfiletemp.getName());
        }
    }
    @FXML
    public void adddata(ActionEvent event) throws IOException{
        String[] ngstext = addngstext.getText().split("\\r?\\n");
        String selecteddataset = (String) datasetlist.getSelectionModel().getSelectedItem();   
        System.out.println(adddatachoicebox.getValue());
        if (! ngstext[0].isEmpty()){
            System.out.println("PARSER MODULE: GO text");
            dataparser newparser = new dataparser(ngstext, "");
            newparser.process(headidentifierfield.getText(),forwardfield.getText(),reversefield.getText(),selecteddataset,!directioncheckbox.isSelected(), "MANUAL INPUT");
            
        }
        else if (importfiletemp != null){
            System.out.println("PARSER MODULE: GO file");
            dataparser newparser = new dataparser(importfiletemp, "");
            newparser.process(headidentifierfield.getText(),forwardfield.getText(),reversefield.getText(),selecteddataset,!directioncheckbox.isSelected(),importfiletemp.getName());
        } else {
            return;
        }
        adddataprogresslabel.setText("importing");
        parserloadbar.doneproperty().addListener(new ChangeListener(){
        @Override public void changed(ObservableValue o,Object oldVal, Object newVal){
            double progress = parserloadbar.getdone();
            adddataprogressbar.setProgress(parserloadbar.getdone());
        }
        });
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        datasetstabpane.getSelectionModel().select(3);
    }      
    
    @FXML 
    public void readdirectiondisable(ActionEvent event){
        if (directioncheckbox.isSelected()){
            forwardfield.setDisable(false);
            reversefield.setDisable(false);
            forwardtext.setDisable(false);
            reversetext.setDisable(false);
        } else{
            forwardfield.setDisable(true);
            reversefield.setDisable(true);
            forwardtext.setDisable(true);
            reversetext.setDisable(true);
        }
    }
    
    public void parserloop(){
        adddataprogresslabel.setText(parserloadlabel);
        if (timelineactive == false){
            adddataprogresslabel.setText("import complete");
            datasetstabpane.getSelectionModel().select(0);
            System.out.println("parse complete");
            parserloadlabel = "";
            adddataprogresslabel.setText(parserloadlabel);
            timeline.stop();
            ngsfilelabel.setText("");
            importfiletemp = null;
        }
    }
    //init
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //TODO - get data from database and load into datasetlist
        adddatachoicebox.setItems(FXCollections.observableArrayList("FASTq", "FASTA"));
        adddatachoicebox.getSelectionModel().selectFirst();
        
    }    
    
}
