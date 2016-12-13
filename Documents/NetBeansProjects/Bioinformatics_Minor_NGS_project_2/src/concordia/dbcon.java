package concordia;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

public class dbcon {
    

    public void importDatabaseInfo(String selectedcollection, String header, String sequence, String qualityvalues, Boolean readrichting, String originfile) throws SQLException{
      Connection con = null;   
      con = DriverManager.getConnection("jdbc:derby:NGSDB");
      Statement sta = con.createStatement(); 
      
      int newheaderid = 1;
      int newsequenceid = 1;
      int check = 1;
      int newreadid = 1;
      int fileid = 1;
      int collectionid = getcollection(con, selectedcollection);
      newheaderid = newheaderid(con);
      newsequenceid = newsequenceid(con);
      newreadid = newreadid(con);
      check = checkheader(con,header);
         
      if (check < newheaderid){
          newheaderid = check;
      }    
      //read insert query
      String insertquery = "INSERT INTO READS" + "(READ_ID,SEQUENCE,QUALITY_VALUES,SEQUENCE_ID,READ_DIRECTION,HEADER_ID)" + " VALUES (?,?,?,?,?,?)";
      PreparedStatement prepStmt = con.prepareStatement(insertquery);
      prepStmt.setInt(1, newreadid);
      prepStmt.setString(2, sequence);
      prepStmt.setString(3, qualityvalues);
      prepStmt.setInt(4, newsequenceid);
      prepStmt.setBoolean(5, readrichting);
      prepStmt.setInt(6, newheaderid);
      prepStmt.executeUpdate();
      //header insert query      
      String insertheaderquery = "INSERT INTO HEADER (HEADER_ID,COLLECTION_ID,FILE_ID,HEADER) VALUES(?,?,?,?)";
      PreparedStatement headerstatement = con.prepareStatement(insertheaderquery);
      headerstatement.setInt(1, newheaderid);
      headerstatement.setInt(2, collectionid);
      headerstatement.setInt(3, fileid);
      headerstatement.setString(4, header);
      headerstatement.executeUpdate();
      //collection insert query
      String insertcollectionquery = "INSERT INTO COLLECTION (COLLECTION_ID,COLLECTION_TITLE) VALUES(?,?)";
      PreparedStatement insertcollectionstatement = con.prepareStatement(insertcollectionquery);
      insertcollectionstatement.setInt(1, collectionid);
      insertcollectionstatement.setString(2, selectedcollection);
      insertcollectionstatement.executeUpdate();
      //file insert query
      String filequery = "INSERT INTO FILE (FILE_ID,FILE_TITLE) VALUES(?,?)";
      PreparedStatement filestatement = con.prepareStatement(filequery);
      filestatement.setInt(1, fileid);
      filestatement.setString(2, originfile);
      filestatement.executeUpdate();
      
      con.commit();
      sta.close();
      con.close();
}
    public int newheaderid (Connection con) throws SQLException{
        int newid = 0;
        String basequery = "SELECT MAX(HEADER_ID) from HEADER";
        PreparedStatement prepStmt = con.prepareStatement(basequery);
        ResultSet result = prepStmt.executeQuery();
        while (result.next()) {
            newid = result.getInt(1);
    };
        return newid+1;           
    }
    
    public int newsequenceid(Connection con) throws SQLException{
        int newid = 0;
        String basequery = "SELECT MAX(SEQUENCE_ID) from READS";
        PreparedStatement prepStmt = con.prepareStatement(basequery);
        ResultSet result = prepStmt.executeQuery();
        while (result.next()) {
            newid = result.getInt(1);
    };
        return newid+1;    
    }
    public int newreadid(Connection con) throws SQLException{
        int newid = 0;
        String basequery = "SELECT MAX(READ_ID) from READS";
        PreparedStatement prepStmt = con.prepareStatement(basequery);
        ResultSet result = prepStmt.executeQuery();
        while (result.next()) {
            newid = result.getInt(1);
    };
        return newid+1;    
    }
    
    public int checkheader(Connection con,String header) throws SQLException{
        int newid = 0;
        String basequery = "SELECT HEADER_ID from HEADER WHERE HEADER = ?";
        
        PreparedStatement prepStmt = con.prepareStatement(basequery);
        prepStmt.setString(1, header);
        ResultSet result = prepStmt.executeQuery();
        while (result.next()) {
            newid = result.getInt(1);
        };
        return newid;   
    }
    
    public int getcollection(Connection con, String collectiontitle) throws SQLException{
        int collectionid = 0;
        //get new id
        String basequery = "SELECT MAX(COLLECTION_ID) from COLLECTION"; 
        PreparedStatement prepStmt = con.prepareStatement(basequery);
        ResultSet result = prepStmt.executeQuery(); 
        while (result.next()) {
            collectionid = result.getInt(1)+1;
        };
        //check if collection already exists
        String basequery2 = "SELECT COLLECTION_ID from COLLECTION where COLLECTION_TITLE = ?"; 
        PreparedStatement prepStmt2 = con.prepareStatement(basequery2);
        prepStmt2.setString(1, collectiontitle);
        ResultSet result2 = prepStmt2.executeQuery(); 
        while (result2.next()) {
            collectionid = result2.getInt(1);
        };
        
        return collectionid;
    }
    
    public int getfileid(Connection con, String originfile) throws SQLException{
        int fileid = 0;
        //get new id
        String basequery = "SELECT MAX(FILE_ID) from FILE"; 
        PreparedStatement prepStmt = con.prepareStatement(basequery);
        prepStmt.setString(1, originfile);
        ResultSet result = prepStmt.executeQuery(); 
        while (result.next()) {
            fileid = result.getInt(1)+1;
        };
        //check if collection already exists
        String basequery2 = "SELECT FILE_ID from FILE where FILE_TITLE = ?"; 
        PreparedStatement prepStmt2 = con.prepareStatement(basequery);
        prepStmt2.setString(1, originfile);
        ResultSet result2 = prepStmt2.executeQuery(); 
        while (result2.next()) {
            fileid = result2.getInt(1);
        };
        
        return fileid;
    }
    
    
    public void getdatabasecontents() throws SQLException{
        
        Connection con = null;   
        con = DriverManager.getConnection("jdbc:derby:NGSDB");
        Statement sta = con.createStatement(); 
        //check database
        System.out.println("Check what's in the database:");
        ResultSet res = sta.executeQuery(
            "SELECT * FROM APP.READS NATURAL JOIN HEADER");
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
