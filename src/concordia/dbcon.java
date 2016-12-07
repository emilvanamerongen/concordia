package concordia;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

public class dbcon {
    
    private int collectionid = 1;
    private int headerid = 1;
    private int readid = 1;
    private int sequenceid = 1;

    public void importDatabaseInfo(String selectedcollection, String header, String sequence, String qualityvalues, Boolean readrichting) throws SQLException{
      String framework = "embedded";
      String protocol = "jdbc:derby:";
      Connection con = null;
      String dbName = "NGSDB";
      con = DriverManager.getConnection("jdbc:derby:NGSDB");
      Statement sta = con.createStatement(); 
      ResultSet headeridresult = sta.executeQuery("SELECT MAX(HEADER_ID) from HEADER");
      ResultSet readidresult = sta.executeQuery("SELECT MAX(READ_ID) from READS");
      int newheaderid = headeridresult.getInt(0)+1;
      ResultSet checkheaderidresult = sta.executeQuery("SELECT HEADER_ID from HEADER WHERE HEADER LIKE \"%"+header+"%\"");
      int check = headeridresult.getInt(0)+1;
      if (check < newheaderid){
          newheaderid = check;
      }
      int newreadid = readidresult.getInt(0)+1;
      System.out.println("newreadid = "+newreadid+" newheaderid = "+headerid);
//      int c = sta.executeUpdate("INSERT INTO HEADER"
//        + " (HEADER_ID, DATASET_ID, HEADER)"
//        + " VALUES (headerid++, datasetid, header)");
//      
//      c = sta.executeUpdate("INSERT INTO READS"
//        + " (READ_ID, SEQUENCE, QUALITY_VALUES, READ_DIRECTION, SEQUENCE_ID)"
//        + " VALUES (read_ID, sequence, qualityvalues, readrichting, sequenceid++)");

      sta.close();
      con.close();  
}
    
    public void checkDataset(String collectiontitle) throws SQLException{
        String framework = "embedded";
        String protocol = "jdbc:derby:";
        Connection con = null;
        String dbName = "NGSDB";
        Boolean recordAdded = false;
        con = DriverManager.getConnection("jdbc:derby:NGSDB");
        Statement sta = con.createStatement();

        ResultSet res = sta.executeQuery("SELECT COLLECTION_ID FROM READS WHERE COLLECTION_TITLE = \""+collectiontitle+"\"");
        
        
         
//        int d = sta.executeUpdate("INSERT INTO DATASET"
//            + " (DATASET_ID, DATASET_TITLE)"
//            + " VALUES (collectionid++, collectiontitle)");
        
        sta.close();
        con.close();
    }
    
    public void createDataTables() throws SQLException{
        String framework = "embedded";
        String protocol = "jdbc:derby:";
        Connection con = null;
        String dbName = "NGSDB";
        Boolean recordAdded = false;
        con = DriverManager.getConnection("jdbc:derby:NGSDB;create=true");
        Statement sta = con.createStatement();
        
        String createCollectionTable = "CREATE TABLE COLLECTION (COLLECTION_ID INTEGER NOT NULL, COLLECTION_TITLE VARCHAR(1000) NOT NULL)";
        String createHeaderTable = "CREATE TABLE HEADER (HEADER_ID INTEGER NOT NULL, COLLECTION_ID INTEGER NOT NULL, HEADER VARCHAR(1000) NOT NULL)";
        String createReadsTable = "CREATE TABLE READS (READ_ID INTEGER NOT NULL, SEQUENCE VARCHAR(10000) NOT NULL, QUALITY_VALUES VARCHAR(10000) NOT NULL, SEQUENCE_ID INTEGER NOT NULL, READ_DIRECTION BOOLEAN NOT NULL, HEADER_ID INTEGER NOT NULL)";
        Statement stmt = con.createStatement();
        stmt.executeUpdate(createCollectionTable);
    }
    
    
    
//    Work in Progress.
//    public void displayDatasets() throws SQLException{
//        Connection con = null;
//        con = DriverManager.getConnection("jdbc:derby:NGSDB");
//        Statement sta = con.createStatement();
//        
//        ResultSet res = sta.executeQuery(
//        "SELECT * FROM APP.READS");
//        System.out.println("Read contents: "); 
//        while (res.next()) {
//         System.out.println(
//           "  "+res.getInt("READ_ID")
//           + ", "+res.getString("SEQUENCE")
//           + ", "+res.getString("QUALITY_VALUES")
//           + ", "+res.getBoolean("READ_DIRECTION")
//           + ", "+res.getInt("SEQUENCE_ID"));
//        }
//        res.close();
//
//        sta.close();
//        con.close(); 
//    }
    
}
