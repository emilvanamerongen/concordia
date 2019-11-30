/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ElasticImport;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

/**
 *
 * @author emil3
 */
public class ElasticImportThread extends Thread{
    public ArrayBlockingQueue<LinkedHashMap<String, ArrayList<String>>> complete = new ArrayBlockingQueue<>(100, true);
    public Boolean active = true;
    public FileWriter outputstream = null;
    private Integer elasticport = 9200;
    private String indexname;
    private Integer id = 0;
    private RestHighLevelClient client;
    private Parser parent;
    
    public ElasticImportThread(ArrayBlockingQueue<LinkedHashMap<String, ArrayList<String>>> complete, String indexname, RestHighLevelClient client, Parser parent){
        this.complete = complete;
        this.indexname = indexname;
        this.client = client;
        this.parent = parent;
    }

    
    public void run(){
        LinkedHashMap<String, ArrayList<String>> poll = null;
                
        while (active){
            try {
                poll = complete.poll(100L,TimeUnit.SECONDS);
                if (!poll.isEmpty()){
                    Boolean done = false;
                    while (!done){
                    try {
                        importdata(poll,client);
                        done = true;
                    } catch (IOException ex) {
                        System.out.println("EX:"+ex);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ex1) {
                            Logger.getLogger(ElasticImportThread.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                    }}
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Rawdataprocesthread.class.getName()).log(Level.SEVERE, null, ex);
            }   
            
        }  
        
        
    
    }
    
    public void importdata(LinkedHashMap<String, ArrayList<String>> request, RestHighLevelClient client) throws java.io.IOException{
        id ++;
        Map<String, Object> json = new HashMap<>();
        for (String item : request.keySet()){
            ArrayList<String> data = request.get(item);
            String datastring = "";
            if (data.isEmpty()){
                //json.put(item,null);
            } else if (data.size() == 1){
                json.put(item,data.get(0));
            } else {
                json.put(item,data);
            }
            
        }
        
//            System.out.println("-----------------------------------");
//        System.out.println(curl);
        Parser.bulkProcessor.add(new IndexRequest(indexname.toLowerCase()).source(json, XContentType.JSON));

//        Process p = new ProcessBuilder(
//                "/bin/sh",
//                "-c",
//                curl).start();
//        try {
//            p.waitFor();
//
//            BufferedReader reader =
//                    new BufferedReader(new InputStreamReader(p.getInputStream()));
//            
//            String line = "";
//            while ((line = reader.readLine())!= null) {
//                //System.out.println(line);
//            }
//            BufferedReader reader2 =
//                    new BufferedReader(new InputStreamReader(p.getErrorStream()));
//            
//            String line2 = "";
//            Boolean error = false;
//            while ((line2 = reader2.readLine())!= null) {
//                if (line2.startsWith("curl:")){
//                    System.out.println(line2);
//                    error = true;
//                }
//                
//            }
//            if (error){
//                System.out.println(curl);
//            }
//        } catch (InterruptedException ex) {
//            Logger.getLogger(ElasticImportThread.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        p.destroy();

        
    }
    
    ActionListener<BulkResponse> listener = new ActionListener<BulkResponse>() {
    @Override
    public void onResponse(BulkResponse bulkResponse) {
        
    }

    @Override
    public void onFailure(Exception e) {
        
    }
};
}
