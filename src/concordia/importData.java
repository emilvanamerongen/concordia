/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Alex
 */
public class importData {
    
    public void importCombinedTable(Connection con, String selectedcollection, String header, String sequence, String qualityvalues, Boolean readrichting, String originfile) throws SQLException{
        Statement sta = con.createStatement(); 

        int newheaderid = 1;
        int check = 1;
        int newreadid = 1;
        int collectionid = getcollection(con, selectedcollection);
        newreadid = newreadid(con);
        
        String insertquery = "INSERT INTO COMBINED" + "(READ_ID,SEQUENCE,QUALITY_VALUES,READ_DIRECTION,HEADER,COLLECTION_ID,COLLECTION_TITLE,FILE_TITLE)" + " VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement prepStmt = con.prepareStatement(insertquery);
        prepStmt.setInt(1, newreadid);
        prepStmt.setString(2, sequence);
        prepStmt.setString(3, qualityvalues);
        prepStmt.setBoolean(4, readrichting);
        prepStmt.setString(5, header);
        prepStmt.setInt(6, collectionid);
        prepStmt.setString(7, selectedcollection);
        prepStmt.setString(8, originfile);
        prepStmt.executeUpdate();

        sta.close();
    }
    
    public int newreadid(Connection con) throws SQLException{
        int newid = 0;
        String basequery = "SELECT MAX(READ_ID) from COMBINED";
        PreparedStatement prepStmt = con.prepareStatement(basequery);
        ResultSet result = prepStmt.executeQuery();
        while (result.next()) {
            newid = result.getInt(1);
    };
        return newid+1;    
    }
    
    public int getcollection(Connection con, String collectiontitle) throws SQLException{
        int collectionid = 0;
        //get new id
        String basequery = "SELECT MAX(COLLECTION_ID) from COMBINED"; 
        PreparedStatement prepStmt = con.prepareStatement(basequery);
        ResultSet result = prepStmt.executeQuery(); 
        while (result.next()) {
            collectionid = result.getInt(1)+1;
        };
        //check if collection already exists
        String basequery2 = "SELECT COLLECTION_ID from COMBINED where COLLECTION_TITLE = ?"; 
        PreparedStatement prepStmt2 = con.prepareStatement(basequery2);
        prepStmt2.setString(1, collectiontitle);
        ResultSet result2 = prepStmt2.executeQuery(); 
        while (result2.next()) {
            collectionid = result2.getInt(1);
        };
        
        return collectionid;
    }
    
    public void getdatabasecontents() throws SQLException{
        
        Connection con = null;   
        con = DriverManager.getConnection("jdbc:derby:NGSDB");
        Statement sta = con.createStatement(); 
        //check database
        System.out.println("Check what's in the database:");
        ResultSet res = sta.executeQuery(
            "SELECT * FROM APP.COMBINED");
            System.out.println("Read contents: "); 
            while (res.next()) {
                System.out.println(
               "  "+res.getInt("READ_ID")
               + ", "+res.getString("SEQUENCE")
               + ", "+res.getString("QUALITY_VALUES")
               + ", "+res.getBoolean("READ_DIRECTION")
               + ", "+res.getString("HEADER")
               + ", "+res.getInt("COLLECTION_ID")
               + ", "+res.getString("COLLECTION_TITLE")
               + ", "+res.getString("FILE_TITLE"));
                
            }
        
    }
}
