/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import Objects.dataset;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;

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
    //add dataset tab
    @FXML
    TextField datasettitle;
    Button datasetaddbutton;
    
    
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
        datasets.put(datasettitle.getText(), new dataset());
        updategui();
    }
    
    public void updategui(){
        ObservableList<String> datasetlistitems =FXCollections.observableArrayList(datasets.keySet());
        datasetlist.setItems(datasetlistitems);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    
    
}
