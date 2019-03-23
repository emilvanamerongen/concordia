/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ElasticImport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author emil3
 */
public class JsonWriteThread extends Thread{
    private File outputfile;
    private ConcurrentLinkedQueue<JSONObject> complete = new ConcurrentLinkedQueue();
    public Boolean active = true;
    public FileWriter outputstream = null;
    
    public JsonWriteThread(File outputfile, ConcurrentLinkedQueue<JSONObject> complete){
        this.outputfile = outputfile;
        this.complete = complete;
        try {
            outputstream = new FileWriter(outputfile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JsonWriteThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JsonWriteThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void run(){
         while (active){
            if (!complete.isEmpty()){
            JSONObject request = complete.poll();
            if (request == null){ 
            } else {
                
                
            
            
                try {                    
                    request.write(outputstream,1,1);              
                    outputstream.flush();
                } catch (IOException ex) {
                    Logger.getLogger(JsonWriteThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            }
        }
    }
    
    
}
