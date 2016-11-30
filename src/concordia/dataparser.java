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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
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
   private String importstring;
   private String filetype;
   
   public dataparser(File importfile, String filetype){
       this.importfile = importfile;
       this.filetype = filetype;
   }
   public dataparser(String importstring, String filetype){
       this.importstring = importstring;
       this.filetype = filetype;
   }
   
   public void process(String headidentifier, String forwardindicator, String reverseindicator) throws IOException{
       processthread thread = new processthread(headidentifier, forwardindicator, reverseindicator);
       thread.start();

   }
    public class processthread extends Thread {
    private String headidentifier;
    private String forwardindicator;
    private String reverseindicator;
    public processthread(String headidentifier, String forwardindicator, String reverseindicator) {
        this.headidentifier = headidentifier;
        this.forwardindicator = forwardindicator;
        this.reverseindicator = reverseindicator;
    }

    public void run(){
       System.out.println("processing data on new thread");
       int seqnumber = 0;
       if (importfile != null){
           System.out.println("File found");
           FileInputStream inputStream = null;
           try {
               inputStream = new FileInputStream(importfile.getAbsolutePath());
           } catch (FileNotFoundException ex) {
               Logger.getLogger(dataparser.class.getName()).log(Level.SEVERE, null, ex);
           }
            Scanner sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.matches("^@.*")){
                seqnumber += 1;   
                
            }
            }
       }
       else if (importstring != null){
           System.out.println("text found");
           
       }
       System.out.println("seq:"+seqnumber);
        
    }
    }
}

