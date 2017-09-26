/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import Refdbmanager.header;
import static javafx.animation.Animation.INDEFINITE;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 *
 * @author emil3
 */
public class IndexSourceElement extends HBox{
    private header myheader;
    private Label namelabel = new Label();
    Timeline timeline = new Timeline(Timeline.INDEFINITE, new KeyFrame(Duration.millis(10000), ae -> updateloop()));
    Boolean active = false;
    
    public IndexSourceElement(header myheader){
        this.setAlignment(Pos.CENTER);
        this.myheader = myheader;
        this.getChildren().add(namelabel);
        namelabel.setText(myheader.getSourcedb()+"\t"+myheader.getHeaderstring());
        timeline.setCycleCount(INDEFINITE);
        timeline.play();
    }
    
    
    public void updateloop(){
        active = false;
        try {
            if (GUIController.indexstorage.containsKey(myheader.getSourcedb()) && GUIController.indexstorage.get(myheader.getSourcedb()).getData().containsKey(myheader.getHeaderstring())){
                active = true;
            }else {
                active = false;
            }
        
        } catch (Exception ex){}
        
        if (active){
            namelabel.setTextFill(Color.GREEN);      
        } else {
            namelabel.setTextFill(Color.BLACK);      
        }
    }
    
}
