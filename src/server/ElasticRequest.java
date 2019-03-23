/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.File;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 *
 * @author emil3
 */
class ElasticRequest {
    private LinkedHashMap<String,ArrayList<String>> query = new LinkedHashMap<>();
    private File outputfile = new File("");
    private PrintWriter out; 
    private String querystring = "";
    
    public ElasticRequest(String querystring, LinkedHashMap<String,ArrayList<String>> query, File outputfile, PrintWriter out){
        this.query = query;
        this.outputfile = outputfile;
        this.out = out;
        this.querystring = querystring;
    }



    /**
     * @return the outputfile
     */
    public File getOutputfile() {
        return outputfile;
    }

    /**
     * @param outputfile the outputfile to set
     */
    public void setOutputfile(File outputfile) {
        this.outputfile = outputfile;
    }

    /**
     * @return the out
     */
    public synchronized PrintWriter getOut() {
        return out;
    }

    /**
     * @param out the out to set
     */
    public synchronized void setOut(PrintWriter out) {
        this.out = out;
    }

    /**
     * @return the query
     */
    public synchronized LinkedHashMap<String,ArrayList<String>> getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public synchronized void setQuery(LinkedHashMap<String,ArrayList<String>> query) {
        this.query = query;
    }

    /**
     * @return the querystring
     */
    public synchronized String getQuerystring() {
        return querystring;
    }

    /**
     * @param querystring the querystring to set
     */
    public synchronized void setQuerystring(String querystring) {
        this.querystring = querystring;
    }
}
