/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UniprotModule;

import Refdbmanager.header;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 *
 * @author emil3
 */
public class UniprotEntry {
    private HashMap<String,LinkedHashSet<String>> data = new HashMap<>();
    private String accessionnumber;
    private HashSet<header> headerconfig = new HashSet<>();
    private Boolean useconfig = false;
    private HashSet<String> indexable = new HashSet<>();
    private String activecomentblock;
    
    public UniprotEntry(){
        
    }
    
    public UniprotEntry(HashSet<header> headerconfig, Boolean useconfig){
        this.headerconfig = headerconfig;
        this.useconfig = useconfig;
    }
    

    
    
    public void addline(String line){
        try {
            String type = line.substring(0, 2);
            String rest = line.substring(5).replace("\t", "");
            String lastentrytype = "emptyentrytype";
            if (type.equals("DR")){
                ArrayList<String> drsplit = new ArrayList<String>();
                for (String item : rest.split("; ")){
                    drsplit.add(item);
                }
                
                String RESOURCE_ABBREVIATION = drsplit.get(0).trim();
                RESOURCE_ABBREVIATION = "DR "+RESOURCE_ABBREVIATION;
                Integer index = 0;
                for (String RESOURCE_IDENTIFIER : drsplit.subList(1, drsplit.size())){
                    RESOURCE_IDENTIFIER = RESOURCE_IDENTIFIER.trim();
                    if (RESOURCE_IDENTIFIER.endsWith(".")){
                        RESOURCE_IDENTIFIER = RESOURCE_IDENTIFIER.substring(0, RESOURCE_IDENTIFIER.length()-1);
                    }
                    Boolean infoitem = false;
                    Boolean skip = false;
                    Boolean versionsplit = false;
                    
                    if (RESOURCE_IDENTIFIER.length() <= 2){
                        skip = true;
                    } 
                    else if (RESOURCE_ABBREVIATION.equals("DR GO") && !RESOURCE_IDENTIFIER.startsWith("GO:")){
                        infoitem = true;
                    } else if (RESOURCE_ABBREVIATION.equals("DR InterPro") && index > 0){
                        infoitem = true;
                    } else if (RESOURCE_ABBREVIATION.equals("DR PROSITE") && index > 0){
                        infoitem = true;
                    } else if (RESOURCE_ABBREVIATION.equals("DR Pfam") && index > 0){
                        infoitem = true;
                    }else if (RESOURCE_ABBREVIATION.equals("DR EMBL") && isAlpha(RESOURCE_IDENTIFIER)){
                        infoitem = true;
                    }else if (RESOURCE_ABBREVIATION.equals("DR TIGRFAMs") && isAlpha(RESOURCE_IDENTIFIER)){
                        infoitem = true;
                    }else if (RESOURCE_ABBREVIATION.equals("DR PIRSF") && isAlpha(RESOURCE_IDENTIFIER)){
                        infoitem = true;
                    }else if (RESOURCE_ABBREVIATION.equals("DR CDD") && !RESOURCE_IDENTIFIER.startsWith("cd")){
                        infoitem = true;
                    }else if (RESOURCE_ABBREVIATION.equals("DR Proteomes") && isAlpha(RESOURCE_IDENTIFIER)){
                        infoitem = true;
                   }else if (RESOURCE_ABBREVIATION.equals("DR PRINTS") && isAlpha(RESOURCE_IDENTIFIER)){
                        infoitem = true;
                    }else if (RESOURCE_ABBREVIATION.equals("DR PDB") && RESOURCE_IDENTIFIER.length()!=4){
                        infoitem = true;
                    }
                    
                    
                    if (!skip){
                    if (infoitem){
                            getData().putIfAbsent(RESOURCE_ABBREVIATION+" info", new LinkedHashSet<String>());
                            getData().get(RESOURCE_ABBREVIATION+" info").add(RESOURCE_IDENTIFIER);
                            
                    } else {
                        try {
                        getData().putIfAbsent(RESOURCE_ABBREVIATION, new LinkedHashSet<String>());
                        getData().get(RESOURCE_ABBREVIATION).add(RESOURCE_IDENTIFIER);
                        indexable.add(RESOURCE_ABBREVIATION);


                        } catch (Exception ex){
                            getData().putIfAbsent(RESOURCE_ABBREVIATION, new LinkedHashSet<String>());
                            getData().get(RESOURCE_ABBREVIATION).add(RESOURCE_IDENTIFIER);
                            indexable.add(RESOURCE_ABBREVIATION);

                        }
                    }}
                    index++;
                }

            } else if (type.equals("FT")){
                getData().putIfAbsent("FT", new LinkedHashSet<String>());
                getData().get("FT").add(rest.replace("\t", ""));
            } else if (type.equals("RA")){
                getData().putIfAbsent("Reference Author", new LinkedHashSet<String>());
                getData().get("Reference Author").add(rest.replace("\t", ""));
            } else if (type.equals("  ")){
                getData().putIfAbsent("SEQUENCE", new LinkedHashSet<String>());
                getData().get("SEQUENCE").add(rest.replace("\t", ""));
            } else if (type.equals("RT")){
                getData().putIfAbsent("Reference Title", new LinkedHashSet<String>());
                getData().get("Reference Title").add(rest.replace("\t", ""));
            } else if (type.equals("DT")){
                getData().putIfAbsent("DATE", new LinkedHashSet<String>());
                getData().get("DATE").add(rest.replace("\t", ""));
            } else if (type.equals("DE")){
                getData().putIfAbsent("Description", new LinkedHashSet<String>());
                getData().get("Description").add(rest.replace("\t", ""));
            } else if (type.equals("OC")){
                getData().putIfAbsent("Organism Classification", new LinkedHashSet<String>());
                getData().get("Organism Classification").add(rest.replace("\t", ""));
            } else if (type.equals("KW")){
                getData().putIfAbsent("Keyword", new LinkedHashSet<String>());
                getData().get("Keyword").add(rest.replace("\t", ""));
            } else if (type.equals("RP")){
                getData().putIfAbsent("Reference Position", new LinkedHashSet<String>());
                getData().get("Reference Position").add(rest.replace("\t", ""));
            } else if (type.equals("RL")){
                getData().putIfAbsent("Reference Location", new LinkedHashSet<String>());
                getData().get("Reference Location").add(rest.replace("\t", ""));
            } else if (type.equals("RN")){
                getData().putIfAbsent("Reference Number", new LinkedHashSet<String>());
                getData().get("Reference Number").add(rest.replace("\t", ""));
            } else if (type.equals("RC")){
                getData().putIfAbsent("Reference Comment", new LinkedHashSet<String>());
                getData().get("Reference Comment").add(rest.replace("\t", ""));
            } else if (type.equals("RX")){
               for (String item : rest.split("; ")){
                   if (item.contains("=")){
                   String database = "RX "+item.split("=")[0].trim();
                   String identifier = item.split("=")[1].trim();
                        getData().putIfAbsent(database, new LinkedHashSet<String>());
                        getData().get(database).add(identifier);
               }
               }
                
            } else if (type.equals("OS")){
                String organismline = rest;
                if (organismline.contains("(")){
                        getData().putIfAbsent("Organism", new LinkedHashSet<String>());
                        getData().putIfAbsent("Organism extra", new LinkedHashSet<String>());
                        getData().get("Organism").add(organismline.substring(0, organismline.indexOf("(",0)));
                        getData().get("Organism extra").add(organismline.substring(organismline.indexOf("(",0)));
                        } else {
                            getData().putIfAbsent("Organism", new LinkedHashSet<String>());
                            getData().get("Organism").add(organismline);
                        }
            } else if (type.equals("AC")){
                setAccessionnumber(rest.split("; ")[0].trim());
                for (String item : rest.split("; ")){
                    getData().putIfAbsent("Accession number", new LinkedHashSet<String>());
                    getData().get("Accession number").add(item.trim());
                    indexable.add("Accession number");
                }
            } else if (type.equals("ID")){
                String entry = rest.split("; ")[0];
                String EntryName = entry.split(" ")[0];
                String Reviewed = entry.split(" ")[1];
                String SequenceLength = rest.split("; ")[1];
                
                getData().putIfAbsent("EntryName", new LinkedHashSet<String>());
                getData().get("EntryName").add(EntryName.trim());
                indexable.add("EntryName");
                getData().putIfAbsent("Reviewed", new LinkedHashSet<String>());
                getData().get("Reviewed").add(Reviewed.trim());
                getData().putIfAbsent("SequenceLength", new LinkedHashSet<String>());
                getData().get("SequenceLength").add(SequenceLength.trim());
            } else if (type.equals("OX")){
                for (String entry : rest.split("; ")){
                    getData().putIfAbsent("NCBI_TaxID", new LinkedHashSet<String>());
                    getData().get("NCBI_TaxID").add(rest.split("=")[1].trim());
                } 
            } else if (type.equals("PE")){
                for (String entry : rest.split("; ")){
                    getData().putIfAbsent("Protein existence", new LinkedHashSet<String>());
                    getData().get("Protein existence").add(rest.split(":")[0].trim());
                } 
            } else if (type.equals("SQ")){
                getData().putIfAbsent("SEQUENCE info", new LinkedHashSet<String>());
                getData().get("SEQUENCE info").add(rest.replace("\t", ""));
            } else if (type.equals("GN")){
                for (String entry : rest.split("; ")){
                    if (entry.contains("=")){      
                    String entrytype = entry.split("=")[0];
                    lastentrytype = entrytype;
                    String entryrest = entry.split("=")[1];
                    if (entryrest.contains(",")){
                    for (String name : entryrest.split(",")){
                                getData().putIfAbsent(entrytype, new LinkedHashSet<String>());
                                getData().get(entrytype).add(name.trim());  
                    }}else {
                            getData().putIfAbsent(entrytype, new LinkedHashSet<String>());
                            getData().get(entrytype).add(entryrest.trim());  
                    }
                }
                } 
            } else if (type.equals("RG")){
                getData().putIfAbsent("Reference Group", new LinkedHashSet<String>());
                getData().get("Reference Group").add(rest.replace("\t", ""));
            } else if (type.equals("OG")){
                getData().putIfAbsent("Organelle", new LinkedHashSet<String>());
                getData().get("Organelle").add(rest.replace("\t", ""));
            } else if (type.equals("OH")){
                getData().putIfAbsent("Organism Host", new LinkedHashSet<String>());
                getData().get("Organism Host").add(rest.replace("\t", ""));
            }  else if (type.equals("CC")){
                if (rest.startsWith("-!-")){
                    activecomentblock = "CC "+(rest.substring(3,rest.indexOf(":")).trim());
                    String information = rest.substring(rest.indexOf(":")+1).trim();
                    getData().putIfAbsent(activecomentblock, new LinkedHashSet<String>());
                    getData().get(activecomentblock).add(information);
                } else{
                    getData().putIfAbsent(activecomentblock, new LinkedHashSet<String>());
                    getData().get(activecomentblock).add(rest.trim());
                }

            }
            } catch (Exception ex){ //System.out.println("ERROR on line: "+line);
            }
                
    }
    
    
    public void printdata(){
        for (String item : getData().keySet()){
            System.out.println(item);
            for (String line : getData().get(item)){
                System.out.println("\t"+line);
                }
            }
        }
    
    public boolean isAlpha(String name) {
        char[] chars = name.toCharArray();

        for (char c : chars) {
            if(Character.isDigit(c) || Character.isWhitespace(c)) {
                return false;
            }
        }

        return true;
    }

    /**
     * @return the data
     */
    public HashMap<String,LinkedHashSet<String>> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(HashMap<String,LinkedHashSet<String>> data) {
        this.data = data;
    }

    /**
     * @return the accessionnumber
     */
    public String getAccessionnumber() {
        return accessionnumber;
    }

    /**
     * @param accessionnumber the accessionnumber to set
     */
    public void setAccessionnumber(String accessionnumber) {
        this.accessionnumber = accessionnumber;
    }

    /**
     * @return the indexable
     */
    public HashSet<String> getIndexable() {
        return indexable;
    }

    /**
     * @param indexable the indexable to set
     */
    public void setIndexable(HashSet<String> indexable) {
        this.indexable = indexable;
    }
}
    
//uniprot type occurrences test:
//
//DR	6131
//CC	2150
//FT	2037
//RA	1603
//  	954
//RT	895
//DT	834
//DE	773
//OC	676
//KW	610
//RP	523
//RL	454
//RN	454
//RC	373
//RX	359
//OS	327
//AC	279
//ID	277
//OX	277
//PE	277
//SQ	277
//GN	194
//RG	33
//OG	0
//OH	0

