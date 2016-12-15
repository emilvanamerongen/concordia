/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;
/**
parcing the info from the GUI to be used by other modules
@author theox
*/
import Objects.NGSread;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class dataparser {
/**
*Used to open given file name and extract the sequences and sequence headers
*@param file the location of the file that has to be opened
*@return returns a nested array with all the data in it for the database
**/
   private File importfile;
   private String[] importstring;
   private String filetype;
   public processthread thread;
   public Connection con = null;
   
    //constructors
    public dataparser(File importfile, String filetype){
       this.importfile = importfile;
       this.filetype = filetype;
    } 
    public dataparser(String[] importstring, String filetype){
       this.importstring = importstring;
       this.filetype = filetype;
    }
   
    public void process(String headidentifier, String forwardindicator, String reverseindicator, String selecteddataset, Boolean indicatoroff, String originfile) throws IOException{
       processthread thread = new processthread(headidentifier, forwardindicator, reverseindicator, selecteddataset, indicatoroff, originfile);
       thread.start();
    }
   
    public class processthread extends Thread {
    //parameters
    private final String headidentifier;
    private final String forwardindicator;
    private final String reverseindicator;
    private final String selecteddataset;
    private final Boolean indicatoroff;
    private String originfile = "none";
    //data
    private int seqnumber;
    private double done;
    private int total;
    int linenr = 0;
    DecimalFormat df = new DecimalFormat("#.##");
    
    importData importdata = new importData();
    dbcon dbconnector = new dbcon();
    NGSread newread = new NGSread();
    
    public processthread(String headidentifier, String forwardindicator, String reverseindicator, String selecteddataset, Boolean indicatoroff, String originfile) {
        this.headidentifier = headidentifier;
        this.forwardindicator = forwardindicator;
        this.reverseindicator = reverseindicator;
        this.selecteddataset = selecteddataset;
        this.indicatoroff = indicatoroff;
        this.originfile = originfile;
        
    }

    public void run(){
       System.out.println("processing data on new thread");      
       System.out.println("##################");
       System.out.println("parameters:");
       System.out.println("head identifier: "+headidentifier);
       if (! indicatoroff){
       System.out.println("forward indicator: "+forwardindicator);
       System.out.println("reverse indicator: "+reverseindicator);
       }
       System.out.println("##################");
       
       if (importfile != null){
            System.out.println("File found");
            GUIController.parserloadlabel = "Calculating Filesize";
            FileInputStream inputStream = null;
            try {
               inputStream = new FileInputStream(importfile.getAbsolutePath());
            } catch (FileNotFoundException ex) {
               Logger.getLogger(dataparser.class.getName()).log(Level.SEVERE, null, ex);
            }
            Scanner sc = new Scanner(inputStream, "UTF-8");
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine()) {
            sc.nextLine();
            total+=1;
            }
            sc.close();
            total = total/4;
            GUIController.parserloadlabel = "importing "+total+" sequences";
            
            inputStream = null;
            try {
               inputStream = new FileInputStream(importfile.getAbsolutePath());
            } catch (FileNotFoundException ex) {
               Logger.getLogger(dataparser.class.getName()).log(Level.SEVERE, null, ex);
            }
            sc = new Scanner(inputStream, "UTF-8");
            
            try {
                openConn();
                    while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                        try {             
                            processline(line);
                        } catch (SQLException ex) {
                            Logger.getLogger(dataparser.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        String format = df.format(done/total);
                        try {
                        GUIController.parserloadbar.setdone(Double.parseDouble(format));}
                        catch (NumberFormatException ex) {
                        GUIController.parserloadbar.setdone(Double.parseDouble(format.replace(",",".")));
                    }
                    } closeConn(con); } catch (SQLException ex) {
                       Logger.getLogger(dataparser.class.getName()).log(Level.SEVERE, null, ex);
           }
            
            try {
               inputStream.close();
            } catch (IOException ex) {
               Logger.getLogger(dataparser.class.getName()).log(Level.SEVERE, null, ex);
            }
            
           
       }
       else if (importstring != null){
            System.out.println("text found");
           
            for (String line : importstring){
               if (line.matches("^"+headidentifier+".*")){
                total+=1;
            }
                          
            }
            try {
                openConn();
                    for (String line : importstring){
                        try {
                            processline(line);
                        } catch (SQLException ex) {
                            Logger.getLogger(dataparser.class.getName()).log(Level.SEVERE, null, ex);
                        }                              
                    }
            closeConn(con); } catch (SQLException ex) {
               Logger.getLogger(dataparser.class.getName()).log(Level.SEVERE, null, ex);
           }
       }
        try {
            //dbconnector.getdatabasecontents();
            importdata.getdatabasecontents();
        } catch (SQLException ex) {
            Logger.getLogger(dataparser.class.getName()).log(Level.SEVERE, null, ex);
        }
       GUIController.timelineactive = false;
       
    }
    
    
    public void processline(String line) throws SQLException{
        linenr += 1;
        if (line.matches("^"+headidentifier+".*")){
                linenr = 0;
                if (newread.getHeader() != null){
                    //dbconnector.importDatabaseInfo(selecteddataset,newread.getHeader(), newread.getSequence(), newread.getQualityvalues(), newread.getReaddirection(), originfile);
                    importdata.importCombinedTable(con, selecteddataset, newread.getHeader(), newread.getSequence(), newread.getQualityvalues(), newread.getReaddirection(), originfile);
                    newread.clear();
                    done += 1;
                }
                newread.setHeader(line);   
                if (! indicatoroff){
                    newread.setHeader(line.substring(0,line.length()-2));
                    if (line.matches(".*"+forwardindicator+".*")){
                        newread.setReaddirection(false);
                    } else if (line.matches(".*"+reverseindicator+".*")){
                        newread.setReaddirection(true);
                    } 
                }
            } 
        if (linenr == 1){
        if (line.matches("[ATCGUatcgu]*")) {
            newread.setSequence(line);
        }}
        
        if (linenr == 3) {
            newread.setQualityvalues(line);
            
        }
    }
    
    public void openConn() throws SQLException{ 
        con = DriverManager.getConnection("jdbc:derby:NGSDB");
    }
    
    public void closeConn(Connection con) throws SQLException{
        con.commit();
        con.close();
    }
    
    }
}
