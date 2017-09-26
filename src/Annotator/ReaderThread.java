/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Annotator;

import Refdbmanager.header;
import Refdbmanager.refdb;
import TabDelimitedModule.TabDelimitedEntry;
import UniprotModule.IDMappingEntry;
import UniprotModule.UniprotEntry;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author minor-g1
 */
public class ReaderThread extends Thread{
    private Boolean active = true;
    private refdb db;
    private String dbname; 
    private RandomAccessFile randomaccessfile = null;
    private TabDelimitedEntry tabentry = new TabDelimitedEntry();
    private UniprotEntry uniprotentry = new UniprotEntry();
    private IDMappingEntry idmappingentry = new IDMappingEntry();
    private String dbtype = "";
    private HashMap<Integer, HashSet<Long>> log = new HashMap<>();
    
    public ReaderThread(refdb db){
        this.db = db;
        this.dbname = this.db.getDbname();
        File file = db.getDatafile();
        dbtype = db.getType();
        try {
            randomaccessfile = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Searcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void run(){
        while (getActive()){
            try{
                Thread.sleep(1);
            if (!AnnotationProcess.readrequests.get(dbname).isEmpty()){
                ReadRequest request = AnnotationProcess.readrequests.get(dbname).poll();
                if (request == null){
                
                } else {
                    try {
                        HashMap<String, LinkedHashSet<String>> result = readdata(request.getQuery());
                        if (!result.isEmpty()){
                            request.setResult(result);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(ReaderThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    AnnotationProcess.completedreadrequests.get(request.getRequestthread()).offer(request);
                  }
                
            }
            } catch (NullPointerException ex){} catch (InterruptedException ex) {
                Logger.getLogger(ReaderThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
    }
    
     public HashMap<String, LinkedHashSet<String>> readdata(Long itemposition) throws FileNotFoundException, IOException{
            HashMap<String, LinkedHashSet<String>> resultdata = new HashMap<>();
            randomaccessfile.seek(itemposition);
            FileChannel fileChannel = randomaccessfile.getChannel();
            boolean endfound = false; 
                try {
                String line = "";
                Integer linenr = 0;
                
                ByteBuffer buffer = ByteBuffer.allocate(100);
                int bytes = fileChannel.read(buffer);
                bytes = fileChannel.read(buffer);
                Boolean process = false;
                if (dbtype.equals("tab-delimited")){
                tabentry = new TabDelimitedEntry();
                LinkedHashMap<Integer,String> headermap = new LinkedHashMap<>();
                Integer tabindex = 0;
                for (header myheader : db.getHeaderset()){
                headermap.put(tabindex, myheader.getHeaderstring());
                tabindex++;
                }

                tabentry.setHeadermap(headermap);
                } else if (dbtype.equals("uniprot")){
                uniprotentry = new UniprotEntry();
                } else if (dbtype.equals("uniprot ID-mapping")){
                idmappingentry = new IDMappingEntry();
                }

                String previousid = "";
                                
                while (bytes != -1) {
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            char character = (char) buffer.get();
                            if (character=='\n'){
                                if (dbtype.equals("uniprot")){
                                uniprotentry.addline(line);
                                if (line.equals("//")){
                                endfound = true;
                                }
                                } else if (dbtype.equals("tab-delimited")){
                                tabentry.addline(line, false);
                                endfound = true;
                                } else if (dbtype.equals("uniprot ID-mapping")){
                                String newid = line.split("\t")[0];
                                if (previousid.equals("") || previousid.equals(newid)){
                                idmappingentry.addline(line);
                                } else {
                                endfound = true;
                                }
                                previousid=newid;
                                }
                  

                                line = "";
                            } else {
                            line += character;
                            }
                            if (endfound){
                            break;
                        }
                        }

                        buffer.clear();
                        if (endfound){
                            break;
                        }
                        bytes = fileChannel.read(buffer);
                        

                    }
                    } catch (IOException ex) {
                        Logger.getLogger(Searcher.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IndexOutOfBoundsException ex) { System.out.println("index out of bounds < readerthread");
                    } finally {
                    }
                 
            if (dbtype.equals("uniprot")){
                resultdata = uniprotentry.getData();
            } else if (dbtype.equals("tab-delimited")){
                resultdata = tabentry.getData();
            } else if (dbtype.equals("uniprot ID-mapping")){
                resultdata = idmappingentry.getData();
            }
            return resultdata;
        }

    /**
     * @return the active
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    
    
    
    
}
