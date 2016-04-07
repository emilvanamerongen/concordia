package annotation_viewer;

/**
 *
 * @author Alex
 */
public class ChooseDatabase {
    
    //// Fields
    
    private String database_url = "jdbc:mysql://213.93.9.4:3306/mydb";
    private String username = "blok7project";
    private String password = "ps6VVpHs7dYSVvKZ";
    
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
    
}
