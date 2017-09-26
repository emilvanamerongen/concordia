/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Annotator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author emil3
 */
public class ReadRequest {
    private Boolean done = false;
    private Boolean active = false;
    private Long query;
    private HashMap<String, LinkedHashSet<String>> result = new HashMap<>();
    private Integer requestthread;
    private String sourceheader = "";
    private String searcheddb = "";
    
    public ReadRequest(Long position, String sourceheader, Integer requestthread, String searcheddb){
        this.query = position;
        this.requestthread = requestthread;
        this.sourceheader = sourceheader;
        this.searcheddb = searcheddb;
    }

    /**
     * @return the done
     */
    public Boolean getDone() {
        return done;
    }

    /**
     * @param done the done to set
     */
    public void setDone(Boolean done) {
        this.done = done;
    }

    /**
     * @return the active
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * @return the query
     */
    public Long getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(Long query) {
        this.query = query;
    }

    /**
     * @return the result
     */


    /**
     * @return the requestthread
     */
    public Integer getRequestthread() {
        return requestthread;
    }

    /**
     * @param requestthread the requestthread to set
     */
    public void setRequestthread(Integer requestthread) {
        this.requestthread = requestthread;
    }

    /**
     * @return the sourceheader
     */
    public String getSourceheader() {
        return sourceheader;
    }

    /**
     * @param sourceheader the sourceheader to set
     */
    public void setSourceheader(String sourceheader) {
        this.sourceheader = sourceheader;
    }

    /**
     * @return the searcheddb
     */
    public String getSearcheddb() {
        return searcheddb;
    }

    /**
     * @param searcheddb the searcheddb to set
     */
    public void setSearcheddb(String searcheddb) {
        this.searcheddb = searcheddb;
    }

    /**
     * @return the result
     */
    public HashMap<String, LinkedHashSet<String>> getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(HashMap<String, LinkedHashSet<String>> result) {
        this.result = result;
    }


    
}
