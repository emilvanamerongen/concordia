/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import Refdbmanager.refdb;
import TabDelimitedModule.TabDelimitedIndexer;
import UniprotModule.IDMappingIndexer;
import UniprotModule.uniprotindexer;

import static javafx.animation.Animation.INDEFINITE;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 *
 * @author emil3
 */
public class ActiveThreadElement extends HBox{
    private Label indexernamelabel = new Label();
    private Label headersindexing = new Label();
    private ProgressBar loadingbar = new ProgressBar();
    private Button killbutton = new Button();
    private StackPane progressbarpane = new StackPane();
    private Label progressbarlabel= new Label();
    private Double progress = 0.0;
    private refdb db; 
    private uniprotindexer uniprotindexerthread = new uniprotindexer();
    private TabDelimitedIndexer tabindexerthread= new TabDelimitedIndexer();
    private IDMappingIndexer idmappingindexerthread = new IDMappingIndexer();
    private String type;
    private long startTime = System.nanoTime();
    private Long previousposition = 0L;
    private Long position = 0L;
    private Long filesize = 0L;
    
    String minutestring = "";
    Timeline indexertimeline = new Timeline(Timeline.INDEFINITE, new KeyFrame(Duration.millis(10000), ae -> indexerupdateloop()));

    ActiveThreadElement(uniprotindexer indexerthread, String type) {
        this.setAlignment(Pos.CENTER);
        this.setPrefHeight(60);
        this.type = type;
        
        this.uniprotindexerthread = indexerthread;
        setup();
    }
    
    ActiveThreadElement(TabDelimitedIndexer indexerthread, String type) {
        this.setAlignment(Pos.CENTER);
        this.setPrefHeight(60);
        this.type = type;
        
        this.tabindexerthread = indexerthread;
        setup();
    }

    ActiveThreadElement(IDMappingIndexer thread, String type) {
        this.setAlignment(Pos.CENTER);
        this.setPrefHeight(60);
        this.type = type;
        
        this.idmappingindexerthread = thread;
        setup();
    }


    
    private void setup(){
        //add children
        this.getChildren().add(indexernamelabel);
        this.getChildren().add(headersindexing);
        
       
        
        //element setup
        killbutton.setText("CANCEL");
        indexernamelabel.setMaxWidth(200);
        indexernamelabel.setMinWidth(200);
        indexernamelabel.setPadding(new Insets(0, 0, 0, 10));
        headersindexing.setMaxWidth(200);
        headersindexing.setMinWidth(200);
        headersindexing.setPadding(new Insets(0, 0, 0, 10));
        headersindexing.setFont(Font.font ("Verdana", 10));
        headersindexing.setTextFill(Color.GREY);
        
        progressbarpane.setPrefSize(200, 25);
        progressbarpane.getChildren().add(loadingbar);
        progressbarpane.getChildren().add(progressbarlabel);
        loadingbar.setPrefSize(200, 25);
        loadingbar.setMinSize(200, 25);
        loadingbar.setPadding(new Insets(0, 0, 0, 10));
        
        progressbarlabel.setText("counting lines..");
        
        this.getChildren().add(progressbarpane);
        this.getChildren().add(killbutton);
        killbutton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                        if (type.equals("uniprot")){
                        if (uniprotindexerthread.isAlive()){
                            uniprotindexerthread.setKill(true);
                        }
                        }  else if (type.equals("tab")){
                            if (tabindexerthread.isAlive()){
                            tabindexerthread.setKill(true);
                        }
                        }
                          else if (type.equals("idmapping")){
                            if (idmappingindexerthread.isAlive()){
                            idmappingindexerthread.setKill(true);
                        }
                        }
            }
        });
        
        if (type.equals("uniprot")){
        if (uniprotindexerthread.isAlive()){
            indexernamelabel.setText(uniprotindexerthread.getIndexername());
            headersindexing.setText(uniprotindexerthread.getHeadersindexing());
        }}  else if (type.equals("tab")){
            indexernamelabel.setText(tabindexerthread.getIndexername());
            headersindexing.setText(tabindexerthread.getHeadersindexing());
            
        }  else if (type.equals("idmapping")){
            indexernamelabel.setText(idmappingindexerthread.getIndexername());
            headersindexing.setText(idmappingindexerthread.getHeadersindexing());
            
        }
        
        //start timeline
        indexertimeline.setCycleCount(INDEFINITE);
        indexertimeline.play();
        
    }

    private void indexerupdateloop() {
        if (type.equals("uniprot")){
            if (uniprotindexerthread.isAlive()){
                position = uniprotindexerthread.getLineposition();
                filesize = uniprotindexerthread.getFilesize();
                progress = position.doubleValue()/filesize.doubleValue();
                if (progress.equals(0.0)){
                    uniprotindexerthread.setComplete(true);
                }
        }}else if (type.equals("tab")){
            position = tabindexerthread.getLineposition();
            filesize = tabindexerthread.getFilesize();
            progress = position.doubleValue()/filesize.doubleValue();
            if (progress.equals(0.0)){
                    tabindexerthread.setComplete(true);
                }
        }else if (type.equals("idmapping")){ 
            position = idmappingindexerthread.getLineposition();
            filesize = idmappingindexerthread.getFilesize();
            progress = position.doubleValue()/filesize.doubleValue();
            if (progress.equals(0.0)){
                    idmappingindexerthread.setComplete(true);
                }
        }
        

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
