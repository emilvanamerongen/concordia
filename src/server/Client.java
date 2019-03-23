/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.carrotsearch.hppc.ObjectLookupContainer;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;

/**
 *
 * @author emil
 */
public class Client extends Thread{
    private Boolean connected = false;
    private TransportClient client;
    private ServerSocket server;
    private Integer port = 0;
    private String ip = "localhost";
    private ConcurrentLinkedQueue<ElasticRequest> requestqueue = new ConcurrentLinkedQueue();
    private ArrayList<String> commandhistory = new ArrayList<>();
    private Boolean started = false;
    private Boolean active = true;
    private Boolean stayalive = false;
    private Boolean remotemode = false;
    private Boolean verified = false;
    private String user = "guest";
    private String password = "";
    
    public Client(TransportClient client,ServerSocket server,String ip,Integer port,ConcurrentLinkedQueue<ElasticRequest> requestqueue,Boolean stayalive){
        this.client = client;
        this.server = server;
        this.ip = ip;
        this.port = port;
        this.requestqueue = requestqueue;
        this.stayalive = stayalive;
        
    }

    Client() {
        
    }
    
    public void run(){
        Thread.currentThread().setName("client "+Thread.activeCount());
        while(getActive()){
            System.out.println("client waiting");
            try{
            connected = false;
            Socket socketclient = getServer().accept();
            connected = true;
            BufferedReader in = new BufferedReader(new InputStreamReader(socketclient.getInputStream()));
            PrintWriter out = new PrintWriter(socketclient.getOutputStream());
            while (!verified){
                out.write("##LOGIN: (user:password)\n");
                out.flush();  
                String loginline = in.readLine();
                if (!loginline.equals("guest")){
                    String[] loginlinesplit = loginline.split(":");
                    user = loginlinesplit[0];
                    password = loginlinesplit[1];
                    UserManager myusermanager = new UserManager();
                    verified = myusermanager.verify(user, password);
                }
            }
            
            out.write("##connected\n");
            out.flush();
            while (!socketclient.isInputShutdown()){
                String line = in.readLine();
                if (line==null){
                    break;
                }
                if (line!=null){
                if (!remotemode){
                // SIMPLE SEARCH
                if (line.startsWith("search")){
                    HashMap<String,ArrayList<String>> parameters = new HashMap<>();
                    line = line.substring(7);
                    line+=" ";
                    Boolean devider = false;
                    Boolean escaped = false;
                    String paramtype = "";
                    String value = "";
                    ArrayList<String> values = new ArrayList<>();

                    for(int i = 0; i < line.length(); i++)
                    {
                       char c = line.charAt(i);
                       //special characters
                       if (c == '"'){
                           escaped = !escaped;
                       } else if (!escaped && c == '='){ //devider
                           devider = true;
                       } else if ((!escaped && c == ' ') || i == line.length()-1){ //execute and clear
                           if (!value.isEmpty()){
                               values.add(value);
                               value = "";
                           }
                           parameters.put(paramtype, values);
                           devider = false;
                           escaped = false;
                           paramtype = "";
                           values = new ArrayList<>();
                       } else if (!escaped && c == ','){ //seperate value
                           values.add(value);
                           value = "";
                       } else{
                       //normal characters
                            if (devider){
                                value+=c;
                            } else {
                                paramtype+=c;
                            }
                       }
                    }
                    out.write("##parameters:"+"\n");
                    for (String parameter : parameters.keySet()){
                        out.write(parameter+"\n");
                        for (String paramvalue : parameters.get(parameter)){
                            out.write("\t"+paramvalue);
                        }
                        out.write("\n");
                        out.flush();
                    }
                    out.write("###########"+"\n");
                    out.flush();
                    if (parameters.containsKey("query") && parameters.containsKey("outputfile")){
                        File outputfile = new File(parameters.get("outputfile").get(0));
                        LinkedHashMap<String,ArrayList<String>> query = new LinkedHashMap<>();
                        for (String paramvalue : parameters.get("query")){
                            String[] parametersplit = paramvalue.split(":");
                            query.putIfAbsent(parametersplit[1], new ArrayList<>());
                            query.get(parametersplit[1]).add(parametersplit[0]);
                        }
                        System.out.println("##search request accepted");

                        out.write("##search request accepted\n");
                        out.flush();

                        requestqueue.add(new ElasticRequest(parameters.get("query").toString(),query,outputfile,out));

                    } else {
                        out.write("##ERROR: missing parameters\n");
                    }
                } else if (line.startsWith("info")){
                    out.write("##connection info\n");
                    out.write(client.connectedNodes().toString()+"\n");
                    out.flush();
                } else if (line.startsWith("REMOTE")){
                    remotemode = true;
                    out.write("##remote connection accepted\n");
                    out.flush();
                }
                // REMOTE MODE
                } else {
                     if (line.startsWith("STATUS")){
                         try {
                         out.write(client.connectedNodes().toString()+"\n");
                         } catch (Exception ex){
                             out.write(ex.toString());
                         }
                         out.flush();
                     }
                }
                }
                }
            
            } catch (IOException ex) {
                System.out.println("Read failed "+ex);
                //System.exit(-1);
            }
            
            if (!stayalive){
                active = false;
                break;
            }
        }   

    System.out.println("Socket Closed");
    }

    
    //incoming REMOTE requests
    private void remoteprocess(String line, BufferedReader in, PrintWriter out){
        if (line.startsWith("getElasticStructure")){
                        try {
                            LinkedHashMap<String, ArrayList<Object>> elasticstructure = getElasticStructure();
                            for (String index : elasticstructure.keySet()){
                                out.write(index+"|"+elasticstructure.get(index).toString());
                                out.flush();
                            }       
                        } catch (InterruptedException | ExecutionException ex) {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                        }
        }
        
        
    }
    
    
    
    
    public LinkedHashMap<String, ArrayList<Object>> getElasticStructure() throws InterruptedException, ExecutionException{
        LinkedHashMap<String,ArrayList<Object>> elasticstructure = new LinkedHashMap<>();
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> structure = indicesAdminClient.getMappings(new GetMappingsRequest()).get().getMappings();
        
        for (ObjectCursor<String> indexname : structure.keys()){
            if (!indexname.value.startsWith(".")){
                elasticstructure.put(indexname.value, new ArrayList<>());
                ImmutableOpenMap<String, MappingMetaData> mapping = structure.get(indexname.value);
                for (ObjectCursor<MappingMetaData> mappingdata : mapping.values()){
                    elasticstructure.get(indexname.value).addAll(mappingdata.value.getSourceAsMap().values());
                }
            }
        }
        return elasticstructure;
    }
    /**
     * @return the connected
     */
    public synchronized Boolean getConnected() {
        return connected;
    }

    /**
     * @param connected the connected to set
     */
    public synchronized void setConnected(Boolean connected) {
        this.connected = connected;
    }

    /**
     * @return the client
     */
    public TransportClient getClient() {
        return client;
    }

    /**
     * @param client the client to set
     */
    public void setClient(TransportClient client) {
        this.client = client;
    }

    /**
     * @return the server
     */
    public ServerSocket getServer() {
        return server;
    }

    /**
     * @param server the server to set
     */
    public void setServer(ServerSocket server) {
        this.server = server;
    }

    /**
     * @return the port
     */
    public Integer getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the started
     */
    public Boolean getStarted() {
        return started;
    }

    /**
     * @param started the started to set
     */
    public void setStarted(Boolean started) {
        this.started = started;
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
     * @return the commandhistory
     */
    public synchronized ArrayList<String> getCommandhistory() {
        return commandhistory;
    }

    /**
     * @param commandhistory the commandhistory to set
     */
    public synchronized void setCommandhistory(ArrayList<String> commandhistory) {
        this.commandhistory = commandhistory;
    }
    
}
