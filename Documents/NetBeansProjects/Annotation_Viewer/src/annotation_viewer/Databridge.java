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
    private ChooseDatabase database;
    private String startString = "";
    private String stopString = "";
    
    public Databridge(){
        database = new ChooseDatabase();
    }
    
    /**
     * De uitleg!
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws SQLException 
     */
    public String DatabaseSequence() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        
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
    public void DatabaseAnnotation() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        
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

    public String getStartString() {
        return startString;
    }

    public String getStopString() {
        return stopString;
    }
    

}
