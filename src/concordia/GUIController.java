/*
 * Tos change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import CazyModule.CazyAnnotator;
import FileManager.Filemanager;
import UniprotModule.UniprotAnnotator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;

/**
 *
 * @author emilvanamerongen
 */
public class GUIController implements Initializable {
    // files
    @FXML
    TabPane filespane;
    @FXML
    ListView fileslist;
    @FXML
    ListView annotationlist;
    @FXML
    Button directorybutton;
    @FXML
    Button blastbutton;
    @FXML
    Button annotationaddbutton;
    //annotation window
    @FXML
    CheckBox uniprotcheck;
    @FXML        
    CheckBox cazycheck;
    @FXML
    ListView uniprotlist;
    @FXML        
    ListView cazylist;
    @FXML
    ProgressBar mainprogressbar;
    @FXML
    Label progresslabel;
    @FXML
    Button activebutton1;
    @FXML
    Button activebutton2;
    @FXML
    Button activebutton3;
    @FXML
    Button activebutton4;
    @FXML
    Button activebutton5;
    @FXML
    Button activebutton6;
    
    Timeline timeline = new Timeline(Timeline.INDEFINITE, new KeyFrame(Duration.millis(1000), ae -> updateloop()));
    public HashMap<String, HashMap<String, Double>> proceslist = new HashMap<>();
    Boolean cazyqueue = false;
    ArrayList<File> cazyqueueitems;
    
    Properties myproperties = new Properties();
    Filemanager filemanager = new Filemanager();
    public static loadbar parserloadbar = new loadbar();
    private final Image IMAGE_empty  = new Image("file:src"+File.separator+"img"+File.separator+"empty.png");
    private final Image IMAGE_F  = new Image("file:src"+File.separator+"img"+File.separator+"filtered.png");
    private final Image IMAGE_C  = new Image("file:src"+File.separator+"img"+File.separator+"cazy.png");
    private final Image IMAGE_U  = new Image("file:src"+File.separator+"img"+File.separator+"uniprot.png");
    private final Image IMAGE_FC  = new Image("file:src"+File.separator+"img"+File.separator+"cazy+filtered.png");
    private final Image IMAGE_CU  = new Image("file:src"+File.separator+"img"+File.separator+"cazy+uniprot.png");
    private final Image IMAGE_FU  = new Image("file:src"+File.separator+"img"+File.separator+"uniprot+filtered.png");
    private final Image IMAGE_FUC  = new Image("file:src"+File.separator+"img"+File.separator+"cazy+uniprot+filtered.png");
    
    //gui manager
    @FXML
    private void switchtab(ActionEvent event){
        try {
        String source = event.getSource().toString();
        if (source.contains("annotate")){
            filespane.getSelectionModel().select(2);
        }
        else  if (source.contains("annotation")){
            filespane.getSelectionModel().select(3);
        }
        else if (source.contains("")){
            
        }
        else if (source.contains("")){
            
        } else{
            filespane.getSelectionModel().select(0);
        }
        } catch (Exception ex){
            filespane.getSelectionModel().select(0);
        }
    }  
    
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
    public void addreferencedatabasefiles(ActionEvent event){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("add BLAST database locations");
        ArrayList<File> newfiles = (ArrayList<File>) chooser.showOpenMultipleDialog(filespane.getScene().getWindow());
        if (newfiles != null){
            filemanager.addreferencedatabaselocations(newfiles);
        }
    }
    // annotation window
    @FXML
    public void annotate(ActionEvent event){
        ArrayList<File> selectedfiles = new ArrayList<File>();
        ObservableList selecteditems;
        if (event.getSource().toString().contains("GO")){
        selecteditems = fileslist.getSelectionModel().getSelectedItems();
        for (Object item : selecteditems){
            selectedfiles.add(filemanager.blastresultmanager.getblastresultfiles().get(item));
        }
        } else {
            selecteditems = annotationlist.getSelectionModel().getSelectedItems();
            for (Object item : selecteditems){
            selectedfiles.add(filemanager.annotationmanager.getAnnotationfiles().get(item));
        }
        }
        
        if (uniprotcheck.isSelected()){
            parserloadbar.setdone(0);
            parserloadbar.settotal(0);
            UniprotAnnotator uniprotannotator = new UniprotAnnotator(selectedfiles,filemanager.uniprotsources,filemanager.annotationmanager.getAnnotationdirectory());
            uniprotannotator.annotate();
            timeline.play();
        }
        if (cazycheck.isSelected()){
            cazyqueueitems = selectedfiles;
            cazyqueue = true;
        }
        updateannotationfilelist();
    }
    
    public void updateloop(){
        if (parserloadbar.gettotal() != 0){
            progresslabel.setText(parserloadbar.getcurrentprocess());
            double total = Double.parseDouble(Integer.toString(parserloadbar.gettotal()));
            double done = Double.parseDouble(Integer.toString(parserloadbar.getdone()));
            try{            
            double progresspercent = done/total;
            mainprogressbar.setProgress(progresspercent);
            if (progresspercent == 1.0){
                parserloadbar.setdone(0);
                parserloadbar.settotal(0);
                if (cazyqueue){
                    CazyAnnotator cazyannotator = new CazyAnnotator(cazyqueueitems,filemanager.cazysources,filemanager.annotationmanager.getAnnotationdirectory());
                    cazyannotator.annotate();
                    cazyqueue = false;
                } else {
                    timeline.stop();
                    System.out.println("pipeline complete");
                    parserloadbar.setcurrentprocess("");
                    filespane.getSelectionModel().select(0);
                }
            }
            } catch (Exception ex){}    
        }
    }
    
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
    
    public void updatecazyfilelist(){
        ArrayList<String> names = new ArrayList<>();
        for (File file : filemanager.cazysources){
            names.add(file.getName());
        }
        cazylist.setItems(FXCollections.observableArrayList(names));
    }
    
    public void updateuniprotfilelist(){
        ArrayList<String> names = new ArrayList<>();
        for (File file : filemanager.uniprotsources){
            names.add(file.getName());
        }
        uniprotlist.setItems(FXCollections.observableArrayList(names));
    }
    
    //filter
    
    
    //init
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fileslist.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        annotationlist.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        updateblastresultfilelist();
        updateannotationfilelist();
        fileslist.setItems(FXCollections.observableArrayList(filemanager.blastresultmanager.getFilenames()));
        updatecazyfilelist();
        updateuniprotfilelist();
        timeline.setCycleCount(Timeline.INDEFINITE);
    }    
}
