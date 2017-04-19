/*
 * Tos change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import CazyModule.CazyAnnotator;
import FileManager.Filemanager;
import Refdbmanager.dbmanager;
import Refdbmanager.refdb;
import UniprotModule.UniprotAnnotator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;

/**
 *
 * @author emilvanamerongen
 */
public class GUIController implements Initializable {
    // <editor-fold desc="main gui FXML">
    @FXML TabPane pages;
    @FXML ImageView logoview;
    // </editor-fold>
    
    // <editor-fold desc="database manager FXML">
    @FXML TableView dbmanagertable;
    public static dbmanager dbmanager = new dbmanager();
    @FXML Pane refdbaddpane;
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
    // </editor-fold>
    
    // <editor-fold desc="settings page FXML">
    @FXML CheckBox lowrammode;
    @FXML Label directorylabel;
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
        // checkbox column

        
        TableColumn<refdb, Boolean> active = new TableColumn<>("");
        active.setCellValueFactory( f -> f.getValue().getSelect());
        active.setCellFactory( tc -> new CheckBoxTableCell<>());
        //type column
        TableColumn<refdb, String> typecolumn = new TableColumn<>("type");
        typecolumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        //db name column
        TableColumn<refdb, String> namecolumn = new TableColumn<>("database name");
        namecolumn.setCellValueFactory(new PropertyValueFactory<>("dbname"));
        //headerindex column
        TableColumn<refdb, Integer> headerindexcolumn = new TableColumn<>("header index");
        headerindexcolumn.setCellValueFactory(new PropertyValueFactory<>("headerindex"));
        //file location column
        TableColumn<refdb, String> locationcolumn = new TableColumn<>("file location");
        locationcolumn.setCellValueFactory(new PropertyValueFactory<>("filepath"));
        //online location column
        TableColumn<refdb, String> urlcolumn = new TableColumn<>("source URL");
        urlcolumn.setCellValueFactory(new PropertyValueFactory<>("remotelocation"));
        
        dbmanagertable.setItems(dbmanager.getReferencedatabases());
        dbmanagertable.getColumns().addAll(active, typecolumn,namecolumn,headerindexcolumn,locationcolumn,urlcolumn);
    }
    
    
    public void updatedbmanagertable(){
        
        
    }
    
    public void setRefdbPopupVisibility(Boolean vis){
        refdbaddpane.setVisible(false);
    }
    
    public void addrow(ArrayList<String> data){
        System.out.println(data);
        dbmanagertable.getItems().add(data);
    }
    
    // </editor-fold>
    
    // <editor-fold desc="run annotation">
    //-----------------------------------------------------------------------------------------------------------------------------------------
    UniprotAnnotator uniprotannotator = null;
    ArrayList<Button> activebuttons = new ArrayList<>();
    File uniprotidfile;
    //pipeline code 
    Timeline timeline = new Timeline(Timeline.INDEFINITE, new KeyFrame(Duration.millis(1000), ae -> updateloop()));
    Boolean cazyqueue = false;
    Boolean uniprotqueue = false;
    ArrayList<File> cazyqueueitems;
    Boolean cazyactive = false;
    Properties myproperties = new Properties();
    Filemanager filemanager = new Filemanager();
    long tStart = 0l;
    long tTotal = 0l;


    //gui manager
    @FXML
    private void switchtab(ActionEvent event){
        try {
        String source = event.getSource().toString();
        if (source.contains("Reference databases")){
            pages.getSelectionModel().select(1);
        }
        else  if (source.contains("Pipelines")){
            pages.getSelectionModel().select(2);
        }
        else if (source.contains("Run annotation")){
            pages.getSelectionModel().select(3);
        }
        else if (source.contains("Settings")){
            pages.getSelectionModel().select(4);
        } else{
            pages.getSelectionModel().select(0);
        }
        } catch (Exception ex){
            System.out.println("error switching to page");
        }
    }  
    
    @FXML
    private void resettab(){
        filespane.getSelectionModel().select(0);
    }

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
                    imageView2.setImage(IMAGE_empty);
                    if(name.contains("ᚒF"))
                        imageView2.setImage(IMAGE_F);
                    else if(name.contains("ᚒC"))
                        imageView2.setImage(IMAGE_C);
                    else if(name.contains("ᚒU"))
                        imageView2.setImage(IMAGE_U);
                    if(name.contains("ᚒU") && name.contains("ᚒC"))
                        imageView2.setImage(IMAGE_CU);
                    if(name.contains("ᚒU") && name.contains("ᚒF"))
                        imageView2.setImage(IMAGE_FU);
                    if(name.contains("ᚒF") && name.contains("ᚒC"))
                        imageView2.setImage(IMAGE_FC);
                    if(name.contains("ᚒU") && name.contains("ᚒC") && name.contains("ᚒF"))
                        imageView2.setImage(IMAGE_FUC);
                    setText(name.replace("ᚒC", "").replace("ᚒF", "").replace("ᚒU", ""));
                    setGraphic(imageView2);
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
    //BLAST

    /**
     *
     * @param event
     */
    public void addreferencedatabasefiles(ActionEvent event){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("add BLAST database locations");
        ArrayList<File> newfiles = (ArrayList<File>) chooser.showOpenMultipleDialog(filespane.getScene().getWindow());
        if (newfiles != null){
            filemanager.addreferencedatabaselocations(newfiles);
        }
    }
    // annotation window

    /**
     *
     * @param event
     */
    @FXML
    public void annotate(ActionEvent event){
        System.out.println(uniprotidfile.getAbsoluteFile());
        for (Button button : activebuttons){
            button.setDisable(true);
        }
        ArrayList<File> selectedfiles = new ArrayList<File>();
        ArrayList<File> uniprotoutfiles = new ArrayList<File>();
        ObservableList selecteditems;

        selecteditems = fileslist.getSelectionModel().getSelectedItems();
        
        for (Object item : selecteditems){
            selectedfiles.add(filemanager.blastresultmanager.getblastresultfiles().get(item));
            uniprotoutfiles.add(new File(filemanager.annotationmanager.getAnnotationdirectory()+File.separator+item+".ᚒU"));
        } 
        
        tStart = System.currentTimeMillis();
        
        if (uniprotcheck.isSelected()){
            uniprotqueue = true;
            uniprotannotator = new UniprotAnnotator(selectedfiles,uniprotidfile,filemanager.uniprotsources,filemanager.annotationmanager.getAnnotationdirectory(), lowrammode.isSelected());
            uniprotannotator.annotate();
            timeline.play();
        }
        if (cazycheck.isSelected()){
            cazyqueueitems = uniprotoutfiles;
            cazyqueue = true;
        }
        if (cazycheck.isSelected() && !uniprotcheck.isSelected()){
            cazyqueue = false;
            CazyAnnotator cazyannotator = new CazyAnnotator(cazyqueueitems,filemanager.cazysources,filemanager.annotationmanager.getAnnotationdirectory());
            cazyannotator.annotate();
            timeline.play();
        }
    }
    
    /**
     *
     * @param event
     */
    @FXML
    public void addannotation(ActionEvent event){
        ArrayList<File> selectedfiles = new ArrayList<File>();
        ObservableList selecteditems;
        selecteditems = annotationlist.getSelectionModel().getSelectedItems();
        for (Object item : selecteditems){
            selectedfiles.add(filemanager.annotationmanager.getAnnotationfiles().get(item));}
        
        tStart = System.currentTimeMillis();
        if (event.getSource().toString().contains("UniProt")){
            UniprotAnnotator uniprotannotator = new UniprotAnnotator(selectedfiles,uniprotidfile,filemanager.uniprotsources,filemanager.annotationmanager.getAnnotationdirectory(), lowrammode.isSelected());
            uniprotannotator.annotate();
            timeline.play();
        } else if (event.getSource().toString().contains("Cazy")){
            CazyAnnotator cazyannotator = new CazyAnnotator(selectedfiles,filemanager.cazysources,filemanager.annotationmanager.getAnnotationdirectory());
            cazyannotator.annotate();
            timeline.play();
        }
    }

    /**
     *
     */
    public void updateloop(){
        if (!uniprotannotator.mainthread.getComplete()){
            progresslabel.setText(uniprotannotator.mainthread.getProcess()+" "+uniprotannotator.mainthread.getDone());
            long total = uniprotannotator.mainthread.getTotal();
            long done = uniprotannotator.mainthread.getDone();
            double tDelta = System.currentTimeMillis() - tStart;
            double elapsedSeconds = (tDelta / 1000.000000); 
            double timeremaining = (elapsedSeconds/done)*(total-done);
            if (timeremaining < 60.0){
                int rounded = (int) Math.ceil(timeremaining);
                timeremaininglabel.setText(rounded+" seconds remaining");
            } else if (timeremaining < 360.0){
                int remainingminutes = (int) Math.ceil(timeremaining / 60);
                timeremaininglabel.setText(remainingminutes+" minutes remaining");
            } else {
                int remaininghours = (int) Math.ceil(timeremaining / 360);
                timeremaininglabel.setText(remaininghours+" hours remaining");
            }
            try{            
            double progresspercent = done/total;
            
            mainprogressbar.setProgress(progresspercent);
//            //animation update
//            if (uniprotannotator.mainthread.getProcess().contains("UniProt")){
//                cazygif.setVisible(false);
//                uniprotgif.setVisible(true);
//            } else if (uniprotannotator.mainthread.getProcess().contains("Cazy")){
//                uniprotgif.setVisible(false);
//                cazygif.setVisible(true);
//            } else {
//                cazygif.setVisible(false);
//                uniprotgif.setVisible(false);
//            }
            
            if (progresspercent >= 1.0){
                tTotal += elapsedSeconds;
                tStart = System.currentTimeMillis();
                updateannotationfilelist();
            }
            } catch (Exception ex){}    
        }
        else if (cazyqueue){
                    CazyAnnotator cazyannotator = new CazyAnnotator(cazyqueueitems,filemanager.cazysources,filemanager.annotationmanager.getAnnotationdirectory());
                    cazyannotator.annotate();
                    cazyqueue = false;
                    
        } else {
            timeline.stop();
                System.out.println("complete");
                progresslabel.setText("completed in: "+(tTotal/60)+" minutes");
                tTotal = 0l;
                mainprogressbar.setProgress(0.00);
                filespane.getSelectionModel().select(0);
                for (Button button : activebuttons){
                    button.setDisable(false);
                }
        }
    }

    /**
     *
     * @param event
     */
    @FXML
    public void addcazyfiles(ActionEvent event){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("add cazy file locations");
        List<File> newfiles = chooser.showOpenMultipleDialog(filespane.getScene().getWindow());
        ArrayList<File> newfilesarray = new ArrayList<>();
        newfilesarray.addAll(newfiles);
        if (newfiles != null){
            filemanager.addcazyfilelocations(newfilesarray);
        }
        updatecazyfilelist();
    }

    /**
     *
     * @param event
     */
    @FXML
    public void adduniprotfiles(ActionEvent event){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("add uniprot file locations");
        List<File> newfiles = chooser.showOpenMultipleDialog(filespane.getScene().getWindow());
        ArrayList<File> newfilesarray = new ArrayList<>();
        newfilesarray.addAll(newfiles);
        if (newfiles != null){
            filemanager.adduniprotfilelocations(newfilesarray);
        }
        updateuniprotfilelist();
    }

    /**
     *
     * @param ex
     * @throws FileNotFoundException
     * @throws IOException
     */
    @FXML 
    public void setidmappingfile(ActionEvent ex) throws FileNotFoundException, IOException{
        FileChooser chooser = new FileChooser();
        chooser.setTitle("set uniprot idmapping file location");
        File file = chooser.showOpenDialog(filespane.getScene().getWindow());
        if (file != null){
            uniprotidfile = new File(file.getAbsolutePath());
            myproperties.setProperty("idmappinglocation", file.getAbsolutePath());
            FileOutputStream out = new FileOutputStream("concordia.properties");
            myproperties.store(out, "This is an optional header comment string");
            idmappinglabel.setText(uniprotidfile.getAbsolutePath());
        }
    }
    
    /**
     *
     */
    public void updatecazyfilelist(){
        ArrayList<String> names = new ArrayList<>();
        for (File file : filemanager.cazysources){
            names.add(file.getName());
        }
        cazylist.setItems(FXCollections.observableArrayList(names));
        cazylist1.setItems(FXCollections.observableArrayList(names));
    }
    
    /**
     *
     */
    public void updateuniprotfilelist(){
        ArrayList<String> names = new ArrayList<>();
        for (File file : filemanager.uniprotsources){
            names.add(file.getName());
        }
        uniprotlist.setItems(FXCollections.observableArrayList(names));
        uniprotlist1.setItems(FXCollections.observableArrayList(names));
    }
    
    /**
     *
     */
    @FXML
    public void shrinkidfile(){
        ShrinkIDfile shrinker = new ShrinkIDfile(uniprotidfile);
        shrinker.shrink();
        System.out.println("COMPLETE");
    }
    
    //-----------------------------------------------------------------------------------------------------------------------------------------------
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
    // </editor-fold>
    
    // <editor-fold desc="main gui">
    //-----------------------------------------------------------------------------------------------------------------------------------------
    
    
    // </editor-fold>
    
    // <editor-fold desc="init">
    //-----------------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logoview.setImage(logo1);
        fileslist.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        annotationlist.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        updateblastresultfilelist();
        updateannotationfilelist();
        fileslist.setItems(FXCollections.observableArrayList(filemanager.blastresultmanager.getFilenames()));
        updatecazyfilelist();
        updateuniprotfilelist();
        timeline.setCycleCount(Timeline.INDEFINITE);
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
            uniprotidfile = new File(myproperties.getProperty("idmappinglocation"));
            idmappinglabel.setText(uniprotidfile.getAbsolutePath());
        } catch (Exception ex) {       
        }
        //settings
        setdirectorylabel();
        //database manager
        dbmanagerinit();
        updatedbmanagertable();
        
    }    
    // </editor-fold>
}
