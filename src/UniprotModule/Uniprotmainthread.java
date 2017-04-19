/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UniprotModule;

import CazyModule.CazyAnnotator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil
 */
public class Uniprotmainthread extends Thread{
    private ArrayList<File> inputfiles;
    private File uniprotidfile;
    private ArrayList<File> outputfiles;
    private ArrayList<File> uniprotfiles;
    private final HashMap<String, String> gitouniprot = new HashMap<>();
    private final HashMap<String, String> uniprotdata = new HashMap<>();
    private final File annotationdirectory;
    private static volatile Integer threadsactive = 0;
    private static volatile Integer threadsdone = 0;
    private static volatile HashSet<Integer> requiredgis = new HashSet<>();
    private static volatile HashSet<String> requiredgenbank = new HashSet<>();
    private static volatile HashSet<String> requireduniprotids = new HashSet<>();
    private static volatile ReentrantLock lock = new ReentrantLock();
    private static volatile Long total = 0l;
    private static volatile Long done = 0l;
    private static volatile String process = "";
    private static volatile Boolean complete = false;
    private Boolean lowrammode;
    
    Uniprotmainthread(ArrayList<File> uniprotfiles, File uniprotidfile, ArrayList<File> inputfiles, File annotationdirectory, Boolean lowrammode){
        this.inputfiles = inputfiles;
        this.uniprotfiles = uniprotfiles;
        this.annotationdirectory = annotationdirectory;
        this.uniprotidfile = uniprotidfile;
        this.lowrammode = lowrammode;
    }
    public void run() {
        ArrayList<ArrayList<File>> splitlist = new ArrayList<>();
        if (lowrammode){
            for (File file : inputfiles){
                ArrayList<File> templist = new ArrayList<>();
                templist.add(file);
                splitlist.add(templist);
            }
        } else {
            splitlist.add(inputfiles);
        }
        for (ArrayList<File> inputfilesplit : splitlist){
        System.out.println("#UNIPROT-ANNOTATOR\tANNOTATING FILE(S): "+inputfilesplit.toString());
        System.out.println("#UNIPROT-ANNOTATOR\trequired ID scan\n---------------------------------------");
        Uniprotmainthread.setProcess("scanning IDs");
        readrequiredgis(inputfilesplit);
        System.out.println("#UNIPROT-ANNOTATOR\t"+requiredgis.size()+" gi's found");
        System.out.println("#UNIPROT-ANNOTATOR\t"+requiredgenbank.size()+" genbankid's found\n");
        readrequiredidlines(uniprotidfile);
        System.out.println("#UNIPROT-ANNOTATOR\t"+requireduniprotids.size()+" required UniProt ID's\n---------------------------------------");
        System.out.println("#UNIPROT-ANNOTATOR\t START ID annotation");
        for (File file : inputfilesplit){
            threadsactive ++;
            new Writethread(gitouniprot,file,annotationdirectory,1,true,true,".temp",12).start();
        } 
        while (threadsactive > threadsdone){ try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {Logger.getLogger(Uniprotmainthread.class.getName()).log(Level.SEVERE, null, ex);}}
        threadsactive = 0;
        threadsdone = 0;
        gitouniprot.clear();
        requiredgis.clear();
        requiredgenbank.clear();
        System.gc();
        System.out.println("#UNIPROT-ANNOTATOR\t ID annotation Complete\n---------------------------------------");
        System.out.println("#UNIPROT-ANNOTATOR\t reading UniProt data");
        try {
            uniprotannotation(inputfilesplit);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Uniprotmainthread.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("#UNIPROT-ANNOTATOR\t Complete\n---------------------------------------");
        }
        Uniprotmainthread.setProcess("complete");
        Uniprotmainthread.setComplete(true);       
    }
    
    /**
     *
     * @param inputfilesplit
     * @throws UnsupportedEncodingException
     */
    public void uniprotannotation(ArrayList<File> inputfilesplit) throws UnsupportedEncodingException{
        int totalestimatedlines = 0;
        long linecount2 = 0;
        for (File uniprotfile : uniprotfiles){
            FileInputStream inputStream = null;
            double totalbytes = uniprotfile.length();
            try {
                inputStream = new FileInputStream(uniprotfile.getAbsolutePath());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CazyAnnotator.class.getName()).log(Level.SEVERE, null, ex);
            }
            Scanner sc = new Scanner(inputStream, "UTF-8");
            List<String> list = new ArrayList<>();
            Uniprotmainthread.setProcess("Calculating estimated lines in files");
            boolean ramcheck = true;
            boolean go = false;
            int itemlimit = 0;
            int bytes5000lines = 0;
            boolean bytecheck = true;
            long linecount = 0;
            long estimatedlines = 0;
            while (sc.hasNextLine()) {
                linecount++;
                String line = sc.nextLine();
                line += "\n";
                if (bytecheck){
                    final byte[] utf8Bytes = line.getBytes("UTF-8");
                    bytes5000lines += utf8Bytes.length;
                    if (linecount == 5000){
                        bytecheck = false;
                        long avglinesize = bytes5000lines/5000;
                        estimatedlines = (long) Math.ceil(totalbytes/avglinesize);
                        totalestimatedlines += estimatedlines;
                        Uniprotmainthread.addTotal(estimatedlines);
                        break;
                }}
        }
            sc.close();
            try {
                inputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(Uniprotmainthread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("estimated lines: "+totalestimatedlines);
        for (File uniprotfile : uniprotfiles){
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(uniprotfile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CazyAnnotator.class.getName()).log(Level.SEVERE, null, ex);
        }
        Scanner sc = new Scanner(inputStream, "UTF-8");
        List<String> list = new ArrayList<>();
        Uniprotmainthread.setProcess("UniProt annotation");
        boolean ramcheck = true;
        boolean go = false;
        int itemlimit = 0;

        while (sc.hasNextLine()) {
            linecount2++;
            String line = sc.nextLine();
            Uniprotmainthread.addDone();
            if (line.contains("//") && line.length() == 2) {    
                String Uniprotid = "";
                String Status = "";
                String Refseq1 = "";
                String Refseq2 = "";
                String EMBLgenome = "";
                String EMBLprotein = "";
                String Recname = "";
                String Organism = "";
                String Organismextra = "";
                HashMap<String, String> CCdata = new HashMap<>();
                String Discription = "";
                String gistring = "";
                String Orfnames = "";
                String Taxlineage = "";
                String GO = "";
                String GOtext = "";
                String Keywords = "";
                String KEGG = "";
                String Interpro = "";
                String Pfam = "";
                String activekey = "";
   
                for (String str : list) {
                    try {
                    if (str.matches("^AC.*")) {
                        Uniprotid = str.substring(3,str.length()).split(";")[0].trim();
                        if (requireduniprotids.contains(Uniprotid)){
                            go = true;
                        } else {
                            break;
                        }
                    }else if(str.contains("ID") && str.contains("AA.")){
                        if (str.contains("Unreviewed")){
                        Status = "Unreviewed";
                        } else  if (str.contains("Reviewed")){
                            Status = "Reviewed";
                        }
                    } else if (str.trim().contains("DR   RefSeq")) {
                        if (Refseq1 != ""){
                            Refseq1 += "; ";
                        }
                        if (Refseq2  != ""){
                            Refseq2 += "; ";
                        }
                        Refseq1 += str.split(" ")[4].replaceFirst(".$", "");
                        Refseq2 += str.split(" ")[5].replaceFirst(".$", "");
                    } else if (str.trim().contains("DR   EMBL")) {
                        if (EMBLgenome != ""){
                            EMBLgenome += "; ";
                        } 
                        if (EMBLprotein != ""){
                            EMBLprotein += "; ";
                        }
                        EMBLgenome += str.split(" ")[4].replaceFirst(".$", "");
                        EMBLprotein += str.split(" ")[5].replaceFirst(".$", "").replace("-", "");
                    } else if (str.trim().contains("DE   RecName")) {
                        if (Recname != ""){
                            Recname += "; ";
                        }
                        Recname += str.split("   ")[1].replaceFirst(".$", "").substring(14).replace("-", "");
                    } else if (str.trim().contains("DE   SubName")){
                        if (Recname != ""){
                            Recname += "; ";
                        }
                        Recname += str.substring(str.indexOf("=")+1).replace(";", "");
                    } else if (str.trim().contains("GN   ORFNames")) {
                        if (Orfnames != ""){
                            Orfnames += "; ";
                        }
                        Orfnames += str.split("   ")[1].replaceFirst(".$", "").substring(9);
                    } else if (str.trim().contains("OS   ")) {
                        String organismline = str.split("   ")[1].replaceFirst(".$", "");
                        if (organismline.contains("(")){
                        Organism = organismline.substring(0, organismline.indexOf("(",0));
                        Organismextra = organismline.substring(organismline.indexOf("(",0));
                        } else {
                            Organism = organismline;
                        }
                    } else if (str.trim().contains("OX   ")) {
                        if (Taxlineage.length() != 0){
                            Taxlineage += "; ";
                        }
                        String taxtemp = str.substring(16);
                        if (taxtemp.contains("{")){
                            taxtemp = taxtemp.substring(0,taxtemp.indexOf("{"));
                        }
                        if (taxtemp.contains(";")){
                            taxtemp = taxtemp.replace(";", "");
                        }
                        Taxlineage += taxtemp;
                    } else if (str.trim().contains("DR   GeneID")) {
                        if (gistring != ""){
                            gistring += "; ";
                        }
                        gistring += str.replaceAll("[^0-9]", "");
                    } else if (str.trim().contains("DR   KEGG")) {
                        if (KEGG != ""){
                            KEGG += "; ";
                        }
                        KEGG += str.split(" ")[4].replaceFirst(".$", "");
                    } else if (str.trim().contains("DR   GO")) {
                        if (GO != ""){
                            GO += "; ";
                        }
                        if (GOtext != ""){
                            GOtext += "; ";
                        }
                        String goid = str.substring(12, 18);
                        String gotext = str.substring(21);
                        gotext = gotext.substring(0,gotext.indexOf(";"));
                        GO += goid;
                        GOtext += gotext;
                    } else if (str.trim().contains("DR   InterPro")) {
                        if (Interpro != ""){
                            Interpro += "; ";
                        }
                        Interpro += str.substring(15).replace("; "," (").replace(".", ")");
                    } else if (str.trim().contains("DR   Pfam")) {
                        if (Pfam != ""){
                            Pfam += "; ";
                        }
                        Pfam += str.substring(11).replaceFirst("; "," (").replace(".", ")");
                    } else if (str.trim().contains("KW   ")) {
                        String keytemp = str.split("   ")[1];
                        if (keytemp.contains("{")){
                        Keywords += keytemp.substring(0,keytemp.indexOf("{"));
                        } else {
                            Keywords += keytemp;
                        }
                    } else if (str.trim().contains("CC  ")) {
                        String ccline = str.substring(3);
                        if (ccline.contains("-!-")){
                            int splitindex = ccline.indexOf(":");
                            activekey = ccline.substring(6,splitindex);
                            String restline = "";
                            try{
                            restline = ccline.substring(splitindex+2).trim();
                            } catch (Exception ex){}
                            CCdata.put(activekey, restline);
                        } else if (ccline.contains("    ")){
                            CCdata.put(activekey, (CCdata.get(activekey)+ccline.substring(1)).trim());
                        }   
                    } else if (str.trim().contains("RT   ")) {
                        Discription += str.substring(4).trim().replace("\"", "");
                    } } catch (Exception ex) {
                        System.out.println("problem at "+str+"\t"+ex);
                    }
                }
             
                list.clear();
                if (go){
                    String uniprotstring = Recname+"\t"+Status+"\t"+Discription+"\t"+Taxlineage+"\t"+Organism+"\t"+Organismextra+"\t"+Refseq1+"\t"+Refseq2+"\t"+EMBLgenome+"\t"+EMBLprotein+"\t"+gistring+"\t"+KEGG+"\t"+Interpro+"\t"+Pfam+"\t"+GO+"\t"+GOtext+"\t"+Keywords+"\t"+Orfnames;          
                    uniprotstring += "\t"+CCdata.get("ALLERGEN");
                    uniprotstring += "\t"+CCdata.get("ALTERNATIVE PRODUCTS");
                    uniprotstring += "\t"+CCdata.get("BIOPHYSICOCHEMICAL PROPERTIES");
                    uniprotstring += "\t"+CCdata.get("BIOTECHNOLOGY");
                    uniprotstring += "\t"+CCdata.get("CATALYTIC ACTIVITY");
                    uniprotstring += "\t"+CCdata.get("CAUTION");
                    uniprotstring += "\t"+CCdata.get("COFACTOR");
                    uniprotstring += "\t"+CCdata.get("DEVELOPMENTAL STAGE");
                    uniprotstring += "\t"+CCdata.get("DISEASE");
                    uniprotstring += "\t"+CCdata.get("DISRUPTION PHENOTYPE");
                    uniprotstring += "\t"+CCdata.get("ENZYME REGULATION");
                    uniprotstring += "\t"+CCdata.get("FUNCTION");
                    uniprotstring += "\t"+CCdata.get("INDUCTION");
                    uniprotstring += "\t"+CCdata.get("INTERACTION");
                    uniprotstring += "\t"+CCdata.get("MASS SPECTROMETRY");
                    uniprotstring += "\t"+CCdata.get("MISCELLANEOUS");
                    uniprotstring += "\t"+CCdata.get("PATHWAY");
                    uniprotstring += "\t"+CCdata.get("PHARMACEUTICAL");
                    uniprotstring += "\t"+CCdata.get("POLYMORPHISM");
                    uniprotstring += "\t"+CCdata.get("PTM");
                    uniprotstring += "\t"+CCdata.get("RNA EDITING");
                    uniprotstring += "\t"+CCdata.get("SEQUENCE CAUTION");
                    uniprotstring += "\t"+CCdata.get("SIMILARITY");
                    uniprotstring += "\t"+CCdata.get("SUBCELLULAR LOCATION");
                    uniprotstring += "\t"+CCdata.get("SUBUNIT");
                    uniprotstring += "\t"+CCdata.get("TISSUE SPECIFICITY");
                    uniprotstring += "\t"+CCdata.get("TOXIC DOSE");
                    uniprotstring += "\t"+CCdata.get("WEB RESOURCE");
                    uniprotstring = uniprotstring.replace("null", "");
                    go = false;
                    uniprotdata.put(Uniprotid, uniprotstring); 
                    list.clear();
                    if (ramcheck){
                        String usagestring = printUsage().toString();
                        BigInteger usage = new BigInteger(usagestring);
                        BigInteger limit = new BigInteger("7085878272");
                        int compareTo = usage.compareTo(limit);
                        if (compareTo == 1){
                            ramcheck = false;
                            itemlimit = uniprotdata.size();
                            System.out.println("#UNIPROT-ANNOTATOR\t RAM almost full, annotating with subset");
                            for (File file : inputfilesplit){
                                threadsactive ++;
                                File tempfile = new File(annotationdirectory.getAbsolutePath()+File.separator+file.getName()+".temp");
                                new Writethread(uniprotdata,tempfile,annotationdirectory,12,false,true,".ᚒU",46).start();
                                }
                                while (threadsactive > threadsdone){ try {
                                        Thread.sleep(1);
                                    } catch (InterruptedException ex) {Logger.getLogger(Uniprotmainthread.class.getName()).log(Level.SEVERE, null, ex);}}
                                threadsactive = 0;
                                threadsdone = 0;
                                uniprotdata.clear();
                                System.out.println("clear");
                                System.gc();
                            }
                        } else if (uniprotdata.size() > itemlimit){
                            System.out.println("#UNIPROT-ANNOTATOR\t RAM almost full, annotating with subset");
                            for (File file : inputfilesplit){
                                threadsactive ++;
                                File tempfile = new File(annotationdirectory.getAbsolutePath()+File.separator+file.getName()+".temp");
                                new Writethread(uniprotdata,tempfile,annotationdirectory,12,false,false,".ᚒU",46).start();
                                
                                while (threadsactive > threadsdone){ try {
                                        Thread.sleep(1);
                                    } catch (InterruptedException ex) {Logger.getLogger(Uniprotmainthread.class.getName()).log(Level.SEVERE, null, ex);}}
                                threadsactive = 0;
                                threadsdone = 0;
                            }
                                uniprotdata.clear();
                                System.out.println("clear");
                                System.gc();
                            }
                }        
            } else {
                list.add(line);
            }
        }

        }
            System.out.println("#UNIPROT-ANNOTATOR\t final annotation");
            System.out.println(uniprotdata.size()+" Uniprot ID's found in annotation files");
            for (File file : inputfilesplit){ 
                threadsactive ++;
                File tempfile = new File(annotationdirectory.getAbsolutePath()+File.separator+file.getName()+".temp");
                new Writethread(uniprotdata,tempfile,annotationdirectory,12,true,true,".ᚒU",46).start();
                
                
                while (threadsactive > threadsdone){ try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {Logger.getLogger(Uniprotmainthread.class.getName()).log(Level.SEVERE, null, ex);}}
                tempfile.delete();
                threadsactive = 0;
                threadsdone = 0;
            }
                uniprotdata.clear();
                System.out.println("clear");
                System.out.println("actual linecount: "+linecount2);
                System.gc();
    }
       
    /**
     *
     * @param uniprotidfile
     */
    public void readrequiredidlines(File uniprotidfile){
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(uniprotidfile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CazyAnnotator.class.getName()).log(Level.SEVERE, null, ex);
        }
        Scanner sc = new Scanner(inputStream, "UTF-8");
        sc = new Scanner(inputStream, "UTF-8");
        List<String> list = new ArrayList<String>();
        LineNumberReader lnr;
        Long maxlines = 0l;
        try {
            lnr = new LineNumberReader(new FileReader(uniprotidfile));
            lnr.skip(Long.MAX_VALUE);
            maxlines = Long.valueOf(lnr.getLineNumber());
        } catch (IOException ex) {
            Logger.getLogger(UniprotThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        Uniprotmainthread.addTotal(maxlines);
        System.out.println("added "+maxlines+" to remaining progress");
        Uniprotmainthread.setProcess("linking UniProt ID's");
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            Uniprotmainthread.addDone();
            String[] splitline = line.split("\t"); 
            if (splitline[1].equals("GI")){
                if (requiredgis.contains(Integer.parseInt(splitline[2]))){
                    try{
                    requireduniprotids.add(splitline[0]);
                    gitouniprot.put(splitline[2], splitline[0]);}
                    catch (Exception ex){}
                }
            } 
            if (splitline[1].equals("RefSeq")){
                if (requiredgenbank.contains(splitline[2])){
                    try{
                    requireduniprotids.add(splitline[0]);
                    gitouniprot.put(splitline[2], splitline[0]);}
                    catch (Exception ex){}
                }
            }
    }
        System.out.println("done");
    }
    
    /**
     *
     * @param inputfilesplit
     */
    public void readrequiredgis(ArrayList<File> inputfilesplit){
        for (File file : inputfilesplit){
            threadsactive ++;
            new Requiredgithread(file).start();
        }
        while (threadsactive > threadsdone){
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(Uniprotmainthread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        threadsactive = 0;
        threadsdone = 0;
    }

    /**
     *
     */
    public static void myThreaddone() {
    lock.lock();
    try {
        Uniprotmainthread.threadsdone ++;;
    } finally {
        lock.unlock();
    }
    }

    /**
     *
     * @param requiredgis
     */
    public static void addRequiredgis(HashSet<Integer> requiredgis) {
    lock.lock();
    try {
        Uniprotmainthread.requiredgis.addAll(requiredgis);
    } finally {
        lock.unlock();
    }
    }

    /**
     *
     * @param requiredgenbank
     */
    public static void addRequiredgenbank(HashSet<String> requiredgenbank) {
    lock.lock();
    try {
        Uniprotmainthread.requiredgenbank.addAll(requiredgenbank);
    } finally {
        lock.unlock();
    }
    }

    /**
     *
     * @param process
     */
    public static void setProcess(String process) {
    lock.lock();
    try {
        Uniprotmainthread.process = process;
    } finally {
        lock.unlock();
    }
    }

    /**
     *
     * @param lines
     */
    public static void setTotal(Long lines) {
    lock.lock();
    try {
        Uniprotmainthread.total = lines;
    } finally {
        lock.unlock();
    }
    }

    /**
     *
     * @param lines
     */
    public static void addTotal(Long lines) {
    lock.lock();
    try {
        Uniprotmainthread.total += lines;
    } finally {
        lock.unlock();
    }
    }

    /**
     *
     * @param done
     */
    public static void setDone(Long done) {
    lock.lock();
    try {
        Uniprotmainthread.done = done;
    } finally {
        lock.unlock();
    }
    }

    /**
     *
     */
    public static void addDone() {
    lock.lock();
    try {
        Uniprotmainthread.done++;
    } finally {
        lock.unlock();
    }
    }

    /**
     *
     * @return
     */
    public static Long getDone() {
    Long done = 0l;
    lock.lock();
    try {
        done = Uniprotmainthread.done;
    } finally {
        lock.unlock();  
    }
    return done;
    }

    /**
     *
     * @return
     */
    public static Long getTotal() {
    Long total = 0l;
    lock.lock();
    try {
        total = Uniprotmainthread.total;
    } finally {
        lock.unlock();  
    }
    return total;
    }

    /**
     *
     * @return
     */
    public static Long getProgress() {
    Long progress = 0l;
    lock.lock();
    try {
        progress = Uniprotmainthread.done/Uniprotmainthread.total;
    } finally {
        lock.unlock();  
    }
    return progress;
    }

    /**
     *
     * @return
     */
    public static String getProcess() {
    String process = "";
    lock.lock();
    try {
        process = Uniprotmainthread.process;
    } finally {
        lock.unlock();  
    }
    return process;
    }

    /**
     *
     * @param complete
     */
    public static void setComplete(Boolean complete) {
    lock.lock();
    try {
        Uniprotmainthread.complete = complete;
    } finally {
        lock.unlock();
    }
    }

    /**
     *
     * @return
     */
    public static Boolean getComplete() {
    lock.lock();
    try {
    } finally {
        lock.unlock();
    }
        return Uniprotmainthread.complete;
    }
    
    private static Object printUsage() {
  OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
  Object myvalue = null;
  for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) {
    method.setAccessible(true);


    if (method.getName().startsWith("getFreePhysicalMemorySize") 
        && Modifier.isPublic(method.getModifiers())) {
            
        try {
            myvalue = method.invoke(operatingSystemMXBean);
        } catch (Exception e) {
            myvalue = e;
        } // try
       
    } // if
  } // for
        
   return myvalue;
}
}
