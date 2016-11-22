/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;

/**
 *
 * @author emilvanamerongen
 */
public class GUIController implements Initializable {
   
    @FXML
    TabPane datasetstabpane;
    
    @FXML
    private void datasetswitchtab(ActionEvent event){
        String source = event.getSource().toString();
        if (source.contains("new")){
            datasetstabpane.getSelectionModel().select(1);
        }
        else if (source.contains("add")){
            datasetstabpane.getSelectionModel().select(2);
        }
        else if (source.contains("")){
        
        }
        else if (source.contains("")){
        
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
