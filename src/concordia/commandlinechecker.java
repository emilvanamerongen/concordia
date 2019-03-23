/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import ElasticImport.Parser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Emil
 */
public class commandlinechecker extends Thread {
    public Boolean active = true;
    Scanner reader = new Scanner(System.in); 
    private GUIController controller;
    
    public commandlinechecker(GUIController controller){
        this.controller = controller;
    }
    
    @Override
    public void run(){
        Thread.currentThread().setName("InputListener");
        while (active){
            String nextLine = reader.nextLine();
            if (!nextLine.isEmpty()){
                if (nextLine.contains("pause")){
                    controller.parser.pause = true;
                    active = false;
                    break;
                } else if (nextLine.contains("help")){
                    System.out.println("use pause to stop import");
                } else if (nextLine.contains("queuestatus")){
                    Parser parser = controller.parser;
                    System.out.println("queue: "+parser.queue.size());
                    System.out.println("complete: "+parser.complete.size());
                    System.out.println("rawdata: "+parser.rawdataqueue.size());
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(commandlinechecker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
