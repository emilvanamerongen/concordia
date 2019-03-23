/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import concordia.remoteGUIController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil
 */
public class ServerConnection extends Thread{
    public String serverip = "";
    public InputStream inputstream =  new InputStream();
    public Boolean active = true;
    public PrintWriter outputwriter;
    
    public ServerConnection(){
        
    }

    public void connect(String selectedserver, remoteGUIController controller){
       this.serverip = selectedserver;
        ObjectOutputStream oos = null;
        try {
            System.out.println("ServerConnect thread$ attempting connection ("+serverip+")");
            String[] serveripsplit = serverip.split(":");

            Socket socket = new Socket(serveripsplit[0],Integer.parseInt(serveripsplit[1]));

            outputwriter = new PrintWriter(socket.getOutputStream(), true);
            inputstream = new InputStream(socket,controller);
            inputstream.start();
            outputwriter.println("hello");
            outputwriter.flush();
            System.out.println("connection test");
            outputwriter.println("REMOTE");

            
            outputwriter.flush();
            //input 
            
            
        } catch (IOException ex) {
            System.out.println("connection test failed");
        }
        
        
    }
    
    
    
    public void run(){
        
    }

   
}
