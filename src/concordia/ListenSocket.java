/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil
 */
public class ListenSocket extends Thread{
    ServerSocket server;
    Integer port = 0;
    public ListenSocket(Integer port) throws IOException{
        this.port = port;
        this.server = new ServerSocket(port);
        System.out.println("ListenSocket$ Listening for requests on: "+server+" "+server.getLocalSocketAddress()+" "+server.getLocalPort());
    }
    
    public void run(){
        try {
            Socket client = server.accept();

            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(),true);
            while(true){
                try{
                    String line = in.readLine();
                    System.out.println("socketrequest: "+line);
                } catch (IOException e) {
                    System.out.println("Read failed");
                    System.exit(-1);
                }
            }   } catch (IOException ex) {
            Logger.getLogger(ListenSocket.class.getName()).log(Level.SEVERE, null, ex);
        }

    
    }
    
}
