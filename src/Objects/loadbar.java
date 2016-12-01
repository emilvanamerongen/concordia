/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Objects;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 *
 * @author emil
 */
public class loadbar {
    
    // Define a variable to store the property
    private DoubleProperty done = new SimpleDoubleProperty();
 
    // Define a getter for the property's value
    public final double getdone(){return done.get();}
 
    // Define a setter for the property's value
    public final void setdone(double value){done.set(value);}
 
     // Define a getter for the property itself
    public DoubleProperty doneproperty() {return done;}
    
    
}
