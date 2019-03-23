/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static server.ConcordiaServer.ANSI_GREEN;
import static server.ConcordiaServer.ANSI_RESET;

/**
 *
 * @author emil3
 */
public class ServerProperties {
    private Integer Socketport = 0;
    private Integer num_requestworkers = 1;
    private String username = "";
    private String elasticIP = "";
    private String elasticCLUSTERNAME = "";
    private Integer elasticPORT = 9300;
    private Integer elasticRESTPORT = 9200;
    private String instructionsetfolder = "";
    private String neo4jURI = "bolt://localhost:7687";
    private String neo4jUSER = "java";
    private String neo4jPASSWORD = "";
            
    public ServerProperties(){
    
    }
    
     
    
    public void loadproperties() {
        System.out.println(ANSI_GREEN+"***************");
        File serverproperties = new File("server.properties");
        Path serverpropertiespath = Paths.get(serverproperties.getAbsolutePath());
        List<String> lines = new ArrayList();
        if (serverproperties.exists()){
            try {
                System.out.println("reading server.properties..");
                lines = Files.readAllLines(serverpropertiespath);
                for (String line : lines){
                    if (line.contains("\n")){
                        line = line.replace("\n","");
                    }
                    if (line.contains("=")){
                        String[] linesplit = line.split("=");
                        String propertytype = linesplit[0];
                        switch (propertytype) {
                            case "Socketport":
                                setSocketport((Integer) Integer.parseInt(linesplit[1].trim()));
                                System.out.println("Socketport:"+getSocketport());
                                break;
                            case "username":
                                setUsername(linesplit[1]);
                                System.out.println("username:"+getUsername());
                                break;
                            case "elasticIP":
                                setElasticIP(linesplit[1]);
                                System.out.println("elasticIP:"+getElasticIP());
                                break;
                            case "elasticCLUSTERNAME":
                                setElasticCLUSTERNAME(linesplit[1]);
                                System.out.println("elasticCLUSTERNAME:"+getElasticCLUSTERNAME());
                                break;
                            case "elasticPORT":
                                setElasticPORT((Integer) Integer.parseInt(linesplit[1].trim()));
                                System.out.println("elasticPORT:"+getElasticPORT());
                                break;
                            case "elasticRESTPORT":
                                setElasticRESTPORT((Integer) Integer.parseInt(linesplit[1].trim()));
                                System.out.println("elasticRESTPORT:"+getElasticRESTPORT());
                                break;
                            case "instructionsetfolder":
                                setInstructionsetfolder(linesplit[1]);
                                System.out.println("instructionsetfolder:"+getInstructionsetfolder());
                                break;
                            case "num_requestworkers":
                                setNum_requestworkers((Integer) Integer.parseInt(linesplit[1].trim()));
                                System.out.println("num_requestworkers:"+getNum_requestworkers());
                                break;
                            case "neo4jURI":
                                setNeo4jURI((String) linesplit[1]);
                                System.out.println("neo4jURI:"+getNeo4jURI());
                                break;
                            case "neo4jUSER":
                                setNeo4jUSER((String) linesplit[1]);
                                System.out.println("neo4jUSER:"+getNeo4jUSER());
                                break;
                            case "neo4jPASSWORD":
                                setNeo4jPASSWORD((String) linesplit[1]);
                                System.out.println("neo4jPASSWORD:"+"******");
                                break;
                            default:
                                break;
                        }
                    }
                } 
                }catch (Exception ex){
                    System.out.println("ERROR in properties file: ");
                    for (StackTraceElement trace : ex.getStackTrace()){
                        System.out.println(trace.toString());
                    }
                    System.out.println("REBUILDING FILE...");
                    serverproperties.delete();
                    lines.clear();
            }
        } 

        if (!serverproperties.exists() || lines.isEmpty()){
            System.out.println("server.properties file not found");
            lines.add("Socketport=6868");
            setSocketport((Integer) 6868);
            lines.add("username=concordia");
            setUsername("concordia");
            lines.add("num_requestworkers=1");
            setNum_requestworkers((Integer) 1);
            lines.add("instructionsetfolder=instructionsets");
            setInstructionsetfolder("instructionsets");
            lines.add("elasticIP=localhost");
            setElasticIP("localhost");
            lines.add("elasticPORT=9300");
            setElasticPORT((Integer) 9300);
            lines.add("elasticRESTPORT=9200");
            setElasticPORT((Integer) 9200);
            lines.add("elasticCLUSTERNAME=elasticsearch");
            setElasticCLUSTERNAME("elasticsearch");
            lines.add("neo4jURI=bolt://localhost:7687");
            lines.add("neo4jUSER=java");
            lines.add("neo4jPASSWORD=none");
            try {
                Files.write(serverpropertiespath, lines);
                System.out.println("generated new server.properties file");
            } catch (IOException ex) {
                Logger.getLogger(ConcordiaServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("***************"+ANSI_RESET);
    }

    /**
     * @return the Socketport
     */
    public synchronized Integer getSocketport() {
        return Socketport;
    }

    /**
     * @param Socketport the Socketport to set
     */
    public synchronized void setSocketport(Integer Socketport) {
        this.Socketport = Socketport;
    }

    /**
     * @return the num_requestworkers
     */
    public synchronized Integer getNum_requestworkers() {
        return num_requestworkers;
    }

    /**
     * @param num_requestworkers the num_requestworkers to set
     */
    public synchronized void setNum_requestworkers(Integer num_requestworkers) {
        this.num_requestworkers = num_requestworkers;
    }

    /**
     * @return the username
     */
    public synchronized String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public synchronized void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the elasticIP
     */
    public synchronized String getElasticIP() {
        return elasticIP;
    }

    /**
     * @param elasticIP the elasticIP to set
     */
    public synchronized void setElasticIP(String elasticIP) {
        this.elasticIP = elasticIP;
    }

    /**
     * @return the elasticCLUSTERNAME
     */
    public synchronized String getElasticCLUSTERNAME() {
        return elasticCLUSTERNAME;
    }

    /**
     * @param elasticCLUSTERNAME the elasticCLUSTERNAME to set
     */
    public synchronized void setElasticCLUSTERNAME(String elasticCLUSTERNAME) {
        this.elasticCLUSTERNAME = elasticCLUSTERNAME;
    }

    /**
     * @return the elasticPORT
     */
    public synchronized Integer getElasticPORT() {
        return elasticPORT;
    }

    /**
     * @param elasticPORT the elasticPORT to set
     */
    public synchronized void setElasticPORT(Integer elasticPORT) {
        this.elasticPORT = elasticPORT;
    }

    /**
     * @return the elasticRESTPORT
     */
    public synchronized Integer getElasticRESTPORT() {
        return elasticRESTPORT;
    }

    /**
     * @param elasticRESTPORT the elasticRESTPORT to set
     */
    public synchronized void setElasticRESTPORT(Integer elasticRESTPORT) {
        this.elasticRESTPORT = elasticRESTPORT;
    }

    /**
     * @return the instructionsetfolder
     */
    public synchronized String getInstructionsetfolder() {
        return instructionsetfolder;
    }

    /**
     * @param instructionsetfolder the instructionsetfolder to set
     */
    public synchronized void setInstructionsetfolder(String instructionsetfolder) {
        this.instructionsetfolder = instructionsetfolder;
    }

    /**
     * @return the neo4jURI
     */
    public String getNeo4jURI() {
        return neo4jURI;
    }

    /**
     * @param neo4jURI the neo4jURI to set
     */
    public void setNeo4jURI(String neo4jURI) {
        this.neo4jURI = neo4jURI;
    }

    /**
     * @return the neo4jUSER
     */
    public String getNeo4jUSER() {
        return neo4jUSER;
    }

    /**
     * @param neo4jUSER the neo4jUSER to set
     */
    public void setNeo4jUSER(String neo4jUSER) {
        this.neo4jUSER = neo4jUSER;
    }

    /**
     * @return the neo4jPASSWORD
     */
    public String getNeo4jPASSWORD() {
        return neo4jPASSWORD;
    }

    /**
     * @param neo4jPASSWORD the neo4jPASSWORD to set
     */
    public void setNeo4jPASSWORD(String neo4jPASSWORD) {
        this.neo4jPASSWORD = neo4jPASSWORD;
    }
    

}
