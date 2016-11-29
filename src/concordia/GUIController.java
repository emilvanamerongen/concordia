/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import Objects.dataset;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
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
import concordia.dbcon;
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
    
    
    
    public File importfiletemp;
    
    HashMap<String, dataset> datasets = new HashMap<>();
    
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
        if (addngstext.getText() == null){
            dbcon con = new dbcon();
        }
        else if (importfiletemp != null){
            
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //TODO - get data from database and load into datasetlist
    }    
    
}
