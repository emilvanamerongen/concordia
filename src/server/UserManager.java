/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil3
 */
public class UserManager {
    private static HashMap<String,userdata> users = new HashMap<>(); 
    private static File storagefile = new File("userstorage");

    public void init(){
        if (storagefile.exists()){
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(storagefile);
                ObjectInput ois = new ObjectInputStream(fis);
                users = (HashMap) ois.readObject();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        } else {
            if (!users.containsKey("admin")){              
                users.put("admin", new userdata("concordia",true));         
            }
            save();
        }
        
    }
    
    public static void save(){
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        try {
            fos = new FileOutputStream(storagefile);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(users);
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fos.close();
                oos.close();
            } catch (IOException ex) {
                Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public synchronized Boolean verify(String user, String password){
        Boolean verified = false;
        if (users.containsKey(user)){
            userdata myuserdata = users.get(user);
            verified = myuserdata.checkpassword(password);
        }
        return verified;
    }
    
    
    
    public class userdata {
        private String password = "";
        private ArrayList<String> history = new ArrayList<>();
        private Boolean isadmin = false;
        private Boolean isprimeadmin = false; 
        
        public userdata(String password, Boolean isprimeadmin){
            this.password = password;
            this.isprimeadmin = isprimeadmin;
            if (isprimeadmin){
                isadmin = true;
            }
        }

        private Boolean checkpassword(String password) {
            return (password == null ? this.password == null : password.equals(this.password));
        }
        
        
    }
}


