/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Objects;

/**
 *
 * @author emil
 */
public class NGSread {
    private String Header;
    private Boolean Readdirection;
    private String Sequence;
    private String Qualityvalues;      

    /**
     * @return the Header
     */
    public String getHeader() {
        return Header;
    }

    /**
     * @param Header the Header to set
     */
    public void setHeader(String Header) {
        this.Header = Header;
    }

    /**
     * @return the Readdirection
     */
    public Boolean getReaddirection() {
        return Readdirection;
    }

    /**
     * @param Readdirection the Readdirection to set
     */
    public void setReaddirection(Boolean Readdirection) {
        this.Readdirection = Readdirection;
    }

    /**
     * @return the Sequence
     */
    public String getSequence() {
        return Sequence;
    }

    /**
     * @param Sequence the Sequence to set
     */
    public void setSequence(String Sequence) {
        this.Sequence = Sequence;
    }

    /**
     * @return the Qualityvalues
     */
    public String getQualityvalues() {
        return Qualityvalues;
    }

    /**
     * @param Qualityvalues the Qualityvalues to set
     */
    public void setQualityvalues(String Qualityvalues) {
        this.Qualityvalues = Qualityvalues;
    }
    
    public void clear() {
        this.Qualityvalues = null;
        this.Header = null;
        this.Readdirection = null;
        this.Sequence = null;
        
    }
}
