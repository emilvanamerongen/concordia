/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import concordia.Concordia;
import concordia.remoteGUIController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil3
 */
public class InputStream extends Thread{
    private Socket connectionsocket;
    private Boolean active = true;
    private Boolean connectionestablished = false;
    private remoteGUIController controller;
    private Boolean tested = false;
    
    public InputStream(Socket connectionsocket, remoteGUIController controller){
        this.connectionsocket = connectionsocket;
        try {
            //test
            BufferedReader in = new BufferedReader(new InputStreamReader(connectionsocket.getInputStream()));
            System.out.println(in.readLine());
        } catch (IOException ex) {
            Logger.getLogger(InputStream.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.controller = controller;
    }

    public InputStream() {
        
    }
    
    @Override
    public void run(){
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(connectionsocket.getInputStream()));
            while (active){
                String line = in.readLine();
                System.out.println(line);
                if (!tested){
                    controller.setConnected(true);
                    tested = true;
                }
            }
        } catch (IOException ex) {
            System.out.println("unable to recieve data");
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                System.out.println("unable to recieve data");
            }
        }
    }
    

            
}
