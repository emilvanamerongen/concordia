/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package annotation_viewer;

/**
 *
 * @author Alex
 */
public class ChooseDatabase {
    private String database_url = "jdbc:mysql://213.93.9.4:3306/mydb";
    private String username = "blok7project";
    private String password = "ps6VVpHs7dYSVvKZ";

    public String getDatabase_url() {
        return database_url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setDatabase_url(String database_url) {
        this.database_url = database_url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    
}
