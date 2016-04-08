package annotation_viewer;

/**
 *
 * @author Alex
 * @version 2.0
 * @since 8-4-2016
 * 
 */
public class ChooseDatabaseDetails {
    
    //// Fields
    
    /**
     * All of the database credentials used for the connection to the database and retrieving data.
     */
    private String database_url = "jdbc:mysql://213.93.9.4:3306/mydb";
    private String username = "blok7project";
    private String password = "ps6VVpHs7dYSVvKZ";
    private String sequenceQuery = "SELECT * FROM Gen;";
    private String annotationQuery = "SELECT * FROM Feature;";
    
    //// Methods

    /**
     * 
     * @return 
     */
    public String getDatabase_url() {
        return database_url;
    }
    
    /**
     * 
     * @return 
     */
    public String getUsername() {
        return username;
    }

    /**
     * 
     * @return 
     */
    public String getPassword() {
        return password;
    }

    /**
     * 
     * @param database_url 
     */
    public void setDatabase_url(String database_url) {
        this.database_url = database_url;
    }

    /**
     * 
     * @param username 
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 
     * @param password 
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 
     * @return 
     */
    public String getSequenceQuery() {
        return sequenceQuery;
    }

    /**
     * 
     * @return 
     */
    public String getAnnotationQuery() {
        return annotationQuery;
    }

    /**
     * 
     * @param sequenceQuery 
     */
    public void setSequenceQuery(String sequenceQuery) {
        this.sequenceQuery = sequenceQuery;
    }

    /**
     * 
     * @param annotationQuery 
     */
    public void setAnnotationQuery(String annotationQuery) {
        this.annotationQuery = annotationQuery;
    }
    
}
