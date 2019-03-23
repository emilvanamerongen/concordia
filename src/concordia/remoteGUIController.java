/*
 * Tos change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import Annotator.Searcher;
import Annotator.AnnotationProcess;

import FileManager.Filemanager;
import Refdbmanager.dbmanager;
import UniprotModule.UniprotAnnotator;
import controller.ServerConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import server.ConcordiaServer;

/**
 *
 * @author emilvanamerongen
 */
public class remoteGUIController implements Initializable {
    // <editor-fold desc="main gui FXML">
    @FXML TabPane pages;
    @FXML ImageView logoview;
    @FXML Button statusbutton;
    @FXML Button dblinkbutton;
    @FXML Button runannotationbutton;
    @FXML Button quicksearchbutton;
    @FXML Button serverbutton;
    @FXML Button settingsbutton;
    @FXML ChoiceBox serverselectionchoicebox;
    @FXML Button connectbutton;
    @FXML Button editserverbutton;
    @FXML Label connectionstatuslabel;
    
    
    public static ConcurrentHashMap<String, Searcher> indexstorage = new ConcurrentHashMap<>();
    
    // </editor-fold>
    
    // <editor-fold desc="addservers FXML">
    @FXML ListView serverlistview;
    @FXML TextField addserverfield;
    @FXML Button addserveraddbutton;
    @FXML Button addserverremovebutton;
    @FXML AnchorPane addserverpane;
    @FXML Button addserverclosebutton;
    
    // </editor-fold>
    
    // <editor-fold desc="database manager FXML">
    @FXML TableView dbmanagertable;
    public static dbmanager dbmanager = new dbmanager();
    @FXML VBox refdbaddpane;
    @FXML Button adddatabutton;
    @FXML Button closebutton;
    @FXML ChoiceBox typebox;
    @FXML TextField namefield;
    @FXML Label headerindexlabel;
    @FXML TextField headerindexfield;
    @FXML TextField priorityfield;
    @FXML TextField importfilepathfield;
    private ObservableList<String> typelist = FXCollections.observableArrayList();
    // </editor-fold>
    
    // <editor-fold desc="database linking FXML">
    @FXML HBox linkingspace;
    HBox emptylinkingspace;
    ArrayList<LinkBox> mylinkboxes = new ArrayList<>();
    File linkfile = new File("linkfile.txt");
    @FXML Button indexbutton;
    @FXML ScrollPane linkingspacescrollpane;
    @FXML Button indexprogressbutton;
    @FXML Label indexprogresslabel;
    @FXML AnchorPane indexprogressanchorpane;
    @FXML VBox indexprogressbox;
    Image loadinggif = new Image("file:src"+File.separator+"img"+File.separator+"ajax-loader.gif");
    // </editor-fold>
    
    // <editor-fold desc="annotation page FXML">
    @FXML CheckBox uniprotcheck;
    @FXML CheckBox cazycheck;
    @FXML ListView uniprotlist;
    @FXML ListView cazylist;
    @FXML ProgressBar mainprogressbar;
    @FXML Label progresslabel;
    @FXML Button activebutton1;
    @FXML Button activebutton2;
    @FXML Button activebutton3;
    @FXML Button activebutton4;
    @FXML Button activebutton5;
    @FXML Label timeremaininglabel;
    @FXML Button adduniprotbutton;
    @FXML Button addcazybutton;
    @FXML ListView uniprotlist1;
    @FXML ListView cazylist1;
    @FXML Label idmappinglabel;
    @FXML TabPane filespane;
    @FXML ListView fileslist;
    @FXML ListView annotationlist;
    @FXML Button directorybutton;
    @FXML Button annotatebutton;
    @FXML Button annotationaddbutton;
    @FXML VBox annotatorbox;
    @FXML ChoiceBox templatechoicebox;
    @FXML VBox annotationprogressbox;
    @FXML VBox annotationdatabox;
    @FXML AnchorPane annotationprogressanchorpane;
    private AnnotationProcess annotationprocess = null;
    
    public static ArrayList<DBpane> dbpanes = new ArrayList();
    // </editor-fold>
    
    // <editor-fold desc="settings page FXML">
    @FXML TextField ramfield;
    @FXML Label directorylabel;
    public Integer availableRAM = 6;
    // </editor-fold>
    
    
    // <editor-fold desc="vars">
    private Boolean indexeractive = false;
    private Integer inactiverunnumber = 0;
    private Integer activethreads = 0;
    private Boolean indexerupdateserviceactive = false;
    private Integer updatedots = 0;
    Filemanager filemanager = new Filemanager();
    Properties myproperties = new Properties();
    ObservableList<String> savedservers = FXCollections.observableArrayList();
    File savedserversfile = new File("savedservers.txt");
    public ServerConnection serverconnection = new ServerConnection();
    private Boolean connected = false;
    private Boolean wasconnected = false;
    // </editor-fold>
    
    
    
    // <editor-fold desc="server connection">
    private void choiceboxinit(){
        serverselectionchoicebox.getItems().clear();
        serverselectionchoicebox.setItems(savedservers);
    }
    @FXML
    private void connect(ActionEvent event){
        if (!connectbutton.getText().equals("DISCONNECT")){
            serverconnection.active = true;
            String selectedserver = serverselectionchoicebox.getSelectionModel().getSelectedItem().toString();
            System.out.println("connecting to: "+selectedserver);
            serverconnection.connect(selectedserver,this);
            connectiontesttimeline.play();
        } else {
            try{
            
            serverconnection = new ServerConnection();
            connectbutton.setText("Connect");
            connectionstatuslabel.setText("Disconnected");
            connectionstatuslabel.setTextFill(Color.web("#ff0000"));
            setConnected(false);
            serverselectionchoicebox.setDisable(false);
            editserverbutton.setDisable(false);
            
            } catch (NullPointerException ex){
                
            }
        }
    }
    
    Timeline connectiontesttimeline = new Timeline(Timeline.INDEFINITE, new KeyFrame(Duration.millis(1000), ae -> connectiontest()));
    
    public void connectiontest(){
        if (getConnected() && !wasconnected){
            serverconnection.start();
            connectbutton.setText("DISCONNECT");
            connectionstatuslabel.setText("Connected");
            connectionstatuslabel.setTextFill(Color.web("#00FF00"));
            serverselectionchoicebox.setDisable(true);
            editserverbutton.setDisable(true);
            wasconnected = true;
            statusbutton.setDisable(false);
            dblinkbutton.setDisable(false);
            runannotationbutton.setDisable(false);
            settingsbutton.setDisable(false);
            serverbutton.setDisable(false);
            quicksearchbutton.setDisable(false);
            addserverpane.setVisible(true);
            statuschecktimeline.play();
            
        }  else if (wasconnected){
            connectbutton.setText("Connect");
            connectionstatuslabel.setText("Disconnected");
            connectionstatuslabel.setTextFill(Color.web("#ff0000"));
            serverselectionchoicebox.setDisable(false);
            editserverbutton.setDisable(false);
            wasconnected = false;
            
        }
    }
  
    //status
    
    Timeline statuschecktimeline = new Timeline(Timeline.INDEFINITE, new KeyFrame(Duration.millis(10000), st -> statuscheck()));

    private void statuscheck(){
        System.out.println("checking");
        Boolean socketconnection = false;
        Boolean elasticsearchserver = false;
        serverconnection.outputwriter.write("STATUS\n");
        serverconnection.outputwriter.flush();
    }
    // </editor-fold>
    
    
    // <editor-fold desc="add server">
    
    @FXML
    private void editserverbutton(ActionEvent event){
        pages.getSelectionModel().select(0);
        if (connected){
        statusbutton.setDisable(false);
        dblinkbutton.setDisable(false);
        runannotationbutton.setDisable(false);
        settingsbutton.setDisable(false);
        serverbutton.setDisable(false);
        quicksearchbutton.setDisable(false);
        addserverpane.setVisible(true);
        }
    }
    
    @FXML 
    private void addserver(){
        String text = addserverfield.getText();
        if (text.contains(":")){
            savedservers.add(text);
            addserversupdate(false);
        }
    }
    
    private void addserversupdate(Boolean init){
        serverlistview.setItems(savedservers);
        if (!init){
        if (savedserversfile.exists()){
            try {
                FileWriter savedserverwriter = new FileWriter(savedserversfile.getAbsolutePath());
                String writestring = "";
 
                System.out.println("writing savedservers.txt");

                for (String line : savedservers){
                    writestring += line+"\n";
                    
                }
                savedserverwriter.write(writestring);
                savedserverwriter.close();
            } catch (IOException ex) {
                Logger.getLogger(ConcordiaServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        }
    }
    
    @FXML 
    private void removeserver(){
        savedservers.removeAll(serverlistview.getSelectionModel().getSelectedItems());
        addserversupdate(false);
    }
    
    @FXML
    private void closeaddserver(){
        addserverpane.setVisible(false);
    }
    
    
    // </editor-fold>
    
    // <editor-fold desc="main gui">
    //-----------------------------------------------------------------------------------------------------------------------------------------
        //gui manager
    @FXML
    private void switchtab(ActionEvent event){
        try {
        String source = event.getSource().toString();
        if (source.contains("Status")){
            pages.getSelectionModel().select(1);
            statusbutton.setDisable(true);
            dblinkbutton.setDisable(false);
            runannotationbutton.setDisable(false);
            settingsbutton.setDisable(false);
            serverbutton.setDisable(false);
            quicksearchbutton.setDisable(false);
        }
        else  if (source.contains("Database linking")){
            pages.getSelectionModel().select(2);
            statusbutton.setDisable(false);
            dblinkbutton.setDisable(true);
            runannotationbutton.setDisable(false);
            settingsbutton.setDisable(false);
            serverbutton.setDisable(false);
            quicksearchbutton.setDisable(false);
        }
        else if (source.contains("Run annotation")){
            pages.getSelectionModel().select(3);
            statusbutton.setDisable(false);
            dblinkbutton.setDisable(false);
            runannotationbutton.setDisable(true);
            settingsbutton.setDisable(false);
            serverbutton.setDisable(false);
            quicksearchbutton.setDisable(false);
        }
        else if (source.contains("Quick search")){
            pages.getSelectionModel().select(4);
            statusbutton.setDisable(false);
            dblinkbutton.setDisable(false);
            runannotationbutton.setDisable(false);
            quicksearchbutton.setDisable(true);
            serverbutton.setDisable(false);
            settingsbutton.setDisable(false);
        }
        else if (source.contains("Server")){
            pages.getSelectionModel().select(5);
            statusbutton.setDisable(false);
            dblinkbutton.setDisable(false);
            serverbutton.setDisable(true);
            runannotationbutton.setDisable(false);
            settingsbutton.setDisable(false);
            quicksearchbutton.setDisable(false);
        }
        else if (source.contains("Settings")){
            pages.getSelectionModel().select(6);
            statusbutton.setDisable(false);
            dblinkbutton.setDisable(false);
            runannotationbutton.setDisable(false);
            settingsbutton.setDisable(true);
            serverbutton.setDisable(false);
            quicksearchbutton.setDisable(false);
        } else{
            pages.getSelectionModel().select(0);
            statusbutton.setDisable(false);
            dblinkbutton.setDisable(false);
            runannotationbutton.setDisable(false);
            settingsbutton.setDisable(false);
            serverbutton.setDisable(false);
            quicksearchbutton.setDisable(false);
            
        }
        } catch (Exception ex){
            System.out.println("error switching to page");
        }
    }  
    
    
    // </editor-fold>
    
   
    // <editor-fold desc="init">
    //-----------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("GUI controller started");
        Path savedserverspath = Paths.get(savedserversfile.getAbsolutePath());
        List<String> lines = new ArrayList();
        if (savedserversfile.exists()){
            try {
                System.out.println("reading savedservers.txt");
                lines = Files.readAllLines(savedserverspath);
                for (String line : lines){
                    line = line.replace("\n","");
                    savedservers.add(line);
                }
            } catch (IOException ex) {
                Logger.getLogger(ConcordiaServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        
        
        if (!savedserversfile.exists() || lines.isEmpty()){
            try {
                Files.write(savedserverspath, lines);
                System.out.println("generated new savedservers.txt file");
            } catch (IOException ex) {
                Logger.getLogger(ConcordiaServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        try {
            choiceboxinit();
            myproperties.load(new FileInputStream("concordia.properties"));
            availableRAM = Integer.parseInt(myproperties.getProperty("availableRAM"));
            ramfield.setText(availableRAM.toString());
        } catch (IOException | NumberFormatException ex) {       
        }
        
        addserverpane.setVisible(false);
        addserversupdate(true);
    }    
    // </editor-fold>

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
}
