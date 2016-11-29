package concordia;

/**
 *
 * @author Alex
 */

import Objects.dataset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

public class DBconn{
    
    
    public static void ADDToDatabase(){
        try{
        String framework = "embedded";
        String protocol = "jdbc:derby:";
        Connection con = null;
        String dbName = "concordiaDB";
        con = DriverManager.getConnection("jdbc:derby:NGSDB");
        Statement sta = con.createStatement(); 

        // getting the data back
        ResultSet res = sta.executeQuery("SELECT * FROM APP.READS");
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
    } catch (Exception ex){
            System.out.println(ex);
    }
    

}
    public static void main(String [] args){
        ADDToDatabase();
    }
}
