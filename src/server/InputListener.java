/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil3
 */
public class InputListener extends Thread{
    public Boolean active = true;
    Scanner reader = new Scanner(System.in); 
    
    public InputListener(){
        
    }
    
    @Override
    public void run(){
        Thread.currentThread().setName("InputListener");
        while (active){
            String nextLine = reader.nextLine();
            if (!nextLine.isEmpty()){
                ConcordiaServer.queue.add(nextLine);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(InputListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
