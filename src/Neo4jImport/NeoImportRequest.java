/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Neo4jImport;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 *
 * @author emil3
 */
public class NeoImportRequest { 
    private final String nodetype;
    private final LinkedHashMap<String, ArrayList<String>> data;
    
    public NeoImportRequest(String nodetype, LinkedHashMap<String, ArrayList<String>> data){
        this.nodetype = nodetype;
        this.data = data;
    }

    /**
     * @return the nodetype
     */
    public String getNodetype() {
        return nodetype;
    }

    /**
     * @return the data
     */
    public LinkedHashMap<String, ArrayList<String>> getData() {
        return data;
    }
}
