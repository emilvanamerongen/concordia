package concordia;

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
      
      
      int c = sta.executeUpdate("INSERT INTO HEADER"
        + " (HEADER_ID, DATASET_ID, HEADER)"
        + " VALUES (headerid++, collectionid, header)");
      
      c = sta.executeUpdate("INSERT INTO READS"
        + " (READ_ID, SEQUENCE, QUALITY_VALUES, READ_DIRECTION, SEQUENCE_ID)"
        + " VALUES (read_ID, sequence, qualityvalues, readrichting, sequenceid++)");

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
