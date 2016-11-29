package concordia;


/**
 *
 * @author Alex
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

public class DerbyQuery {
    /* the default framework is embedded */

    
  public static void main(String [] args) throws SQLException {    
    String framework = "embedded";
    String protocol = "jdbc:derby:";
    Connection con = null;
      String dbName = "NGSDB";
      con = DriverManager.getConnection("jdbc:derby:NGSDB");
      Statement sta = con.createStatement(); 

      //Dit werkt voor het inserten van data in de NGSDB.
      /**
      int count = 0;
      int c = sta.executeUpdate("INSERT INTO READS"
        + " (READ_ID, SEQUENCE, QUALITY_VALUES, READ_RICHTING, SEQUENCE_ID)"
        + " VALUES (556, 'ATGCTGCAAA', '*&^%$#@#$', false, 13)");
      count = count + c;
 
      c = sta.executeUpdate("INSERT INTO READS"
        + " (READ_ID, SEQUENCE, QUALITY_VALUES, READ_RICHTING, SEQUENCE_ID)"
        + " VALUES (667, 'TGAAAGCTAD', '^##%^*&$#$%$', true, 12)");
      count = count + c;
      System.out.println("Number of rows inserted: "+count);
      **/
      
    // getting the data back
      ResultSet res = sta.executeQuery(
        "SELECT * FROM APP.READS");
      System.out.println("Read contents: "); 
      while (res.next()) {
         System.out.println(
           "  "+res.getInt("READ_ID")
           + ", "+res.getString("SEQUENCE")
           + ", "+res.getString("QUALITY_VALUES")
           + ", "+res.getBoolean("READ_RICHTING")
           + ", "+res.getInt("SEQUENCE_ID"));
      }
      res.close();

      sta.close();
      con.close();    
  }
}