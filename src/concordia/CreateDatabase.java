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
    String dbName = "NGSDB";
    
    public void createDataTables() throws SQLException{
        String framework = "embedded";
        String protocol = "jdbc:derby:";
        Connection con = null;
        
        Boolean recordAdded = false;
        con = DriverManager.getConnection("jdbc:derby:NGSDB;create=true");


//        String createCollectionTable = "CREATE TABLE COLLECTION (COLLECTION_ID INTEGER NOT NULL, COLLECTION_TITLE VARCHAR(1000) NOT NULL)";
//        String createFileTable =  "CREATE TABLE FILE (FILE_ID INTEGER NOT NULL, FILE_TITLE LONG VARCHAR NOT NULL)";
//        String createHeaderTable = "CREATE TABLE HEADER (HEADER_ID INTEGER NOT NULL, COLLECTION_ID INTEGER NOT NULL, FILE_ID INTEGER NOT NULL, HEADER VARCHAR(1000) NOT NULL)";
//        String createReadsTable = "CREATE TABLE READS (READ_ID INTEGER NOT NULL, SEQUENCE LONG VARCHAR NOT NULL, QUALITY_VALUES LONG VARCHAR NOT NULL, SEQUENCE_ID INTEGER NOT NULL, READ_DIRECTION BOOLEAN NOT NULL, HEADER_ID INTEGER NOT NULL)";
        
        String createCombinedTable = "CREATE TABLE COMBINED (COLLECTION_ID INTEGER NOT NULL, COLLECTION_TITLE VARCHAR(1000) NOT NULL, "
                + "FILE_TITLE LONG VARCHAR NOT NULL, HEADER VARCHAR(1000) NOT NULL, "
                + "READ_ID INTEGER NOT NULL, SEQUENCE LONG VARCHAR NOT NULL, QUALITY_VALUES LONG VARCHAR NOT NULL, READ_DIRECTION BOOLEAN NOT NULL)";

        Statement stmt = con.createStatement();
        

        DatabaseMetaData md = con.getMetaData();
        ResultSet rs = md.getTables(null, null, "COLLECTION", null);
        if (!rs.next()) {

            stmt.executeUpdate(createCombinedTable);
//            stmt.executeUpdate(createCollectionTable);
//            stmt.executeUpdate(createFileTable);
//            stmt.executeUpdate(createHeaderTable);
//            stmt.executeUpdate(createReadsTable);

            System.out.println("Database created");
        } else {
            System.out.println("Tables already exist");
        }
        
        con.close(); 
        
    }
    
    public void deleteFullFolder() throws SQLException{
        String framework = "embedded";
        String protocol = "jdbc:derby:";
        Connection con = null;

        Boolean recordAdded = false;
        con = DriverManager.getConnection("jdbc:derby:NGSDB;create=true");

        //STEP 4: Execute a query
        System.out.println("Deleting database...");
        Statement stmt = con.createStatement();

        String sql = "DROP DATABASE NGSDB";
        stmt.executeUpdate(sql);
        System.out.println("Database deleted successfully...");

        con.close(); 

        
//        //Declareer string pathname voor het verwijderen van een folder dat de files bevat.
//        String pathname = System.getProperty("user.dir")+"\\"+dbName;
//        System.out.println(pathname);
//        //Concerteer de gegeven pathname string naar abstracte pathname door het creeëren van nieuwe File instance.
//        File folder = new File(pathname);
//       
//        try {
//            if(folder.exists()){
//                if(folder.isDirectory()){
//                    FileUtils.deleteDirectory(folder);
//                    for (File file : folder.listFiles()) {
//                        FileDeleteStrategy.FORCE.delete(file);
//                    }   
////                    File[]list = folder.listFiles();
////                    for (File file : list){
////                        file.delete();
////                    }
////                    folder.delete();
//                    System.out.println("Folder "+folder.getName()+" was deleted from project directory");
//                }
//            } else {
//                System.out.println("Folder "+folder.getName()+" does not exist in project directory");
//            }
//        } catch (Exception e){
//            e.printStackTrace();
//        }
    }
}