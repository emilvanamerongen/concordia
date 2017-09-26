/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UniprotModule;

import FileManager.Filemanager;
import Refdbmanager.header;
import Refdbmanager.refdb;
import TabDelimitedModule.TabDelimitedIndexer;
import Tools.ByteArrayWrapper;
import Tools.ByteUtils;
import static UniprotModule.UniprotParser.active;
import concordia.GUIController;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
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
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.layout.HBox;
import static javafx.util.Duration.millis;

/**
 *
 * @author emil3
 */
public class uniprotindexer extends Thread{
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
        private Long entryposition = 0L;
        private Long lineposition = 0L;
        private float loadfactor = 0.75f;
        private THashMap<String, THashMap<ByteArrayWrapper, long[]>> indexset = new THashMap<>(1,loadfactor);
        private Boolean findid = false;
        private Boolean findac = false;
        private HashSet<String> headerstrings = new HashSet<>();
        private HashSet<String> removeversionheaderstrings = new HashSet<>();
        private ByteArrayWrapper itempositionbytes = null;
        
        public uniprotindexer(ArrayList<header> headers, File inputfile, refdb parent){
            this.headers = headers; 
            this.inputfile = inputfile;
            String allheaders = "";
            for (header myheader : headers){
                headersindexing += myheader.getHeaderstring()+"   ";
            }
            indexername = headers.get(0).getSourcedb()+"-"+inputfile.getName();
            this.parent = parent;
        }

    public uniprotindexer() {
        
    }
        
        public void run(){
            
            try {
                filesize = filesize(inputfile.getAbsolutePath());
            } catch (IOException ex) {
                Logger.getLogger(uniprotindexer.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("index started for: ");
            
            
            for (header myheader : headers){
            System.out.println("\t"+myheader.getHeaderstring());
            headerstrings.add(myheader.getHeaderstring());
            indexset.put(myheader.getHeaderstring(), new THashMap<>(1,loadfactor));
            if (myheader.getParameters().contains("removeversion")){
                removeversionheaderstrings.add(myheader.getHeaderstring());
            }
            }
            System.out.println("from: "+headers.get(0).getSourcedb()+"\t"+inputfile.getName());
            Boolean done = false;
            double linenr = 0;
            
            
            if (headerstrings.contains("EntryName")){
                findid = true;
            }
            if (headerstrings.contains("Accession number")){
                findac = true;
            }
           
                
                RandomAccessFile stream = null;
            try {
                stream = new RandomAccessFile(inputfile, "r");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(uniprotindexer.class.getName()).log(Level.SEVERE, null, ex);
            }
                
                 try {
                String line = "";

                FileChannel fileChannel = stream.getChannel();
                ByteBuffer buffer = ByteBuffer.allocate(600);
                int bytes = fileChannel.read(buffer);
                bytes = fileChannel.read(buffer);

                while (bytes != -1) {
                     
                        buffer.flip();
                        String totalbufferline = "";
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
                            charsdone +=1;
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
            parent.getUniprotindexerthreads().remove(indexername);
            }catch (Exception ex){}

            
        }


            public void processline(String line){
                String type = line.substring(0,2);
                
                if (type.equals("ID")){
                    entryposition = getLineposition();
                   
                }
                if (findid && type.equals("ID")){
                            String rest = line.substring(5);
                            StringTokenizer st = new StringTokenizer(rest," ");

                            String value = st.nextToken();

                            if (removeversionheaderstrings.contains("EntryName")){
                            try {
                                value = value.substring(0,value.indexOf("."));
                            } catch (Exception ex){}
                            }
                            ByteArrayWrapper bytevalue = new ByteArrayWrapper(value.getBytes(StandardCharsets.US_ASCII));
                            indexset.get("EntryName").putIfAbsent(bytevalue, new long[1]);
                            long[] previousvalue = indexset.get("EntryName").get(bytevalue);
                            if (previousvalue.length == 1){
                                indexset.get("EntryName").get(bytevalue)[0] = entryposition;
                            } else {
                                indexset.get("EntryName").remove(bytevalue);
                                indexset.get("EntryName").putIfAbsent(bytevalue, new long[previousvalue.length+1]);
                                int index = 0;
                                for (long item : previousvalue){
                                    indexset.get("EntryName").get(bytevalue)[index] = item;
                                    index++;
                                }
                                indexset.get("EntryName").get(bytevalue)[index] = entryposition;
                            }
                            
                            

                            
                        } else if (findac && type.equals("AC")){ 
                            String rest = line.substring(5);
                            StringTokenizer st = new StringTokenizer(rest," ");

                            String value = st.nextToken();
                            value = value.substring(0, value.length()-1);

                            ByteArrayWrapper bytevalue = new ByteArrayWrapper(value.getBytes(StandardCharsets.US_ASCII));
                            
                            if (removeversionheaderstrings.contains("Accession number")){
                            try {
                                value = value.substring(0,value.indexOf("."));
                            } catch (Exception ex){}
                            }
                            indexset.get("Accession number").putIfAbsent(bytevalue, new long[1]);
                            long[] previousvalue = indexset.get("Accession number").get(bytevalue);
                            if (previousvalue.length == 1){
                                indexset.get("Accession number").get(bytevalue)[0] = entryposition;
                            } else {
                                indexset.get("Accession number").remove(bytevalue);
                                indexset.get("Accession number").putIfAbsent(bytevalue, new long[previousvalue.length+1]);
                                int index = 0;
                                for (long item : previousvalue){
                                    indexset.get("Accession number").get(bytevalue)[index] = item;
                                    index++;
                                }
                                indexset.get("Accession number").get(bytevalue)[index] = entryposition;
                            }

                            
                        }else if (type.equals("DR")){
                            
                            String rest = line.substring(5);
                            StringTokenizer st = new StringTokenizer(rest,"; ");
                            ArrayList<String> drsplit = new ArrayList<String>();
                            String RESOURCE_ABBREVIATION = st.nextToken().trim();
                            
                            RESOURCE_ABBREVIATION = "DR "+RESOURCE_ABBREVIATION;
                            
                            if (headerstrings.contains(RESOURCE_ABBREVIATION)){
                                Integer tabindex = 0;
                                while (st.hasMoreTokens()) {
                                    String RESOURCE_IDENTIFIER = st.nextToken();
                                    Boolean infoitem = false;
                                    Boolean skip = false;
                                    Boolean bracketremove = false;

                                    if (RESOURCE_IDENTIFIER.length() <= 2){
                                        skip = true;
                                    } 
                                    else if (RESOURCE_ABBREVIATION.equals("DR GO") && !RESOURCE_IDENTIFIER.startsWith("GO:")){
                                        infoitem = true;
                                    } else if (RESOURCE_ABBREVIATION.equals("DR InterPro") && tabindex > 0){
                                        infoitem = true;
                                    } else if (RESOURCE_ABBREVIATION.equals("DR PROSITE") && tabindex > 0){
                                        infoitem = true;
                                    } else if (RESOURCE_ABBREVIATION.equals("DR Pfam") && tabindex > 0){
                                        infoitem = true;
                                    }else if (RESOURCE_ABBREVIATION.equals("DR EMBL") && isAlpha(RESOURCE_IDENTIFIER)){
                                        infoitem = true;
                                    }else if (RESOURCE_ABBREVIATION.equals("DR TIGRFAMs") && isAlpha(RESOURCE_IDENTIFIER)){
                                        infoitem = true;
                                    }else if (RESOURCE_ABBREVIATION.equals("DR PIRSF") && isAlpha(RESOURCE_IDENTIFIER)){
                                        infoitem = true;
                                    }else if (RESOURCE_ABBREVIATION.equals("DR CDD") && !RESOURCE_IDENTIFIER.startsWith("cd")){
                                        infoitem = true;
                                    }else if (RESOURCE_ABBREVIATION.equals("DR Proteomes") && isAlpha(RESOURCE_IDENTIFIER)){
                                        infoitem = true;
                                   }else if (RESOURCE_ABBREVIATION.equals("DR PRINTS") && isAlpha(RESOURCE_IDENTIFIER)){
                                        infoitem = true;
                                    }else if (RESOURCE_ABBREVIATION.equals("DR PDB") && RESOURCE_IDENTIFIER.length()!=4){
                                        infoitem = true;
                                    }else if (RESOURCE_ABBREVIATION.equals("DR RefSeq")){
                                        bracketremove = true;
                                    }

                                    tabindex++;
                                    
                                
                                
                                    
                                
                                if (bracketremove){
                                    try{
                                        RESOURCE_IDENTIFIER = RESOURCE_IDENTIFIER.substring(0,RESOURCE_IDENTIFIER.indexOf("["));
                                    } catch (Exception ex){}
                                }
                                if (skip || infoitem){
                                    
                                } else {
                                if (removeversionheaderstrings.contains(RESOURCE_IDENTIFIER)){
                                try {
                                    RESOURCE_IDENTIFIER = RESOURCE_IDENTIFIER.substring(0,RESOURCE_IDENTIFIER.indexOf("."));
                                } catch (Exception ex){}
                                }
                                RESOURCE_IDENTIFIER = RESOURCE_IDENTIFIER.trim();
                                ByteArrayWrapper bytevalue = new ByteArrayWrapper(RESOURCE_IDENTIFIER.getBytes(StandardCharsets.US_ASCII));
                                
                               indexset.get(RESOURCE_ABBREVIATION).putIfAbsent(bytevalue, new long[1]);
                                long[] previousvalue = indexset.get(RESOURCE_ABBREVIATION).get(bytevalue);
                                if (previousvalue.length == 1){
                                    indexset.get(RESOURCE_ABBREVIATION).get(bytevalue)[0] = entryposition;
                                } else {
                                    indexset.get(RESOURCE_ABBREVIATION).remove(bytevalue);
                                    indexset.get(RESOURCE_ABBREVIATION).putIfAbsent(bytevalue, new long[previousvalue.length+1]);
                                    int index = 0;
                                    for (long item : previousvalue){
                                        indexset.get(RESOURCE_ABBREVIATION).get(bytevalue)[index] = item;
                                        index++;
                                    }
                                    indexset.get(RESOURCE_ABBREVIATION).get(bytevalue)[index] = entryposition;
                             }

                                
                            }
                                }
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
     * @return the entryposition
     */
    synchronized public Long getEntryposition() {
        return entryposition;
    }

    /**
     * @param entryposition the entryposition to set
     */
    synchronized public void setEntryposition(Long entryposition) {
        this.entryposition = entryposition;
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
