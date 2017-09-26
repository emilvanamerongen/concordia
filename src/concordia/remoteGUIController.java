/*
 * Tos change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import Annotator.Searcher;
import Annotator.AnnotationProcess;
import Annotator.AnnotationFileInfo;
import CazyModule.CazyAnnotator;

import FileManager.Filemanager;
import Refdbmanager.dbmanager;
import Refdbmanager.header;
import Refdbmanager.refdb;
import TabDelimitedModule.TabDelimitedIndexer;
import Tools.ByteArrayWrapper;
import UniprotModule.IDMappingIndexer;
import UniprotModule.UniprotAnnotator;
import UniprotModule.uniprotindexer;
import gnu.trove.map.hash.THashMap;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import static javafx.animation.Animation.INDEFINITE;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

/**
 *
 * @author emilvanamerongen
 */
public class remoteGUIController implements Initializable {
    // <editor-fold desc="main gui FXML">
    @FXML TabPane pages;
    @FXML ImageView logoview;
    @FXML Button refdbbutton;
    @FXML Button dblinkbutton;
    @FXML Button runannotationbutton;
    @FXML Button quicksearchbutton;
    @FXML Button serverbutton;
    @FXML Button settingsbutton;
    public static ConcurrentHashMap<String, Searcher> indexstorage = new ConcurrentHashMap<>();
    
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
    

    
    // <editor-fold desc="images"> 
    private final Image IMAGE_empty  = new Image("file:src"+File.separator+"img"+File.separator+"empty.png");
    private final Image logo1  = new Image("file:src"+File.separator+"img"+File.separator+"logo-concordia.png");
    private final Image IMAGE_F  = new Image("file:src"+File.separator+"img"+File.separator+"filtered.png");
    private final Image IMAGE_C  = new Image("file:src"+File.separator+"img"+File.separator+"cazy.png");
    private final Image IMAGE_U  = new Image("file:src"+File.separator+"img"+File.separator+"uniprot.png");
    private final Image IMAGE_FC  = new Image("file:src"+File.separator+"img"+File.separator+"cazy+filtered.png");
    private final Image IMAGE_CU  = new Image("file:src"+File.separator+"img"+File.separator+"cazy+uniprot.png");
    private final Image IMAGE_FU  = new Image("file:src"+File.separator+"img"+File.separator+"uniprot+filtered.png");
    private final Image IMAGE_FUC  = new Image("file:src"+File.separator+"img"+File.separator+"cazy+uniprot+filtered.png");
    // </editor-fold>
    
    // <editor-fold desc="database manager">
    //-----------------------------------------------------------------------------------------------------------------------------------------
    public void dbmanagerinit(){
        //adddatapane init
        typelist.addAll("tab-delimited","template (tab)","uniprot","uniprot ID-mapping");
        typebox.setItems(typelist);
        closebutton.setVisible(false);
        typebox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
            Integer boxindex = number2.intValue();
            if (boxindex == 0 || boxindex == 1){
                headerindexlabel.setDisable(false);
                headerindexfield.setDisable(false);
            } else { 
                headerindexlabel.setDisable(true);
                headerindexfield.setDisable(true);
                headerindexfield.setText("0");
        }
        }});
        // checkbox column 
        TableColumn<refdb, Boolean> active = new TableColumn<>("");
        active.setCellValueFactory( f -> f.getValue().getSelect());
        active.setCellFactory( tc -> new CheckBoxTableCell<>());
        active.setMaxWidth(500.0);
        //type column
        TableColumn<refdb, String> typecolumn = new TableColumn<>("type");
        typecolumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        //db name column
        TableColumn<refdb, String> namecolumn = new TableColumn<>("database name");
        namecolumn.setCellValueFactory(new PropertyValueFactory<>("dbname"));
        namecolumn.setCellFactory(TextFieldTableCell.<refdb>forTableColumn());
        namecolumn.setOnEditCommit(
        (CellEditEvent<refdb, String> t) -> {
        ((refdb) t.getTableView().getItems().get(
            t.getTablePosition().getRow())
            ).setDbname(t.getNewValue());
            dbmanager.setchangetostoragelines();
        });
        //headerindex column
        TableColumn<refdb, String> headerindexcolumn = new TableColumn<>("header index");
        headerindexcolumn.setCellValueFactory(new PropertyValueFactory<>("headerindex"));
        headerindexcolumn.setCellFactory(TextFieldTableCell.<refdb>forTableColumn());
        headerindexcolumn.setOnEditCommit(
        (CellEditEvent<refdb, String> t) -> {
        ((refdb) t.getTableView().getItems().get(
            t.getTablePosition().getRow())
            ).setHeaderindex(t.getNewValue());
            dbmanager.setchangetostoragelines();
            
        });
        //file location column
        TableColumn<refdb, String> locationcolumn = new TableColumn<>("file location");
        locationcolumn.setCellValueFactory(new PropertyValueFactory<>("filepath"));
        locationcolumn.setCellFactory(TextFieldTableCell.<refdb>forTableColumn());
        locationcolumn.setOnEditCommit(
        (CellEditEvent<refdb, String> t) -> {
        ((refdb) t.getTableView().getItems().get(
            t.getTablePosition().getRow())
            ).setFilepath(t.getNewValue());
            dbmanager.setchangetostoragelines();
        });
        //online location column
        TableColumn<refdb, String> urlcolumn = new TableColumn<>("source URL");
        urlcolumn.setCellValueFactory(new PropertyValueFactory<>("remotelocation"));
        urlcolumn.setCellFactory(TextFieldTableCell.<refdb>forTableColumn());
        urlcolumn.setOnEditCommit(
        (CellEditEvent<refdb, String> t) -> {
        ((refdb) t.getTableView().getItems().get(
            t.getTablePosition().getRow())
            ).setRemotelocation(t.getNewValue());
            dbmanager.setchangetostoragelines();
        });
        //priority column
        TableColumn<refdb, String> prioritycolumn = new TableColumn<>("priority");
        prioritycolumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        prioritycolumn.setCellFactory(TextFieldTableCell.<refdb>forTableColumn());
        prioritycolumn.setOnEditCommit(
        (CellEditEvent<refdb, String> t) -> {
        ((refdb) t.getTableView().getItems().get(
            t.getTablePosition().getRow())
            ).setPriority(t.getNewValue());
            dbmanager.setchangetostoragelines();
            regeneratefilelinkingtables();
        });
        
        //status column
        TableColumn<refdb, String> statuscolumn = new TableColumn<>("Status");
        statuscolumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        locationcolumn.setMaxWidth(400.0);
        dbmanagertable.setItems(dbmanager.getReferencedatabases());
        dbmanagertable.getColumns().addAll(active,namecolumn,typecolumn,headerindexcolumn,locationcolumn,statuscolumn,prioritycolumn);
        dbmanagertable.setEditable(true);
        generatefilelinkingtables();
    }
    public void updatetable(){
        dbmanagertable.setItems(dbmanager.getReferencedatabases());
        regeneratefilelinkingtables();
    }
    
    @FXML public void importrefdb(){
        String type = typebox.getSelectionModel().getSelectedItem().toString();
        String name = namefield.getText();
        String headerindex = "0";
        if (! headerindexfield.getText().isEmpty()){
        headerindex = headerindexfield.getText();
        }
        String localpath = importfilepathfield.getText();
        String priority = priorityfield.getText();
        dbmanager.adddb(type,name,headerindex,localpath,priority); 
        typebox.getSelectionModel().select(0);
        namefield.setText("");
        headerindexfield.setText("");
        importfilepathfield.setText("");
        priorityfield.setText("");
        namefield.setText("");
        regeneratefilelinkingtables();
        closedbpanes();
    }
    
    @FXML public void importfilepicker(){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("select your reference database file");
        File newfile = chooser.showOpenDialog(typebox.getScene().getWindow());
        if (newfile != null){
            importfilepathfield.setText(newfile.getAbsolutePath());
        }
    }
    
    @FXML public void removedb(){
        dbmanager.removeselected();
        updatetable();
    }
    
    @FXML public void closedbpanes(){
        refdbaddpane.setVisible(false);
        adddatabutton.setOpacity(1.0);
        closebutton.setVisible(false);
    }
    
    @FXML public void togglerefdbaddpane(){
        if (refdbaddpane.isVisible()){
            refdbaddpane.setVisible(false);
            adddatabutton.setOpacity(1.0);
            closebutton.setVisible(false);
        } else {
            refdbaddpane.setVisible(true);
            adddatabutton.setOpacity(0.7);
            closebutton.setVisible(true);
        }
    }
    
    public void addrow(ArrayList<String> data){
        System.out.println(data);
        dbmanagertable.getItems().add(data);
    }
    
    @FXML public void forceheaderrescan(){
        for (refdb db : dbmanager.getReferencedatabases()){
            if (db.getSelect().getValue()){
                db.forceheaderreset();
            }
        }
        dbmanager = new dbmanager();
        regeneratefilelinkingtables();
    }
    
    // </editor-fold>
    
    // <editor-fold desc="database linking">
    public void generatefilelinkingtables(){
        ObservableList<refdb> referencedatabases = dbmanager.getReferencedatabases();
        Integer counter = 0;
        while (counter <= 100){
            for (refdb database : referencedatabases){
                if (database.getPriority().equals(counter.toString())){
                        mylinkboxes.add(new LinkBox(database.getDbname(), database.getHeaderset()));  
                }
            }
            counter++;
        }
        linkingspace.getChildren().addAll(mylinkboxes);
    }

    public void regeneratefilelinkingtables(){
        linkingspace.getChildren().clear();
        linkingspace = emptylinkingspace;
        mylinkboxes.clear();
        generatefilelinkingtables();
        
    }
    
    public void linksave(){
        
    }
    
    public void linkread(){
        
        
    }
    
    @FXML public void indexupdate() throws InterruptedException{
        System.out.println("requesting indexupdate");
        System.out.println("----------------------");
        dbmanager.updateallindices();
        indexertimeline.play();

    }
    
    @FXML public void toggleindexprogresspane(){
        indexprogressanchorpane.setVisible(!indexprogressanchorpane.isVisible());   
    }
    
    // <editor-fold desc="indexerloop">

    
    Timeline indexertimeline = new Timeline(Timeline.INDEFINITE, new KeyFrame(Duration.millis(1000), ae -> indexerupdateloop()));
    private Boolean indexeractive = false;
    private Integer inactiverunnumber = 0;
    private Integer activethreads = 0;
    private Boolean indexerupdateserviceactive = false;
    private Integer updatedots = 0;
    
    private void indexerupdateloop(){
        if (!indexerupdateserviceactive){
            indexerupdateserviceactive = true;
            System.out.println("indexer update service started"); 
        }
        
        Integer newactivethreads = 0;
        for (refdb db : dbmanager.getReferencedatabases()){
            newactivethreads += db.getIDmappingindexerthreads().size();
            newactivethreads += db.getTabindexerthreads().size();
            newactivethreads += db.getUniprotindexerthreads().size();
        }
        if (!Objects.equals(newactivethreads, activethreads)){
            //change in amount of active indexers
            activethreads = newactivethreads;
            indexprogressbox.getChildren().clear();
            for (refdb db : dbmanager.getReferencedatabases()){
                for (uniprotindexer thread : db.getUniprotindexerthreads().values()){
                indexprogressbox.getChildren().add(new ActiveThreadElement(thread,"uniprot"));
                }
                for (TabDelimitedIndexer thread : db.getTabindexerthreads().values()){
                indexprogressbox.getChildren().add(new ActiveThreadElement(thread,"tab"));
                }
                for (IDMappingIndexer thread : db.getIDmappingindexerthreads().values()){
                indexprogressbox.getChildren().add(new ActiveThreadElement(thread,"idmapping"));
                }
        }
            
            regeneratefilelinkingtables();
        }
        
        if (indexeractive == false && activethreads != 0){
            indexprogressbutton.setGraphic(new ImageView(loadinggif));
            indexprogressbutton.getGraphic().setVisible(true);
            indexeractive = true;         
        } else if (indexeractive && activethreads == 0) {
            indexprogressbutton.getGraphic().setVisible(false);
            indexeractive = false;   
        }
        if (indexeractive){
            inactiverunnumber = 0;
            if (updatedots == 3){
                updatedots = 0;
            } else { 
                updatedots++;
            }
            
            indexprogresslabel.setText(activethreads+" indexers active"+"...".substring(0,updatedots));
        } else {
            inactiverunnumber++;
            indexprogresslabel.setText(activethreads+" indexers active"+"...".substring(0,updatedots));
        }
        
        if (inactiverunnumber == 10){
            inactiverunnumber = 0;
            indexertimeline.stop();
            indexeractive = false;
            indexerupdateserviceactive = false;
            System.out.println("indexer update service stoped"); 
        }       
    }
    

    
    // </editor-fold>
    
    
    
    // </editor-fold>

    // <editor-fold desc="run annotation">
    //-----------------------------------------------------------------------------------------------------------------------------------------
    UniprotAnnotator uniprotannotator = null;
    ArrayList<Button> activebuttons = new ArrayList<>();
    File uniprotidfile;
    //pipeline code 
    Boolean cazyqueue = false;
    Boolean uniprotqueue = false;
    ArrayList<File> cazyqueueitems;
    Boolean cazyactive = false;
    Properties myproperties = new Properties();
    Filemanager filemanager = new Filemanager();
    long tStart = 0l;
    long tTotal = 0l;



    /**
     *
     */
    @FXML
    public void updateblastresultfilelist(){
        filemanager.blastresultmanager.updatefiles();
        ObservableList<String> names = FXCollections.observableArrayList(filemanager.blastresultmanager.getFilenames());    
        SortedList<String> sorted = names.sorted();
        fileslist.setItems(sorted);     
        fileslist.setCellFactory(param -> new ListCell<String>() {
            private ImageView imageView2 = new ImageView();
            @Override
            public void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(name.replace("ᚒC", "").replace("ᚒF", "").replace("ᚒU", ""));
                }
            }
        });    }

    /**
     *
     */
    @FXML
    public void updateannotationfilelist(){
        filemanager.annotationmanager.updatefiles();
        ObservableList<String> names = FXCollections.observableArrayList(filemanager.annotationmanager.getFilenames());    
        SortedList<String> sorted = names.sorted();
        annotationlist.setItems(sorted);     
        annotationlist.setCellFactory(param -> new ListCell<String>() {
            private ImageView imageView = new ImageView();
            @Override
            public void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    imageView.setImage(IMAGE_empty);
                    if(name.contains("ᚒF"))
                        imageView.setImage(IMAGE_F);
                    else if(name.contains("ᚒC"))
                        imageView.setImage(IMAGE_C);
                    else if(name.contains("ᚒU"))
                        imageView.setImage(IMAGE_U);
                    if(name.contains("ᚒU") && name.contains("ᚒC"))
                        imageView.setImage(IMAGE_CU);
                    if(name.contains("ᚒU") && name.contains("ᚒF"))
                        imageView.setImage(IMAGE_FU);
                    if(name.contains("ᚒF") && name.contains("ᚒC"))
                        imageView.setImage(IMAGE_FC);
                    if(name.contains("ᚒU") && name.contains("ᚒC") && name.contains("ᚒF"))
                        imageView.setImage(IMAGE_FUC);
                    setText(name.replace("ᚒC", "").replace("ᚒF", "").replace("ᚒU", ""));
                    setGraphic(imageView);
                }
            }
        });
    }  

    /**
     *
     * @param event
     */
    @FXML
    public void addblastresultfiles(ActionEvent event){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("move files to blastresult data in project folder");
        List<File> newfiles = chooser.showOpenMultipleDialog(filespane.getScene().getWindow());
        if (newfiles != null){
            for (File file : newfiles){
                file.renameTo(new File(filemanager.blastresultmanager.getblastresultdirectory().getAbsolutePath()+File.separator+file.getName()));
            }
            filemanager.blastresultmanager.updatefiles();
        }
        updateblastresultfilelist();
    }

    /**
     *
     */
    @FXML
    public void deleteblastresultfile(){
        ObservableList selectedfile = fileslist.getSelectionModel().getSelectedItems();
        try{
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete file");
        alert.setHeaderText(selectedfile.get(0)+" wil be permanently deleted");
        alert.setContentText("Are you sure want to permanently delete this file?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            File markedfordelete = filemanager.blastresultmanager.getblastresultfiles().get(selectedfile.get(0));
            Files.delete(markedfordelete.toPath());
            filemanager.blastresultmanager.updatefiles();
        } else {
            System.out.println("deletion canceled");
        }
        } catch (Exception ex){
            System.out.println("no files deleted");
        }
        updateblastresultfilelist();
    }

    /**
     *
     */
    @FXML
    public void deleteannotationfile(){
        ObservableList selectedfile = annotationlist.getSelectionModel().getSelectedItems();
        try{
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete file");
        alert.setHeaderText(selectedfile.get(0)+" wil be permanently deleted");
        alert.setContentText("Are you sure want to permanently delete this file?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            File markedfordelete = filemanager.annotationmanager.getAnnotationfiles().get(selectedfile.get(0));
            Files.delete(markedfordelete.toPath());
            filemanager.annotationmanager.updatefiles();
        } else {
            System.out.println("deletion canceled");
        }
        } catch (Exception ex){
            System.out.println("no files deleted");
        }
        updateannotationfilelist();
    }


    //-----------------------------------------------------------------------------------------------------------------------------------------------
    // </editor-fold>
    
    // <editor-fold desc="run annotation 2">
    private void annotatorinit(){
        ObservableList<refdb> referencedatabases = dbmanager.getReferencedatabases();
        Integer counter = 0;
        while (counter <= 100){
            for (refdb database : referencedatabases){
                if (database.getPriority().equals(counter.toString())){
                    if (! database.getType().equals("template (tab)"))
                        dbpanes.add(new DBpane(database));  
                }
            }
            counter++;
        }
        annotatorbox.getChildren().addAll(dbpanes);
        templatechoicebox.getItems().clear();
        for (refdb database : dbmanager.getReferencedatabases()){
            if (database.getType().equals("template (tab)")){
            templatechoicebox.getItems().add(database.getDbname());}
        }
    }
    
    
    @FXML public void startannotator(){
        ArrayList<File> selecteddata = new ArrayList<File>();
        ObservableList selecteditems;
        selecteditems = fileslist.getSelectionModel().getSelectedItems();
        for (Object item : selecteditems){
            selecteddata.add(filemanager.blastresultmanager.getblastresultfiles().get(item));
        }
        ArrayList<refdb> annotationsources = new ArrayList<refdb>();
        for (DBpane dbpane : remoteGUIController.dbpanes){
            refdb database = dbpane.getDatabase();
            if (dbpane.checkbox.isSelected()){
                annotationsources.add(database);
            }
        }
        refdb template = null;
        String choiceboxselection = templatechoicebox.getSelectionModel().getSelectedItem().toString();
        for (refdb mydb : dbmanager.getReferencedatabases()){
            if (mydb.getDbname().equals(choiceboxselection)){
                template = mydb;
                break;
            }
        }
        
        if (template!=null && selecteddata.size()!=0 && annotationsources.size()!=0){
        annotationprocess = new AnnotationProcess(selecteddata,template,annotationsources);
        annotationprocess.start();
        } else {
            System.out.println("Annotation not started!");
        }
        for (refdb annotationsource : annotationsources){
            for (header myheader : annotationsource.getHeaderset()){
                if (myheader.getIndexed()){
                    annotationdatabox.getChildren().add(new IndexSourceElement(myheader));
                }
            }
        }
        annotatortimeline.play();
    }
    
    @FXML public void preloaddata(){
        ArrayList<refdb> annotationsources = new ArrayList<refdb>();
        for (DBpane dbpane : remoteGUIController.dbpanes){
            refdb database = dbpane.getDatabase();
            if (dbpane.checkbox.isSelected()){
                annotationsources.add(database);
            }
        }
        for (refdb mydb : annotationsources){
                
                for (header myheader : mydb.getHeaderset()){
                    if (myheader.getIndexed()){
                        boolean alreadyloaded = false;
                        try{
                            alreadyloaded = remoteGUIController.indexstorage.get(myheader.getSourcedb()).getData().containsKey(myheader.getHeaderstring());
                        } catch (Exception ex){}
                        
                        if (! alreadyloaded){
                            loadindex(myheader, mydb);
                        } 
                    }
                }
            }
    }
    
    public void loadindex(header myheader, refdb db){ 
            THashMap<ByteArrayWrapper, long[]> index = new THashMap<>(1,0.75f);
                System.out.println("loading: "+filemanager.getIndexdirectory()+File.separator+myheader.getSourcedb()+"."+myheader.getHeaderstring()+".ser");
                try
                {
                   FileInputStream fis = new FileInputStream(filemanager.getIndexdirectory()+File.separator+myheader.getSourcedb()+"."+myheader.getHeaderstring()+".ser");
                   ObjectInput ois = new ObjectInputStream(fis);

                   index = (THashMap) ois.readObject();
                   System.out.println(index.size());

                   remoteGUIController.indexstorage.putIfAbsent(myheader.getSourcedb(), new Searcher(db));
                   remoteGUIController.indexstorage.get(myheader.getSourcedb()).adddata(index, myheader);
                   ois.close();
                   fis.close();
                   
                }catch(IOException ioe)
                {
                   ioe.printStackTrace();
                   return;
                }catch(ClassNotFoundException c)
                {
                   System.out.println("Class not found");
                   c.printStackTrace();
                   return;
                }
                System.out.println("Index loaded");
                
                
        }
    
    // <editor-fold desc="annotatorloop">

    
    Timeline annotatortimeline = new Timeline(Timeline.INDEFINITE, new KeyFrame(Duration.millis(1000), ae -> annotatorupdateloop()));

    private Boolean annotatorupdateserviceactive = false;
    private Integer annotatorupdatedots = 0;
    private Integer prevactivefiles = 0;
    
    private void annotatorupdateloop(){
        if (!annotatorupdateserviceactive){
            annotatorupdateserviceactive = true;
            System.out.println("annotator update service started"); 
            annotationprogressanchorpane.setVisible(true);
            for (AnnotationFileInfo infodata : annotationprocess.getProgressset().values()){
                annotationprogressbox.getChildren().add(new AnnotatorFileElement(infodata));
            }
        }
        int activefiles = annotationprocess.getProgressset().size();
        if (prevactivefiles != activefiles){
            prevactivefiles = activefiles;
            annotationprogressbox.getChildren().clear();
            for (AnnotationFileInfo infodata : annotationprocess.getProgressset().values()){
                annotationprogressbox.getChildren().add(new AnnotatorFileElement(infodata));
            }
        }
        
        Boolean done = false;
        try {
            done = annotationprocess.getDone();
            
            
        } catch (Exception ex){}


        
        if (done){
            inactiverunnumber = 0;
            annotatorupdateserviceactive = false;
            annotationdatabox.getChildren().clear();
            System.out.println("indexer update service stoped"); 
            annotatortimeline.stop();
            
        }       
    }
    

    
    // </editor-fold>
    
    // </editor-fold>
    

    
    
    // <editor-fold desc="settings">
    //-----------------------------------------------------------------------------------------------------------------------------------------
    
    // file manager
    @FXML
    private void setdirectory(ActionEvent event) throws FileNotFoundException, IOException{
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Concordia project folder");
        File newselectedDirectory = chooser.showDialog(filespane.getScene().getWindow());
        if (newselectedDirectory != null){
            filemanager.setProjectdirectory(newselectedDirectory);
        }      
        if (newselectedDirectory != null){
        myproperties.setProperty("projectfolder", filemanager.getProjectdirectory().getAbsolutePath());
        OutputStream out = new FileOutputStream("concordia.properties");
        myproperties.store(out, "This is an optional header comment string");
        out.close();
        filemanager.annotationmanager.updatefiles();
        filemanager.blastresultmanager.updatefiles();
        }
        setdirectorylabel();
    }
    @FXML
    private void openexplorer(ActionEvent event) throws IOException{
        Runtime rt = Runtime.getRuntime();
        try {
            Process pr = rt.exec("nautilus "+filemanager.getProjectdirectory().getAbsolutePath());
        } catch (Exception ex){
            Process pr = rt.exec("explorer "+filemanager.getProjectdirectory().getAbsolutePath());
        }
    }
    
    private void setdirectorylabel(){
        directorylabel.setText(filemanager.getProjectdirectory().getAbsolutePath());
    }
    
    @FXML
    private void changeAvailableRAM(){
        try{
        availableRAM = Integer.parseInt(ramfield.getText());
        myproperties.setProperty("availableRAM", availableRAM.toString());
        myproperties.store(new FileOutputStream("concordia.properties"), "");       
        }catch (Exception ex){}
    }
    
    // </editor-fold>
    
    // <editor-fold desc="main gui">
    //-----------------------------------------------------------------------------------------------------------------------------------------
        //gui manager
    @FXML
    private void switchtab(ActionEvent event){
        try {
        String source = event.getSource().toString();
        if (source.contains("Reference databases")){
            pages.getSelectionModel().select(1);
            refdbbutton.setDisable(true);
            dblinkbutton.setDisable(false);
            runannotationbutton.setDisable(false);
            settingsbutton.setDisable(false);
            serverbutton.setDisable(false);
            quicksearchbutton.setDisable(false);
        }
        else  if (source.contains("Database linking")){
            pages.getSelectionModel().select(2);
            refdbbutton.setDisable(false);
            dblinkbutton.setDisable(true);
            runannotationbutton.setDisable(false);
            settingsbutton.setDisable(false);
            serverbutton.setDisable(false);
            quicksearchbutton.setDisable(false);
        }
        else if (source.contains("Run annotation")){
            pages.getSelectionModel().select(3);
            refdbbutton.setDisable(false);
            dblinkbutton.setDisable(false);
            runannotationbutton.setDisable(true);
            settingsbutton.setDisable(false);
            serverbutton.setDisable(false);
            quicksearchbutton.setDisable(false);
        }
        else if (source.contains("Quick search")){
            pages.getSelectionModel().select(4);
            refdbbutton.setDisable(false);
            dblinkbutton.setDisable(false);
            runannotationbutton.setDisable(false);
            quicksearchbutton.setDisable(true);
            serverbutton.setDisable(false);
            settingsbutton.setDisable(false);
        }
        else if (source.contains("Server")){
            pages.getSelectionModel().select(5);
            refdbbutton.setDisable(false);
            dblinkbutton.setDisable(false);
            serverbutton.setDisable(true);
            runannotationbutton.setDisable(false);
            settingsbutton.setDisable(false);
            quicksearchbutton.setDisable(false);
        }
        else if (source.contains("Settings")){
            pages.getSelectionModel().select(6);
            refdbbutton.setDisable(false);
            dblinkbutton.setDisable(false);
            runannotationbutton.setDisable(false);
            settingsbutton.setDisable(true);
            serverbutton.setDisable(false);
            quicksearchbutton.setDisable(false);
        } else{
            pages.getSelectionModel().select(0);
            refdbbutton.setDisable(false);
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
        fileslist.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        annotationlist.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        emptylinkingspace = linkingspace;
        updateblastresultfilelist();
        updateannotationfilelist();
        fileslist.setItems(FXCollections.observableArrayList(filemanager.blastresultmanager.getFilenames()));
        activebuttons.add(activebutton1);
        activebuttons.add(activebutton2);
        activebuttons.add(activebutton3);
        activebuttons.add(activebutton4);
        activebuttons.add(activebutton5);
        activebuttons.add(annotatebutton);
        activebuttons.add(annotationaddbutton);
        activebuttons.add(addcazybutton);
        activebuttons.add(adduniprotbutton);
        try {
            myproperties.load(new FileInputStream("concordia.properties"));
            availableRAM = Integer.parseInt(myproperties.getProperty("availableRAM"));
            ramfield.setText(availableRAM.toString());
        } catch (Exception ex) {       
        }
        //settings
        setdirectorylabel();
        //database manager
        dbmanagerinit();
        //annotator
        annotatorinit();
        //timelines
        indexertimeline.setCycleCount(INDEFINITE);
        annotatortimeline.setCycleCount(INDEFINITE);
        
    }    
    // </editor-fold>
}
