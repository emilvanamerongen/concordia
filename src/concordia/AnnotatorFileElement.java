/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import Annotator.AnnotationFileInfo;
import static javafx.animation.Animation.INDEFINITE;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 *
 * @author emil3
 */
public class AnnotatorFileElement extends VBox{
    private AnnotationFileInfo data;
    private Label titlelabel = new Label();
    private ProgressBar loadingbar = new ProgressBar();
    private StackPane progressbarpane = new StackPane();
    private Label progressbarlabel= new Label();
    private Double progress = 0.0;
    private long startTime = System.nanoTime();
    private Long previousposition = 0L;
    private Long position = 0L;
    private Long filesize = 0L;
    private String minutestring;
    
    Timeline timeline = new Timeline(Timeline.INDEFINITE, new KeyFrame(Duration.millis(3000), ae -> updateloop()));
    
    public AnnotatorFileElement(AnnotationFileInfo data){
        this.data = data;
        //add children
        this.getChildren().add(titlelabel);   
        titlelabel.setText(data.getFile().getName());
        //element setup
        
        progressbarpane.setPrefSize(200, 25);
        progressbarpane.getChildren().add(loadingbar);
        progressbarpane.getChildren().add(progressbarlabel);
        loadingbar.setPrefSize(200, 25);
        loadingbar.setMinSize(200, 25);
        loadingbar.setPadding(new Insets(0, 0, 0, 10));
        
        progressbarlabel.setText("counting lines..");
        
        this.getChildren().add(progressbarpane);
        timeline.setCycleCount(INDEFINITE);
        timeline.play();
    }
    
    public void updateloop(){
        position = data.getProgress();
        filesize = data.getTotalsize();
        progress = data.getProgress().doubleValue()/data.getTotalsize().doubleValue();
        Long estimatedTime = System.nanoTime() - startTime;
        double estimatedTimedouble = estimatedTime;
        Long bytesincycle = position-previousposition; 
        Long bytestodo = filesize-position;
        double timeremaining = (estimatedTimedouble/bytesincycle.doubleValue())*bytestodo.doubleValue();
        Double minutes = timeremaining / 60000000000.0;
        minutestring = minutes.toString();
            try{
        minutestring = minutestring.substring(0,minutestring.indexOf("."));
        } catch (Exception ex){}
        minutestring = minutestring+" minutes remaining..";

        startTime = System.nanoTime();
        previousposition = position;

        progressbarlabel.setText(minutestring);
        loadingbar.setProgress(progress);
    }
    
    
    
}
