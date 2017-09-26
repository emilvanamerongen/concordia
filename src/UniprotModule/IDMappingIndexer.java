/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UniprotModule;

import FileManager.Filemanager;
import Refdbmanager.header;
import Refdbmanager.refdb;
import Tools.ByteArrayWrapper;
import Tools.ByteUtils;
import gnu.trove.map.hash.THashMap;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil3
 */
public class IDMappingIndexer extends Thread{
        ArrayList<header> headers;
        File inputfile;
        private HashMap<String, String> headertotype = new HashMap<>();
        private String indexername = "";
        private String headersindexing = "";
        private Boolean kill = false;
        private Boolean complete = false;
        private refdb parent;
        Filemanager filemanager = new Filemanager();
        private Long filesize = 0L;
        private Long lineposition = 0L;
        private float loadfactor = 0.75f;
        private THashMap<String, THashMap<ByteArrayWrapper, long[]>> indexset = new THashMap<>(1,loadfactor);
        private HashSet<String> headerstrings = new HashSet<>();
        private HashSet<String> removeversionheaderstrings = new HashSet<>();
        private Boolean UniProtKBac = false;
        private Long itemposition = 0L;
        private String previousuniprotid = "";
        
        public IDMappingIndexer(ArrayList<header> headers, File inputfile, refdb parent){
            this.headers = headers; 
            this.inputfile = inputfile;
            String allheaders = "";
            for (header myheader : headers){
                headersindexing += myheader.getHeaderstring()+"   ";
            }
            indexername = headers.get(0).getSourcedb()+"-"+inputfile.getName();
            this.parent = parent;
        }

    public IDMappingIndexer() {
        
    }
        
        public void run(){
            
            try {
                filesize = filesize(inputfile.getAbsolutePath());
            } catch (IOException ex) {
                Logger.getLogger(IDMappingIndexer.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("index started for: ");

            LinkedHashMap<Integer, String> requestedtabindices = new LinkedHashMap<>();
            for (header myheader : headers){
                System.out.println("\t"+myheader.getHeaderstring());
                headerstrings.add(myheader.getHeaderstring());
                indexset.put(myheader.getHeaderstring(), new THashMap<>());
                requestedtabindices.put(myheader.getTabindex(),myheader.getHeaderstring());
                if (myheader.getParameters().contains("removeversion")){
                    removeversionheaderstrings.add(myheader.getHeaderstring());
                }
            }
            System.out.println("from: "+headers.get(0).getSourcedb()+"\t"+inputfile.getName());
            
            
            
            Boolean done = false;
            String ac = "";
            double linenr = 0;

           
            
            String line = "";
            if (headerstrings.contains("Accession number")){
                UniProtKBac = true;
            }
             RandomAccessFile stream = null;
            try {
                stream = new RandomAccessFile(inputfile, "r");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(uniprotindexer.class.getName()).log(Level.SEVERE, null, ex);
            }
                
                try {
                

                FileChannel fileChannel = stream.getChannel();
                ByteBuffer buffer = ByteBuffer.allocate(600);
                int bytes = fileChannel.read(buffer);
                bytes = fileChannel.read(buffer);

                while (bytes != -1) {
                     
                        buffer.flip();
                        int charsdone = 0;
                        while (buffer.hasRemaining()) {
                            char character = (char) buffer.get();
                            if (character=='\n'){
                                processline(line);
                                setLineposition(stream.getFilePointer()-(600-charsdone));
                                line = null;
                                line = "";
                            } else {
                            line += character;
                            }
                            charsdone ++;
                        }

                        buffer.clear();
                        bytes = fileChannel.read(buffer);

                        if (kill){
                            break;
                        }
                    
                        if (complete){
                            System.out.println("end of file from completeboolean");
                            break;
                        }
   

                    }
                    } catch (EOFException ex ){
                        System.out.println("end of file from exeption");
                        done = true;
                    } catch (IOException ex) {
                        Logger.getLogger(UniprotParser.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IndexOutOfBoundsException ex) {

                    } catch (NullPointerException ex) {
                        System.out.println("end of file from exeption");
                        done = true;
                    } finally {
                        try {
                            stream.close();
                            System.out.println("done");
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }

            if (kill){
                System.out.println("index canceled");
            } else {
            System.out.println("index complete");
             try
            {
                
                for (header myheader : headers){
                  System.out.println(myheader.getHeaderstring()+" HashMap size: "+indexset.get(myheader.getHeaderstring()).size());
                  
                  FileOutputStream fos =
                     new FileOutputStream(filemanager.getIndexdirectory()+File.separator+myheader.getSourcedb()+"."+myheader.getHeaderstring()+".ser");
                  ObjectOutputStream oos = new ObjectOutputStream(fos);
                  oos.writeObject(indexset.get(myheader.getHeaderstring()));
                  oos.close();
                  fos.close();
                  System.out.println("Serialized HashMap data is saved in: "+myheader.getSourcedb()+"."+myheader.getHeaderstring()+".ser");
                }
           }catch(IOException ioe)
            {
                  ioe.printStackTrace();
            }
            }
            indexset.clear();
            try {
            parent.getIDmappingindexerthreads().remove(indexername);
            }catch (Exception ex){}

            
        }

        
        
        
        public void processline(String line){
            StringTokenizer st = new StringTokenizer(line,"\t");
            String uniprotid = st.nextToken();
            String key = st.nextToken();
            String value = st.nextToken();
            
            if (!previousuniprotid.equals(uniprotid)){
                itemposition = lineposition;
                previousuniprotid = uniprotid;
                if (UniProtKBac){
                    ByteArrayWrapper bytevalue = new ByteArrayWrapper(uniprotid.getBytes(StandardCharsets.US_ASCII));
                    indexset.putIfAbsent("Accession number", new THashMap<>());
                    indexset.get("Accession number").putIfAbsent(bytevalue, new long[1]);
                    long[] previousvalue = indexset.get("Accession number").get(bytevalue);
                    if (previousvalue.length == 1){
                        indexset.get("Accession number").get(bytevalue)[0] = itemposition;
                    } else {
                        indexset.get("Accession number").remove(bytevalue);
                        indexset.get("Accession number").putIfAbsent(bytevalue, new long[previousvalue.length+1]);
                        int index = 0;
                        for (long item : previousvalue){
                            indexset.get("Accession number").get(bytevalue)[index] = item;
                            index++;
                        }
                        indexset.get("Accession number").get(bytevalue)[index] = itemposition;
                    }
                    
                    
                }
            }

            if (headerstrings.contains(key)){
                if (removeversionheaderstrings.contains(key)){
                    try {
                        value = value.substring(0,value.indexOf("."));
                    } catch (Exception ex){}
                }
                ByteArrayWrapper bytevalue = new ByteArrayWrapper(value.getBytes(StandardCharsets.US_ASCII));
                indexset.putIfAbsent(key, new THashMap<>());
                indexset.get(key).putIfAbsent(bytevalue, new long[1]);
                    long[] previousvalue = indexset.get(key).get(bytevalue);
                    if (previousvalue.length == 1){
                        indexset.get(key).get(bytevalue)[0] = itemposition;
                    } else {
                        indexset.get(key).remove(bytevalue);
                        indexset.get(key).putIfAbsent(bytevalue, new long[previousvalue.length+1]);
                        int index = 0;
                        for (long item : previousvalue){
                            indexset.get(key).get(bytevalue)[index] = item;
                            index++;
                        }
                        indexset.get(key).get(bytevalue)[index] = itemposition;
                    }
            }

        }
        
        public boolean isAlpha(String name) {
        char[] chars = name.toCharArray();

        for (char c : chars) {
            if(Character.isDigit(c) || Character.isWhitespace(c)) {
                return false;
            }
        }

        return true;
        }

        public Long filesize(String filename) throws IOException {
            long size = Files.size(new File(filename).toPath());
            System.out.println(filename+" "+size);
            return size;
}




    /**
     * @return the indexername
     */
    synchronized public String getIndexername() {
        return indexername;
    }

    /**
     * @param indexername the indexername to set
     */
    synchronized public void setIndexername(String indexername) {
        this.indexername = indexername;
    }

    /**
     * @return the headersindexing
     */
    synchronized public String getHeadersindexing() {
        return headersindexing;
    }

    /**
     * @param headersindexing the headersindexing to set
     */
    synchronized public void setHeadersindexing(String headersindexing) {
        this.headersindexing = headersindexing;
    }

    /**
     * @return the kill
     */
    synchronized public Boolean getKill() {
        return kill;
    }

    /**
     * @param kill the kill to set
     */
    synchronized public void setKill(Boolean kill) {
        this.kill = kill;
    }

    /**
     * @return the filesize
     */
    synchronized public long getFilesize() {
        return filesize;
    }

    /**
     * @param filesize the filesize to set
     */
    synchronized public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    /**
     * @return the lineposition
     */
    synchronized public long getLineposition() {
        return lineposition;
    }

    /**
     * @param entryposition the lineposition to set
     */
    synchronized public void setLineposition(long lineposition) {
        this.lineposition = lineposition;
    }

    /**
     * @return the complete
     */
    synchronized public Boolean getComplete() {
        return complete;
    }

    /**
     * @param complete the complete to set
     */
    synchronized public void setComplete(Boolean complete) {
        this.complete = complete;
    }
}
