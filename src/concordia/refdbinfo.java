/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import javafx.scene.control.CheckBox;

/**
 *
 * @author emil3
 */
public class refdbinfo {
    private CheckBox select = new CheckBox();
    private String type;
    private String name;
    private String dateadded;
    private String sizeondisk;
    private String locationondisk;
    private String remoteURL;
    
    public refdbinfo(String type, String name, String dateadded, String sizeondisk, String locationondisk, String remoteURL){
        this.type = type;
        this.name = name;
        this.dateadded = dateadded;
        this.sizeondisk = sizeondisk;
        this.locationondisk = locationondisk;
        this.remoteURL = remoteURL;    
    }

    
}
