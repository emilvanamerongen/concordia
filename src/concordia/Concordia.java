/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import java.io.File;
import java.util.HashMap;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.ConcordiaServer;

/**
 *
 * @author emilvanamerongen
 */

public class Concordia extends Application {
    
    static Boolean server = false;
    static Boolean remote = false;
    public static Boolean serveractive = false;
    
            
    @Override
    public void start(Stage stage) throws Exception {
        Thread.currentThread().setName("CONCORDIA");
        System.out.println("-------------------------");
        if (!server && !remote){
            System.out.println("STARTING CONCORDIA IN LOCAL MODE");
            Parent root = FXMLLoader.load(Concordia.class.getResource("localGUI.fxml"));   
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } else if (remote){
            System.out.println("STARTING CONCORDIA IN REMOTE MODE");
            Parent root = FXMLLoader.load(Concordia.class.getResource("remoteGUI.fxml"));   
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } 
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // check command line arguments and set server/remote booleans accordingly
        try{
            if (args[0].contains("SERVER")||args[0].contains("server")){
                server = true;
            } else if (args[0].contains("REMOTE")||args[0].contains("remote")){
                remote = true;
            }
        } catch (Exception ex){}
        if (server){
            System.out.println("STARTING CONCORDIA IN SERVER MODE");
            serveractive = true;
            ConcordiaServer concordiaserverthread = new ConcordiaServer();
            concordiaserverthread.start();
        } else{
            launch(args);
        }
        
    }
    
}
