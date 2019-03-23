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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.index.query.QueryBuilders;

/**
 *
 * @author emil3
 */
public class dbmanager {


    private File locationstorage;
    private ObservableList<refdb> referencedatabases = FXCollections.observableArrayList();
    TransportClient elasticsearchclient = null;
    
    public dbmanager(){
        read();
    }
    
    public void read(){
        referencedatabases.clear();
        if (elasticsearchclient != null){
        try{
        ImmutableOpenMap<String, IndexMetaData> indices = elasticsearchclient.admin().cluster()
        .prepareState().get().getState()
        .getMetaData().getIndices();
        

        Integer index = 1;
        for (ObjectCursor<String> indexname : indices.keys()){
            String indexnamestring = indexname.value;
//            get number of documents
            long totalHits = elasticsearchclient.prepareSearch(indexnamestring)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setTypes("entry")
                    .setSize(0)
                    .setQuery(QueryBuilders.queryStringQuery("*")).get().getHits().getTotalHits();
            
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
//        }
        } catch (Exception ex){
            System.out.println("ERROR unable to access elasticsearch indices");
            System.out.println(ex);
        }
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
                elasticsearchclient.admin().indices().delete(new DeleteIndexRequest(db.getFulldbname())).actionGet();
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

    public void giveclient(TransportClient elasticsearchclient) {
        this.elasticsearchclient = elasticsearchclient;
    }

}
