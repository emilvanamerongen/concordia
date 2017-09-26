/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Annotator;

import Refdbmanager.header;
import Refdbmanager.refdb;
import Tools.ByteArrayWrapper;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 *
 * @author emil3
 */
public class Searcher {
    private LinkedHashMap<String, header> sourceheaders = new LinkedHashMap<>();
    private LinkedHashMap<String, THashMap<ByteArrayWrapper, long[]>> data = new LinkedHashMap<>();
    private refdb db;


    private String dbtype = "";
    private HashMap<Integer, HashSet<Long>> log = new HashMap<>();
            
    public Searcher(refdb db){
        this.db = db;
        File file = db.getDatafile();
    }
    
    public void adddata(THashMap<ByteArrayWrapper, long[]> index, header myheader){
        getSourceheaders().put(myheader.getHeaderstring(), myheader);
        getData().put(myheader.getHeaderstring(), index);
    }
    
    public void removedata(header myheader){
        getSourceheaders().remove(myheader.getHeaderstring());
        getData().remove(myheader.getHeaderstring());
    }
    
    public void clearlog(Integer threadnumber){
        try{
        log.get(threadnumber).clear();
        } catch (Exception ex){}
    }
    
//    public synchronized ArrayList<String> readdata(Long position, String type) throws IOException{
//        ArrayList<String> lines = new ArrayList<>();
//        randomaccessfile.seek(position);
//        ByteBuffer buffer = ByteBuffer.allocate(100);
//        int bytes;
//        bytes = fileChannel.read(buffer);
//        String previousid = "";
//        boolean endfound = false; 
//        String line = "";
//        while (bytes != -1) {
//             buffer.flip();
//             while (buffer.hasRemaining()) {
//                 char character = (char) buffer.get();
//                 if (character=='\n'){
//                     lines.add(line);
//                     if (type.equals("uniprot")){
//
//                         if (line.equals("//")){
//                         endfound = true;
//                         }
//                     } else if (type.equals("tab-delimited")){
//                         endfound = true;
//                     } else if (type.equals("uniprot ID-mapping")){
//                         String newid = line.split("\t")[0];
//                         if (previousid.equals("") || previousid.equals(newid)){
//                         } else {
//                         endfound = true;
//                         }
//                         previousid=newid;
//                     }
//                 line = "";
//                 } else {
//                 line += character;
//                 }
//                 if (endfound){
//                 break;
//             }
//             }
//
//             buffer.clear();
//             if (endfound){
//                 break;
//             }
//             bytes = fileChannel.read(buffer);
//
//            }
//        
//        return lines;
//    }
 
    
    
    public synchronized HashMap<String, ArrayList<Long>> getgesultpositions(String query, Integer colorindex){
        HashMap<String, ArrayList<Long>> results = new LinkedHashMap<>();
        ByteArrayWrapper bytequery = new ByteArrayWrapper(query.getBytes());

        for (String key : data.keySet()){
            Integer color = sourceheaders.get(key).getColorindex();
            if (color.equals(colorindex)){
            THashMap<ByteArrayWrapper, long[]> index = data.get(key);
            try {
                results.put(key, new ArrayList<>());
                for (long position: index.get(bytequery)){
                    results.get(key).add(position);
                }

            } catch (Exception ex){}
        }
        }
        return results;
    }
    
    
    public LinkedHashMap<String, ArrayList<Long>> getgesultpositions(String query){
            LinkedHashMap<String, ArrayList<Long>> results = new LinkedHashMap<>();
            ByteArrayWrapper bytequery = new ByteArrayWrapper(query.getBytes());
            
            for (String key : data.keySet()){
                THashMap<ByteArrayWrapper, long[]> index = data.get(key);
                try {
                    results.put(key, new ArrayList<>());
                    for (long position: index.get(bytequery)){
                        results.get(key).add(position);
                    }
       
                } catch (Exception ex){}
            }
            
            return results;
        }
        
   
            
            
         
        
        public void writeline(String line, File outputfile){
            
        }

    /**
     * @return the sourceheaders
     */
    synchronized public LinkedHashMap<String, header> getSourceheaders() {
        return sourceheaders;
    }

    /**
     * @param sourceheaders the sourceheaders to set
     */
    synchronized public void setSourceheaders(LinkedHashMap<String, header> sourceheaders) {
        this.sourceheaders = sourceheaders;
    }

    /**
     * @return the data
     */
    synchronized public LinkedHashMap<String, THashMap<ByteArrayWrapper, long[]>> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    synchronized public void setData(LinkedHashMap<String, THashMap<ByteArrayWrapper, long[]>> data) {
        this.data = data;
    }

    /**
     * @return the db
     */
    synchronized public refdb getDb() {
        return db;
    }

    /**
     * @param db the db to set
     */
    synchronized public void setDb(refdb db) {
        this.db = db;
    }
}
