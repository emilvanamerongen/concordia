/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ElasticImport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import server.ConcordiaServer;




/**
 *
 * @author emil3
 */
public class ElasticIndexGenerator{


    
    public ElasticIndexGenerator(){

    }
            
    public void generateindex(LinkedHashMap<String,Boolean> map, String indexname, RestHighLevelClient client){
        try {
            CreateIndexRequest request = new CreateIndexRequest(indexname);
            request.settings(Settings.builder()
                    .put("index.number_of_shards", 6)
                    .put("index.number_of_replicas", 0));
            CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
                    
                    
                    
                } catch (Exception ex) {
            System.out.println("index already exists");
        }
        
        
    }
        
        
//        try {
//            
//            
//            String curl = "curl -XPUT ";
//            curl+= "\'"+ConcordiaServer.serverproperties.getElasticIP()+":"+ConcordiaServer.serverproperties.getElasticPORT()+"/"+indexname.toLowerCase();
//            curl+= "\' -H \'Content-Type: application/json\'";
//            curl+= " -d\'{\n\"settings\" : {\n\"number_of_shards\" : 1},\n\"mappings\" : {\n\"entry\" : {\n\"properties\" : {\n";
//            for (String item : map.keySet()){
//                if (!map.get(item)){
//                    curl+= "\""+item+"\" : {\"type\": \"keyword\"},\n";
//                } else {
//                    curl+= "\""+item+"\" : {\"type\": \"text\"},\n";
//                }
//            }
//            curl = curl.substring(0,curl.length()-2)+"\n";
//            curl += "}}}}}\'";
//            //System.out.println(curl);
//                    
//
//            
//            Process p = new ProcessBuilder(
//            "/bin/sh", 
//            "-c", 
//            curl).start();
//            
//            p.waitFor();
//            
//            BufferedReader reader =
//                    new BufferedReader(new InputStreamReader(p.getInputStream()));
//            
//            String line = "";
//            while ((line = reader.readLine())!= null) {
//                System.out.println(line);
//            }
//            BufferedReader reader2 =
//                    new BufferedReader(new InputStreamReader(p.getErrorStream()));
//            
//            String line2 = "";
//            while ((line2 = reader2.readLine())!= null) {
//                System.out.println(line2);
//            }
//        } catch (IOException | InterruptedException ex) {
//            System.out.println(ex.toString().substring(0,50));
//        }
//                


    

    

}
