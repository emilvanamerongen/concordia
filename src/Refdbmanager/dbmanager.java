/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Refdbmanager;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;


/**
 *
 * @author emil3
 */
public class dbmanager {


    private File locationstorage;
    private ObservableList<refdb> referencedatabases = FXCollections.observableArrayList();
    RestHighLevelClient elasticsearchclient = null;
    
    public dbmanager(RestHighLevelClient elasticsearchclient){
        this.elasticsearchclient = elasticsearchclient;
        if (elasticsearchclient != null){
            read();
        }
    }
    
    public void read(){
        if (elasticsearchclient == null){
            return;
        }
        try{
        referencedatabases.clear(); 
            GetIndexRequest request = new GetIndexRequest("*");
            GetIndexResponse getIndexResponse = elasticsearchclient.indices().get(request, RequestOptions.DEFAULT);         

        Integer index = 1;
        for (String indexname : getIndexResponse.getIndices()){
            String indexnamestring = indexname;
//            get number of documents
            CountRequest countRequest = new CountRequest(indexname); 

            CountResponse countresponse = elasticsearchclient.count(countRequest, RequestOptions.DEFAULT);
            Long totalHits = countresponse.getCount();
            if (!indexnamestring.startsWith(".")){
                referencedatabases.add(new refdb(index,indexnamestring,""+totalHits,elasticsearchclient));
                index ++; 
            }
        }
//        
//        locationstorage = new File("locationstorage.txt");
//        System.out.println("loading saved reference database locations from: ");
//        System.out.println(locationstorage.getAbsolutePath());
//        referencedatabases.clear();
//        if (! locationstorage.exists()){
//            try {
//                locationstorage.createNewFile();
//            } catch (IOException ex) {
//                System.out.println("ERROR creating db location file!");
//            }     
//        } else {
//            try {
//                locationstoragelines = Files.readAllLines(Paths.get(locationstorage.getAbsolutePath()));
//                Integer index = 0;
//                for (String line : locationstoragelines){
//                    referencedatabases.add(new refdb(index,line));
//                    index ++; 
//                    };        
//            } catch (IOException ex) {
//                Logger.getLogger(dbmanager.class.getName()).log(Level.SEVERE, null, ex);
//            }
        
        } catch (IOException | InterruptedException | ExecutionException ex){
            System.out.println("ERROR unable to access elasticsearch indices");
            System.out.println(ex);
        }
        
    }
    

    public void updateallindices(){
        for (refdb database : referencedatabases){
            database.scanindexrequests();
        }
    }
    
    

    
    
    public void removeselected(){
        int removeindex = -1;
        for (refdb db : referencedatabases){
            if (db.getSelect().getValue()){
                DeleteIndexRequest request = new DeleteIndexRequest(db.getFulldbname());
                try {
                    AcknowledgedResponse deleteIndexResponse = elasticsearchclient.indices().delete(request, RequestOptions.DEFAULT);
                } catch (IOException ex) {
                    Logger.getLogger(dbmanager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

            read();

    }
    
        /**
     * @return the referencedatabases
     */
    public ObservableList<refdb> getReferencedatabases() {
        return referencedatabases;
    }

    public void giveclient(RestHighLevelClient elasticsearchclient) {
        this.elasticsearchclient = elasticsearchclient;
    }

}
