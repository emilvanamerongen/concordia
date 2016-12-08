/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Alex
 */
public class CreateDatabase {
    public void createDataTables() throws SQLException{
        
        String framework = "embedded";
        String protocol = "jdbc:derby:";
        Connection con = null;
        String dbName = "NGSDB";
        Boolean recordAdded = false;
        con = DriverManager.getConnection("jdbc:derby:NGSDB;create=true");

        String createCollectionTable = "CREATE TABLE COLLECTION (COLLECTION_ID INTEGER NOT NULL, COLLECTION_TITLE VARCHAR(1000) NOT NULL)";
        String createHeaderTable = "CREATE TABLE HEADER (HEADER_ID INTEGER NOT NULL, COLLECTION_ID INTEGER NOT NULL, HEADER VARCHAR(1000) NOT NULL)";
        String createReadsTable = "CREATE TABLE READS (READ_ID INTEGER NOT NULL, SEQUENCE VARCHAR(10000) NOT NULL, QUALITY_VALUES VARCHAR(10000) NOT NULL, SEQUENCE_ID INTEGER NOT NULL, READ_DIRECTION BOOLEAN NOT NULL, HEADER_ID INTEGER NOT NULL)";
        Statement stmt = con.createStatement();
        

        DatabaseMetaData md = con.getMetaData();
        ResultSet rs = md.getTables(null, null, "COLLECTION", null);
        if (!rs.next()) {
            stmt.executeUpdate(createCollectionTable);
            stmt.executeUpdate(createHeaderTable);
            stmt.executeUpdate(createReadsTable);
            System.out.println("Database created");
        } else {
            System.out.println("Tables already exist");
        }
        
        con.close(); 
        
    } 
}

