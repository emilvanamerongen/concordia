package concordia;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


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
      String dbName = "concordiaDB";
      con = DriverManager.getConnection("jdbc:derby:NGSDB");
      Statement sta = con.createStatement(); 

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