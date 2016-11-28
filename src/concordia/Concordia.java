/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import static com.sun.org.apache.xalan.internal.lib.ExsltDatetime.time;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author emilvanamerongen
 */
public class Concordia extends Application {
    public static Stage loadingstage = new Stage(StageStyle.TRANSPARENT);
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent loadingroot = FXMLLoader.load(getClass().getResource("loadingGUI.fxml"));        
        Scene loadingscene = new Scene(loadingroot); 
        loadingscene.setFill(Color.TRANSPARENT);
        loadingstage.setScene(loadingscene);
       
        Parent root = FXMLLoader.load(getClass().getResource("GUI.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.getIcons().add( new Image( Concordia.class.getResourceAsStream( "logo-concordia.png" )));
        stage.show();
        loadingstage.show();
        loadingstage.close();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
