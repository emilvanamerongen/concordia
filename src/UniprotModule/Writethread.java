/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UniprotModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil3
 */
public class Writethread extends Thread{
    private HashMap<String, String> annotationdata;
    private final File inputfile;
    private final File outputdirectory;
    private Integer linkposition;
    private Boolean lastrun;
    private Boolean firstrun;
    private String extension;
    private Integer tabcount;
    
    Writethread(HashMap<String, String> annotationdata, File inputfile, File outputdirectory, Integer linkposition, Boolean firstrun, Boolean lastrun, String extension, Integer tabcount){
        this.annotationdata = annotationdata;
        this.inputfile = inputfile;
        this.linkposition = linkposition;
        this.extension = extension;
        this.tabcount = tabcount;
        this.outputdirectory = outputdirectory;
        this.lastrun = lastrun;
        this.firstrun = firstrun;
    }
    
    public void run(){
        File outputfile = null;
        if (!lastrun){
            outputfile = new File(outputdirectory.getAbsolutePath()+File.separator+inputfile.getName()+".temp");
        } else{
            outputfile = new File(outputdirectory.getAbsolutePath()+File.separator+inputfile.getName().replace(".temp", "")+extension);
        }
        FileInputStream stream = null;
        FileOutputStream outputstream = null;
        try {
            stream = newfilestream();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UniprotThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            outputstream = new FileOutputStream(outputfile.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UniprotThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        Scanner sc = new Scanner(stream, "UTF-8");
        while (sc.hasNextLine()) {
            // LOOP over regels van bestand
            String line = sc.nextLine();
            try {
            String[] splitline = line.split("\t");         
            if (linkposition == 1){
                String giitem = line.split("\t")[1];
                String gi = giitem.substring(3, giitem.indexOf("ref")-1); 
                String genbank = ""; 
                genbank = giitem.substring(giitem.indexOf("ref")+4, giitem.length()-1);
                if (annotationdata.containsKey(genbank)){
                    line += "\t"+annotationdata.get(genbank);
                } else if (annotationdata.containsKey(gi)){
                    line += "\t"+annotationdata.get(gi);
                }  
            } else {
            if (line.contains("log(e-value)	bit-score")){
                line += "   [UniProt] ID	[UniProt] Recname	[UniProt] Status	[UniProt] Discription	[UniProt] Taxlineage	[UniProt] Organism	[UniProt] Organismextra	[UniProt] Refseq1	[UniProt] Refseq2	[UniProt] EMBLgenome	[UniProt] EMBLprotein	[UniProt] gistring	[UniProt] KEGG	[UniProt] Interpro	[UniProt] Pfam	[UniProt] GO	[UniProt] GOtext	[UniProt] Keywords	[UniProt] Orfnames	[UniProt] ALLERGEN	[UniProt] ALTERNATIVE PRODUCTS	[UniProt] BIOPHYSICOCHEMICAL PROPERTIES	[UniProt] BIOTECHNOLOGY	[UniProt] CATALYTIC ACTIVITY	[UniProt] CAUTION	[UniProt] COFACTOR	[UniProt] DEVELOPMENTAL STAGE	[UniProt] DISEASE	[UniProt] DISRUPTION PHENOTYPE	[UniProt] ENZYME REGULATION	[UniProt] FUNCTION	[UniProt] INDUCTION	[UniProt] INTERACTION	[UniProt] MASS SPECTROMETRY	[UniProt] MISCELLANEOUS	[UniProt] PATHWAY	[UniProt] PHARMACEUTICAL	[UniProt] POLYMORPHISM	[UniProt] PTM	[UniProt] RNA EDITING	[UniProt] SEQUENCE CAUTION	[UniProt] SIMILARITY	[UniProt] SUBCELLULAR LOCATION	[UniProt] SUBUNIT	[UniProt] TISSUE SPECIFICITY	[UniProt] TOXIC DOSE	[UniProt] WEB RESOURCE";}
            String linkid = splitline[linkposition];
            if (annotationdata.containsKey(linkid)){
                line += "\t"+annotationdata.get(linkid);
                                
            } else if (lastrun){
                Integer count = 0;
                while (count++ <= tabcount){
                    line += "\t";
                }
            }
            }
            }catch (Exception ex){
            }
            try {
                outputstream.write((line+"\n").getBytes());
                outputstream.flush();
            } catch (IOException ex) {
                Logger.getLogger(UniprotThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        try {
                outputstream.close();
            } catch (IOException ex) {
                Logger.getLogger(UniprotThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        if (!firstrun){
            String inputfilepath = inputfile.getAbsolutePath();
            inputfile.delete();
            outputfile.renameTo(new File (inputfilepath));
        }
        Uniprotmainthread.myThreaddone();
    }
    
    private FileInputStream newfilestream () throws FileNotFoundException{
        FileInputStream inputStream = null;
        inputStream = new FileInputStream(inputfile.getAbsolutePath());
        return inputStream;
    }
    
}
