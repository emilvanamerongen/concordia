/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author emil
 */
public class databasechecker {
   
    public static void main(String[] args) throws SQLException{
        
        Connection con = null;   
        con = DriverManager.getConnection("jdbc:derby:NGSDB");
        Statement sta = con.createStatement(); 
        //check database
        System.out.println("Check what's in the database:");
        ResultSet res = sta.executeQuery(
            "SELECT * FROM APP.READS");
            System.out.println("Read contents: "); 
            while (res.next()) {
                System.out.println(
               "  "+res.getInt("READ_ID")
               + ", "+res.getString("SEQUENCE")
               + ", "+res.getString("QUALITY_VALUES")
               + ", "+res.getBoolean("READ_DIRECTION")
               + ", "+res.getInt("SEQUENCE_ID")
               + ", "+res.getInt("HEADER_ID")
               + ", "+res.getString("HEADER"));
                
            }
        
    }
}
