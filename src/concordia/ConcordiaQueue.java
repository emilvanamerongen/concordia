/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author emil3
 */
public class ConcordiaQueue extends Thread{
    public Boolean active = true; 
    public ConcurrentLinkedQueue<Task> queue = new ConcurrentLinkedQueue<>();
    public Task currenttask = new Task("none");
            
    public ConcordiaQueue(){
        
    }
    
    @Override
    public void run(){
        while (active){
            try { Thread.sleep(10); } catch (InterruptedException ex) {Logger.getLogger(ConcordiaQueue.class.getName()).log(Level.SEVERE, null, ex);}
            if (!queue.isEmpty()){
                currenttask = queue.poll();
                processtask(currenttask);
            }
        }
    }
    
    private void processtask(Task task){
        String type = task.type;
    }
}
