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
    
    public int datasetid = 1;
    public int headerid = 1;
    public int readid = 1;
    public int sequenceid = 1;

    public void importReadsTabel(String datasettitle, String header, String sequence, String qualityvalues, Boolean readrichting) throws SQLException{
      String framework = "embedded";
      String protocol = "jdbc:derby:";
      Connection con = null;
      String dbName = "NGSDB";
      con = DriverManager.getConnection("jdbc:derby:NGSDB");
      Statement sta = con.createStatement(); 
      
      int c = sta.executeUpdate("INSERT INTO DATASET"
        + " (DATASET_ID, DATASET_TITLE)"
        + " VALUES (datasetid++, datasettitle)");
      
 
      c = sta.executeUpdate("INSERT INTO HEADER"
        + " (HEADER_ID, DATASET_ID, HEADER)"
        + " VALUES (headerid++, datasetid, header)");
      
      c = sta.executeUpdate("INSERT INTO READS"
        + " (READ_ID, SEQUENCE, QUALITY_VALUES, READ_RICHTING, SEQUENCE_ID)"
        + " VALUES (read_ID, sequence, qualityvalues, readrichting, sequenceid++)");

      sta.close();
      con.close();  
}
    
    
}
