/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;
/**
parcing the info from the GUI to be used by other modules
@author theox
*/
import Objects.loadbar;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class dataparser {
/**
*Used to open given file name and extract the sequences and sequence headers
*@param file the location of the file that has to be opened
*@return returns a nested array with all the data in it for the database
**/
   private File importfile;
   private String[] importstring;
   private String filetype;
   
   public dataparser(File importfile, String filetype){
       this.importfile = importfile;
       this.filetype = filetype;
   }
   public dataparser(String[] importstring, String filetype){
       this.importstring = importstring;
       this.filetype = filetype;
   }
   
   public void process(String headidentifier, String forwardindicator, String reverseindicator, String selecteddataset) throws IOException{
       processthread thread = new processthread(headidentifier, forwardindicator, reverseindicator, selecteddataset);
       thread.start();

   }
    public class processthread extends Thread {
    //parameters
    private String headidentifier;
    private String forwardindicator;
    private String reverseindicator;
    //data
    private int seqnumber;
    private double done;
    private double total;
    DecimalFormat df = new DecimalFormat("#.##");
    public processthread(String headidentifier, String forwardindicator, String reverseindicator, String selecteddataset) {
        this.headidentifier = headidentifier;
        this.forwardindicator = forwardindicator;
        this.reverseindicator = reverseindicator;
    }

    public void run(){
       System.out.println("processing data on new thread");      
       System.out.println("##################");
       System.out.println("parameters:");
       System.out.println("head identifier: "+headidentifier);
       System.out.println("forward indicator: "+forwardindicator);
       System.out.println("reverse indicator: "+reverseindicator);
       System.out.println("##################");
       
       if (importfile != null){
            System.out.println("File found");
           
            FileInputStream inputStream = null;
            try {
               inputStream = new FileInputStream(importfile.getAbsolutePath());
            } catch (FileNotFoundException ex) {
               Logger.getLogger(dataparser.class.getName()).log(Level.SEVERE, null, ex);
            }
            Scanner sc = new Scanner(inputStream, "UTF-8");
            System.out.println(total);
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine()) {
            sc.nextLine();
            total+=1;
            }
            sc.close();
            total = total/4;
            inputStream = null;
            try {
               inputStream = new FileInputStream(importfile.getAbsolutePath());
            } catch (FileNotFoundException ex) {
               Logger.getLogger(dataparser.class.getName()).log(Level.SEVERE, null, ex);
            }
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine()) {
            String line = sc.nextLine();
               
                processline(line);             
                String format = df.format(done/total);
                GUIController.parserloadbar.setdone(Double.parseDouble(format));
            }
            try {
               inputStream.close();
            } catch (IOException ex) {
               Logger.getLogger(dataparser.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("parser done");
           
       }
       else if (importstring != null){
            System.out.println("text found");
           
            for (String line : importstring){
               if (line.matches("^"+headidentifier+".*")){
                total+=1;
            }
            
               
            }
            for (String line : importstring){
                processline(line);
               
               
            }
           
       }
       
        
    }
    
   

    
    public void processline(String line){
        
        if (line.matches("^"+headidentifier+".*")){
                done += 1;
            }
    }
    }
}

