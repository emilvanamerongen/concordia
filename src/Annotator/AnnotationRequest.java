/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Annotator;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author emil3
 */
public class AnnotationRequest {
    private Boolean done = false;
    private Boolean active = false;
    private String query = "";
    private String result = "";
    ReentrantLock lock = new ReentrantLock();
    
    public AnnotationRequest(String query){
        this.query = query;
    }
    /**
     * @return the done
     */
    public synchronized Boolean getDone() {
        return done;
    }

    /**
     * @param done the done to set
     */
    public synchronized void setDone(Boolean done) {
        this.done = done;
    }

    /**
     * @return the query
     */
    public synchronized String getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public synchronized void setQuery(String query) {
        this.query = query;
    }

    /**
     * @return the result
     */
    public synchronized String getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public synchronized void setResult(String result) {
        this.result = result;
    }

    /**
     * @return the active
     */
    public synchronized Boolean getActive() {
        return active;
    }
    
    public synchronized Boolean claim() {
        lock.lock();
        Boolean claimed = false;
        try {
        if (!active){
            active = true;
            claimed = true;
        } 
        } finally {
            lock.unlock();
            
        }
        return claimed;
        
    }
    
    /**
     * @param active the active to set
     */
    public synchronized void setActive(Boolean active) {
        this.active = active;
    }
    
    
    
}
