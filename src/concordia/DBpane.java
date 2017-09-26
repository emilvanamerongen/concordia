/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import Refdbmanager.refdb;
import UniprotModule.Uniprotmainthread;
import java.util.concurrent.locks.ReentrantLock;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 *
 * @author emil3
 */
public class DBpane extends StackPane{
    private refdb database = new refdb();
    private ProgressBar loadingbar = new ProgressBar();
    public CheckBox checkbox = new CheckBox(); 
    private Integer progress;
    private ReentrantLock lock = new ReentrantLock();
    
    Timeline timeline = new Timeline(Timeline.INDEFINITE, new KeyFrame(Duration.millis(1000), ae -> updateloop()));
    
    public DBpane(refdb database){
        this.maxWidth(1000.0);
        this.maxHeight(300.0);
        loadingbar.setMaxSize(1000, 300);
        loadingbar.setPrefHeight(200);
        loadingbar.setProgress(0);

        this.database = database;
        checkbox.setText(database.getDbname());
        if (database.getType().equals("template (tab)")){
           checkbox.setDisable(true);
           checkbox.setSelected(true);
        }
        this.getChildren().addAll(loadingbar,checkbox);
        
    }
    
    private void updateloop(){
        loadingbar.setProgress(progress);
    }
    
    public void startloop(){
        timeline.play();
    }
    
    public void stoploop(){
        timeline.stop();
    }

    /**
     * @return the database
     */
    public refdb getDatabase() {
                lock.lock();
        try {} finally {
        lock.unlock();  
    }
        return database;
    }

    /**
     * @param database the database to set
     */
    public void setDatabase(refdb database) {
        this.database = database;
    }

    /**
     * @return the progress
     */
    public Integer getProgress() {
        lock.lock();
        try {} finally {
        lock.unlock();  
    }
        return progress;
    }

    /**
     * @param progress the progress to set
     */
    public void setProgress(Integer progress) {
        lock.lock();
        try {
            this.progress = progress;
        } finally {
        lock.unlock();  
    }
        
    }
}
