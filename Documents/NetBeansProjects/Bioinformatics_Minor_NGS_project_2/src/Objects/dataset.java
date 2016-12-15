/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Objects;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author emil
 */
public class dataset {
    private int id;
    private String title;
    private int rawsequences;
    private int blastedsequences;
    private List<Object> annotations = new ArrayList<Object>();

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the rawsequences
     */
    public int getRawsequences() {
        return rawsequences;
    }

    /**
     * @param rawsequences the rawsequences to set
     */
    public void setRawsequences(int rawsequences) {
        this.rawsequences = rawsequences;
    }

    /**
     * @return the blastedsequences
     */
    public int getBlastedsequences() {
        return blastedsequences;
    }

    /**
     * @param blastedsequences the blastedsequences to set
     */
    public void setBlastedsequences(int blastedsequences) {
        this.blastedsequences = blastedsequences;
    }

    /**
     * @return the annotations
     */
    public List<Object> getAnnotations() {
        return annotations;
    }

    /**
     * @param annotations the annotations to set
     */
    public void setAnnotations(List<Object> annotations) {
        this.annotations = annotations;
    }
}
