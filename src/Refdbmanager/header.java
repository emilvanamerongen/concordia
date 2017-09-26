/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Refdbmanager;

import FileManager.Filemanager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author emil3
 */
public class header{
    private String sourcedb;
    private String headerstring;
    private LinkedHashSet<String> examples = new LinkedHashSet<>();
    private Boolean enabled;
    private Integer colorindex = 0;
    private Boolean indexable = true;
    private Boolean indexed = false;
    private Integer tabindex = -1;
    private HashSet<String> parameters = new HashSet<>();
    private String value = "";
    
    Filemanager filemanager = new Filemanager();
    /**
     *
     * @param headerstring
     * @param examples
     * @param enabled
     * @param linkcolor
     */
    public header(String headerstring, LinkedHashSet<String> examples, Boolean enabled, String linkcolor, Integer indexable, Integer tabindex, HashSet<String> parameters){
        try {
            this.colorindex = Integer.parseInt(linkcolor);
        } catch (Exception ex){}
                
        this.headerstring = headerstring;    
        Integer index = 0;
        for (String example : examples){
            index++;
            this.examples.add(example);
            if (index == 20){
                break;
            }
        }
        this.enabled = enabled;
        if (indexable.equals(0)){
            this.indexable = false;
        }
        this.tabindex = tabindex;
        this.parameters = parameters;
        checkifindexed();
    }
    
    public header(String headerstring, Boolean enabled){
        this.headerstring = headerstring;
        this.enabled = enabled;
    }

    public header(String headerstring, Integer indexable) {
        this.headerstring = headerstring;
        if (indexable.equals(1)){
        this.indexable = true;
        } else {
            this.indexable = false;
        }
    }

    
    public Integer cyclecolor(){       
        if (colorindex == 7){
            colorindex = 0;
        } else {
            colorindex++; 
        }
        return colorindex;
    }
    
    public void checkifindexed(){
        File indexfile = new File(filemanager.getIndexdirectory()+File.separator+getSourcedb()+"."+getHeaderstring()+".ser");
        setIndexed(indexfile.exists());

    }
    /**
     * @return the headerstring
     */
    public String getHeaderstring() {
        return headerstring;
    }

    /**
     * @param headerstring the headerstring to set
     */
    public void setHeaderstring(String headerstring) {
        this.headerstring = headerstring;
    }

    /**
     * @return the examples
     */
    public LinkedHashSet<String> getExamples() {
        return examples;
    }

    /**
     * @param examples the examples to set
     */
    public void setExamples(LinkedHashSet<String> examples) {
        this.examples = examples;
    }

    /**
     * @return the enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public void toggleEnabled(){
        this.enabled = !this.enabled;
    }

    /**
     * @return the sourcedb
     */
    public String getSourcedb() {
        return sourcedb;
    }

    /**
     * @param sourcedb the sourcedb to set
     */
    public void setSourcedb(String sourcedb) {
        this.sourcedb = sourcedb;
    }

    /**
     * @return the linkcolor
     */

    /**
     * @return the colorindex
     */
    public Integer getColorindex() {
        return colorindex;
    }

    /**
     * @param colorindex the colorindex to set
     */
    public void setColorindex(Integer colorindex) {
        this.colorindex = colorindex;
    }
    
        public static Comparator<header> headerComparator = new Comparator<header>() {

	    public int compare(header header1, header header2) {

	      String headername1 = header1.getHeaderstring().toUpperCase();
	      String headername2 = header2.getHeaderstring().toUpperCase();

	      //ascending order
	      return headername1.compareTo(headername2);

	      //descending order
	      //return fruitName2.compareTo(fruitName1);
	    }

	};

    /**
     * @return the indexable
     */
    public Boolean getIndexable() {
        return indexable;
    }

    /**
     * @param indexable the indexable to set
     */
    public void setIndexable(Boolean indexable) {
        this.indexable = indexable;
    }

    /**
     * @return the indexed
     */
    public Boolean getIndexed() {
        return indexed;
    }

    /**
     * @param indexed the indexed to set
     */
    public void setIndexed(Boolean indexed) {
        this.indexed = indexed;
    }

    /**
     * @return the tabindex
     */
    public Integer getTabindex() {
        return tabindex;
    }

    /**
     * @param tabindex the tabindex to set
     */
    public void setTabindex(Integer tabindex) {
        this.tabindex = tabindex;
    }

    /**
     * @return the parameters
     */
    public HashSet<String> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(HashSet<String> parameters) {
        this.parameters = parameters;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }


}
