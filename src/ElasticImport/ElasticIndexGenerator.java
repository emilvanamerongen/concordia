/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ElasticImport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;

import org.elasticsearch.transport.client.PreBuiltTransportClient;
import server.ConcordiaServer;




/**
 *
 * @author emil3
 */
public class ElasticIndexGenerator{


    
    public ElasticIndexGenerator(){

    }
            
    public void generateindex(LinkedHashMap<String,Boolean> map, String indexname){
        try {
            String curl = "curl -XPUT ";
            curl+= "\'"+ConcordiaServer.serverproperties.getElasticIP()+":"+ConcordiaServer.serverproperties.getElasticRESTPORT()+"/"+indexname.toLowerCase();
            curl+= "\' -H \'Content-Type: application/json\'";
            curl+= " -d\'{\n\"settings\" : {\n\"number_of_shards\" : 1},\n\"mappings\" : {\n\"entry\" : {\n\"properties\" : {\n";
            for (String item : map.keySet()){
                if (!map.get(item)){
                    curl+= "\""+item+"\" : {\"type\": \"keyword\"},\n";
                } else {
                    curl+= "\""+item+"\" : {\"type\": \"text\"},\n";
                }
            }
            curl = curl.substring(0,curl.length()-2)+"\n";
            curl += "}}}}}\'";
            //System.out.println(curl);
                    

            
            Process p = new ProcessBuilder(
            "/bin/sh", 
            "-c", 
            curl).start();
            
            p.waitFor();
            
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            
            String line = "";
            while ((line = reader.readLine())!= null) {
                System.out.println(line);
            }
            BufferedReader reader2 =
                    new BufferedReader(new InputStreamReader(p.getErrorStream()));
            
            String line2 = "";
            while ((line2 = reader2.readLine())!= null) {
                System.out.println(line2);
            }
        } catch (IOException | InterruptedException ex) {
            System.out.println(ex.toString().substring(0,50));
        }
                


    }

    

}
