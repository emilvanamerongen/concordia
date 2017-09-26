/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TabDelimitedModule;


import CazyModule.CazyAnnotator;
import FileManager.Filemanager;
import Refdbmanager.header;
import Refdbmanager.refdb;
import Tools.ByteArrayWrapper;
import UniprotModule.UniprotParser;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author emil3
 */
public class TabDelimitedIndexer extends Thread{
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
        
        public TabDelimitedIndexer(ArrayList<header> headers, File inputfile, refdb parent){
            this.headers = headers; 
            this.inputfile = inputfile;
            String allheaders = "";
            for (header myheader : headers){
                headersindexing += myheader.getHeaderstring()+"   ";
            }
            indexername = headers.get(0).getSourcedb()+"-"+inputfile.getName();
            this.parent = parent;
        }

    public TabDelimitedIndexer() {
        
    }
        
        public void run(){

            try {
                filesize = filesize(inputfile.getAbsolutePath());
            } catch (IOException ex) {
                Logger.getLogger(TabDelimitedIndexer.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.println("index started for: ");
            ArrayList<String> headerstrings = new ArrayList<>();
            HashMap<String, THashMap<ByteArrayWrapper, long[]>> indexset = new HashMap<>();
            ArrayList<String> removeversionheaderstrings = new ArrayList<>();
            LinkedHashMap<Integer, String> requestedtabindices = new LinkedHashMap<>();
            for (header myheader : headers){
            System.out.println("\t"+myheader.getHeaderstring());
            headerstrings.add(myheader.getHeaderstring());
            indexset.put(myheader.getHeaderstring(), new THashMap<>(1,loadfactor));
            requestedtabindices.put(myheader.getTabindex(),myheader.getHeaderstring());
            if (myheader.getParameters().contains("removeversion")){
                removeversionheaderstrings.add(myheader.getHeaderstring());
            }
            }
            System.out.println("from: "+headers.get(0).getSourcedb()+"\t"+inputfile.getName());
            
            
            Set<Integer> requestedindiceskeys = requestedtabindices.keySet();
            Boolean done = false;

            try {
                
                RandomAccessFile stream = new RandomAccessFile(inputfile, "r");
                Boolean atempt = false;
                
                while (!done){
                    try{ 
                        if (kill){
                            break;
                        }
                        if (atempt){
                            System.out.println("end of file from check");
                            break;
                        }
                        atempt = true;
                        setLineposition(stream.getFilePointer());
                        String line = stream.readLine();   
                        
                           
                        
                        if (complete){
                            System.out.println("end of file from completeboolean");
                            break;
                        }
                        
                        ArrayList<String> splitline = new ArrayList<>();
                        StringTokenizer st = new StringTokenizer(line,"\t");
                        while (st.hasMoreTokens()) {
                            splitline.add(st.nextToken());
                        }
                            
                        for (Integer tabindex : requestedindiceskeys){
                            try{
                            String item = splitline.get(tabindex);
                            if (item != ""){
                                if (removeversionheaderstrings.contains(requestedtabindices.get(tabindex))){
                                try {
                                    item = item.substring(0,item.indexOf("."));
                                } catch (Exception ex){}
                                }
  
                                ByteArrayWrapper bytevalue = new ByteArrayWrapper(item.getBytes(StandardCharsets.US_ASCII));
                                indexset.putIfAbsent(requestedtabindices.get(tabindex), new THashMap<>());
                                indexset.get(requestedtabindices.get(tabindex)).putIfAbsent(bytevalue, new long[1]);
                                long[] previousvalue = indexset.get(requestedtabindices.get(tabindex)).get(bytevalue);
                                if (previousvalue.length == 1){
                                    indexset.get(requestedtabindices.get(tabindex)).get(bytevalue)[0] = getLineposition();
                                } else {
                                    indexset.get(requestedtabindices.get(tabindex)).remove(bytevalue);
                                    indexset.get(requestedtabindices.get(tabindex)).putIfAbsent(bytevalue, new long[previousvalue.length+1]);
                                    int index = 0;
                                    for (long mylong : previousvalue){
                                        indexset.get(requestedtabindices.get(tabindex)).get(bytevalue)[index] = mylong;
                                        index++;
                                    }
                                    indexset.get(requestedtabindices.get(tabindex)).get(bytevalue)[index] = getLineposition();
                                }
                            }
                                    
                            } catch (Exception ex){}
                        }
                                
                            
                        atempt = false;

                    } catch (EOFException ex ){
                        System.out.println("end of file from exeption");
                        done = true;
                        break;
                    } catch (IOException ex) {
                        Logger.getLogger(UniprotParser.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IndexOutOfBoundsException ex) {
                        System.out.println("IndexOutOfBoundsException");
                        atempt = false;
                    } catch (NullPointerException ex) {
                        System.out.println("end of file from exeption");
                        done = true;
                        break;  
                    }
                }
                
                

                
            } catch (FileNotFoundException ex) {
                Logger.getLogger(UniprotParser.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(UniprotParser.class.getName()).log(Level.SEVERE, null, ex);
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
            parent.getTabindexerthreads().remove(indexername);
            }catch (Exception ex){}

            
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
    synchronized public Long getFilesize() {
        return filesize;
    }

    /**
     * @param filesize the filesize to set
     */
    synchronized public void setFilesize(Long filesize) {
        this.filesize = filesize;
    }

    /**
     * @return the lineposition
     */
    synchronized public Long getLineposition() {
        return lineposition;
    }

    /**
     * @param lineposition the lineposition to set
     */
    synchronized public void setLineposition(Long lineposition) {
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


