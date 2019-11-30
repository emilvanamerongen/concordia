/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;

/**
 *
 * @author emil
 */
public class ListenSocket extends Thread{
    private ServerSocket server;
    Integer port = 0;
    private String ip = "localhost";
    private ConcurrentLinkedQueue<ElasticRequest> requestqueue = new ConcurrentLinkedQueue();
    private ArrayList<RequestWorker> requestworkers = new ArrayList<>();
    private Integer num_workers = 1;
    private Boolean started = false;
    private Boolean active = true;
    private RestHighLevelClient client;
    private ArrayList<Client> clients = new ArrayList<>();
    private Client primaryclient = new Client();
    
    public ListenSocket(Integer port, Integer num_workers, RestHighLevelClient client) throws IOException{
        this.port = port;
        this.server = new ServerSocket(port);
        System.out.println("ListenSocket$ Listening for requests on: "+server+" "+server.getLocalSocketAddress()+" "+server.getLocalPort());
        this.num_workers = num_workers;
        this.client = client;
    }


    
    public void run(){
        Thread.currentThread().setName("ListenSocket");
        if (!started){
            startworkers();
        }

               
        while (active){
            Boolean freesocket = false;
            for (Client client : clients){
                if (!client.getConnected()){
                    freesocket = true;
                }
            }
            if (!freesocket){
//                Client newclient = new Client(this.client, getServer(),ip,port,requestqueue,false);
//                newclient.start();
//                clients.add(newclient);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(ListenSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    private void startworkers(){
        int counter = 0;
        while (counter <= num_workers){
            requestworkers.add(new RequestWorker(requestqueue,client));
            counter++;
        }
        for (RequestWorker requestworker : requestworkers){
            requestworker.start();
        }
        started = true;
    }
    
    private void stopworkers(){
        for (RequestWorker requestworker : requestworkers){
            requestworker.setActive(false);
        }
        requestworkers.clear();
        started = false;
    }

    /**
     * @return the server
     */
    public synchronized ServerSocket getServer() {
        return server;
    }

    /**
     * @param server the server to set
     */
    public synchronized void setServer(ServerSocket server) {
        this.server = server;
    }
    
}
