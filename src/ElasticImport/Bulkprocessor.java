/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ElasticImport;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

/**
 *
 * @author emil3
 */
public class Bulkprocessor {
    public static BulkProcessor bulkProcessor;
    
    
    
    public static void init(String adress, Integer port) throws UnknownHostException{
        TransportClient client = new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(adress), port));
            
        bulkProcessor = BulkProcessor.builder(
        client, new BulkProcessor.Listener() {
        @Override
        public void beforeBulk(long executionId,
                               BulkRequest request) {  } 

        @Override
        public void afterBulk(long executionId,
                              BulkRequest request,
                              BulkResponse response) {  } 

        @Override
        public void afterBulk(long executionId,
                              BulkRequest request,
                              Throwable failure) {  } 


        })
        .setBulkActions(10000) 
        .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB)) 
        .setFlushInterval(TimeValue.timeValueSeconds(5)) 
        .setConcurrentRequests(1) 
        .setBackoffPolicy(
            BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3)) 
        .build();
    }
}
