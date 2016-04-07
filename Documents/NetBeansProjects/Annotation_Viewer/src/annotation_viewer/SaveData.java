package annotation_viewer;


import static java.lang.Integer.parseInt;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class SaveData{
    
    private Databridge dataBridge;
    private TreeMap<Integer, Integer> annotationMap;
    private ArrayList<String> aminoList;
    private String sequence;
    private String proteinString; 
    private int keysSize;
    
    public SaveData(){
        dataBridge = new Databridge();
    }
    
    public String DatabridgeSequence(){        
        try {
            
            // the SD.fa sequence from the database is saved and put into one long String 
            sequence = dataBridge.DatabaseSequence();
            sequence = sequence.replaceAll("\n", "");
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            Logger.getLogger(SaveData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return sequence;
            
    }
    
    public String DatabridgeSequenceToProtein(){
        
        String[] sequenceArray = DatabridgeSequence().toUpperCase().split("(?<=\\G...)");
        
        aminoList = new ArrayList<String>();
        
        for(String s: sequenceArray){
            
            String aminoacid = (String) codonTable.get(s);
            
            if(aminoacid != null && !aminoacid.isEmpty()){
                
                aminoList.add(aminoacid);
            }
        }
        
        String proteinString = aminoList.toString();
        
        proteinString = proteinString.substring(1, proteinString.length()-1).replaceAll(",", " ");
        
        return proteinString;
    }
    
    public void DatabridgeAnnotation() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException{
        annotationMap = new TreeMap<Integer, Integer>();
        
        dataBridge.DatabaseAnnotation();
        
        String[] keys = dataBridge.getStartString().split(";");
        
        String[] values = dataBridge.getStopString().split(";");
        
        int keysSize = (keys != null) ? keys.length : 0;

        for (int i = 0; i < keysSize; i++) {
            
            if(parseInt(keys[i]) < parseInt(values[i])){
                
                annotationMap.put(Integer.parseInt(keys[i]), Integer.parseInt(values[i]));
                
            } 
            
            else if(parseInt(keys[i]) > parseInt(values[i])){
                //annotationMap.put(Integer.parseInt(values[i]), Integer.parseInt(keys[i]));
                // Maybe Still useful for reverse strand mapping
            }
            
        }  
    }

    public TreeMap<Integer, Integer> getAnnotationMap() {
        return annotationMap;
    }

    // get total number of annotated genes
    public int getKeysSize() {
        return keysSize;
    }
    
    //HashMap codonTable, used for the translation of the main sequence in "ATGC" to amino acids.
    public static final HashMap<String, String> codonTable = new HashMap<String, String>() {{
    put("TTT", "F"); put("TCT", "S"); put("TAT", "Y"); put("TGT", "C");
    put("TTC", "F"); put("TCC", "S"); put("TAC", "Y"); put("TGC", "C");
    put("TTA", "L"); put("TCA", "S"); put("TAA", "*"); put("TGA", "*");
    put("TTG", "L"); put("TCG", "S"); put("TAG", "*"); put("TGG", "W");
    put("CTT", "L"); put("CCT", "P"); put("CAT", "H"); put("CGT", "R");
    put("CTC", "L"); put("CCC", "P"); put("CAC", "H"); put("CGC", "R");
    put("CTA", "L"); put("CCA", "P"); put("CAA", "Q"); put("CGA", "R");
    put("CTG", "L"); put("CCG", "P"); put("CAG", "Q"); put("CGG", "R");
    put("ATT", "I"); put("ACT", "T"); put("AAT", "N"); put("AGT", "S");
    put("ATC", "I"); put("ACC", "T"); put("AAC", "N"); put("AGC", "S");
    put("ATA", "I"); put("ACA", "T"); put("AAA", "K"); put("AGA", "R");
    put("ATG", "M"); put("ACG", "T"); put("AAG", "K"); put("AGG", "R");
    put("GTT", "V"); put("GCT", "A"); put("GAT", "D"); put("GGT", "G");
    put("GTC", "V"); put("GCC", "A"); put("GAC", "D"); put("GGC", "G");
    put("GTA", "V"); put("GCA", "A"); put("GAA", "E"); put("GGA", "G");
    put("GTG", "V"); put("GCG", "A"); put("GAG", "E"); put("GGG", "G");
}};
    
}
