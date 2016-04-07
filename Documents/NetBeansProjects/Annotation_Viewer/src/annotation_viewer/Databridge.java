package annotation_viewer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Alex
 */
public class Databridge {
    
    //// Fields
    
    private ChooseDatabase database;
    private String startString = "";
    private String stopString = "";
    
    //// Constructor
    
    public Databridge(){
        database = new ChooseDatabase();
    }
    
    
    //// Methods
    
    /**
     * De uitleg!
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws SQLException 
     */
    public String databaseSequence() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        
        Connection con = DriverManager.getConnection(database.getDatabase_url(), database.getUsername(), database.getPassword());
        //System.out.println("db is connected!");

        StringBuffer sequence = new StringBuffer();
        Statement st = con.createStatement();
        String sql = ("SELECT * FROM Gen;");
        ResultSet rs = st.executeQuery(sql);
        
        if (rs.next()) {
            
            sequence.append(rs.getString("Sequentie"));
        }
        
        con.close();
        
        return sequence.toString();
    }
    
    /**
     * 
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws SQLException 
     */
    public void databaseAnnotation() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        
        Connection con = DriverManager.getConnection(database.getDatabase_url(), database.getUsername(), database.getPassword());

        StringBuffer posStart = new StringBuffer();
        StringBuffer posStop = new StringBuffer();
        
        Statement st = con.createStatement();
        String sql = ("SELECT * FROM Feature;");
        ResultSet rs = st.executeQuery(sql);
        
        if (rs.next()) {
            
            posStart.append(rs.getString("posStart"));
            posStop.append(rs.getString("posStop"));
            
        }
        
        con.close();
        
        String posStartString = posStart.toString();
        startString = posStartString;
        
        String posStopString = posStop.toString();
        stopString = posStopString;
    }

    /**
     * 
     * @return 
     */
    public String getStartString() {
        return startString;
    }
    
    /**
     * 
     * @return 
     */
    public String getStopString() {
        return stopString;
    }
    

}
