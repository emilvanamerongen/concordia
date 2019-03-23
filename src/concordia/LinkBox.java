/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concordia;

import Refdbmanager.header;
import Refdbmanager.refdb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import static javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Callback;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;

/**
 *
 * @author emil3
 */
public class LinkBox extends VBox{
    private Label boxlabel = new Label();
    private ListView boxlistview = new ListView();
    private ScrollPane scrollpane = new ScrollPane();
    private String dbname;       
    private LinkedHashSet<header> headers = new LinkedHashSet<>();
    
    
    public LinkBox(String dbname, LinkedHashSet<header> headers){
        this.dbname = dbname;
        this.alignmentProperty().set(Pos.CENTER);
        this.setFillWidth(true);
        this.setMinWidth(230);
        this.setPadding(new Insets(0,0,20,0));
        this.headers = headers;
        boxlabel.setText(dbname);
        boxlabel.maxWidth(800.0);
        boxlabel.setFont(Font.font ("Verdana", 20));
        boxlabel.prefWidth(800.0);
        boxlabel.alignmentProperty().set(Pos.CENTER);
        ObservableList<header> headerlist = FXCollections.observableArrayList();    
        
        for (header myheader : headers){
            myheader.setSourcedb(dbname);
            headerlist.add(myheader);
        }
        
        //boxlistview.maxHeight(10000.0);
        //boxlistview.maxHeightProperty().set(10000.0);
        //boxlistview.prefHeightProperty().set(headers.size()*35);
        boxlistview.setItems(headerlist);  

        scrollpane.setMaxWidth(10000.0);
        scrollpane.setHbarPolicy(NEVER);
        scrollpane.setFitToWidth(true);
        scrollpane.setFitToHeight(true);
        VBox.setVgrow(scrollpane, Priority.ALWAYS);

        boxlistview.setCellFactory(new Callback<ListView<String>, ListCell<header>>() {
            @Override
            public ListCell<header> call(ListView<String> param) {
                return new XCell();
            }
        });
        boxlistview.setOnMouseClicked(new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            header selecteditem = (header) boxlistview.getSelectionModel().getSelectedItem();
            if (event.getButton() == MouseButton.SECONDARY) {
                    String selectedname = selecteditem.getHeaderstring();
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Examples:");
                    alert.setHeaderText(selectedname);
                    String contenttext = "";
                    for (header myheader : headers){
                        if (myheader.getHeaderstring().equals(selectedname)){
                            for (String example : myheader.getExamples()){
                                contenttext += example+"\n";
                            }
                            alert.setContentText(contenttext);
                            CheckBox removeversioncheckbox = new CheckBox();
                            removeversioncheckbox.setText("Remove version (.1 , .2 etc.)");
                            if (myheader.getParameters().contains("removeversion")){
                                removeversioncheckbox.setSelected(true);
                            }
                            
                            CheckBox trimcheckbox = new CheckBox();
                            trimcheckbox.setText("trim");
                            if (myheader.getParameters().contains("trimcheckbox")){
                                trimcheckbox.setSelected(true);
                            }
                            
                            
                            GridPane expContent = new GridPane();
                            expContent.setMaxWidth(Double.MAX_VALUE);
                            Label explabel = new Label();
                            explabel.setText("Indexing options:");
                            //expContent.add(explabel, 0, 0);
                            //expContent.add(removeversioncheckbox, 0, 1);
                            //expContent.add(trimcheckbox, 0, 2);
                            
                            alert.getDialogPane().setExpandableContent(expContent);
                            alert.showAndWait();
                            
                            if (removeversioncheckbox.isSelected()){
                                myheader.getParameters().add("removeversion");
                            } else {
                                myheader.getParameters().remove("removeversion");
                            }
                            
                        }
                    }
                    for (refdb db : GUIController.dbmanager.getReferencedatabases()){
                        if (db.getDbname().equals(dbname)){
                            db.headerupdate();
                        }
                    }
                    
                    
            } 
            
        }
        
        
    });
        
        this.getChildren().add(boxlabel);
        this.getChildren().add(scrollpane);
        scrollpane.setContent(boxlistview);
        
    }

    /**
     * @return the dbname
     */
    public String getDbname() {
        return dbname;
    }

    /**
     * @param dbname the dbname to set
     */
    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    class XCell extends ListCell<header> {
        HBox hbox = new HBox();
        Label label = new Label("(empty)");
        Pane pane = new Pane();
        Button button = new Button("   ");
        CheckBox checkbox = new CheckBox();
        String lastItem;
        ArrayList<String> colors = new ArrayList<>();
        private SimpleStringProperty dbname = new SimpleStringProperty();
        int activecolor = 0;
        Boolean selected = true;
        
        public XCell() {
            super();
            colors.add("#A4A4A4");
            colors.add("#FF0000");
            colors.add("#FE9A2E");
            colors.add("#F7FE2E");
            colors.add("#2EFE2E");
            colors.add("#2EFEF7");
            colors.add("#2E2EFE");
            colors.add("#CC2EFA");
            hbox.getChildren().addAll(checkbox, label, pane, button);
            HBox.setHgrow(pane, Priority.ALWAYS);
            label.setMaxWidth(150);
            hbox.setStyle("-fx-background: #96ff70;");
            
            
            checkbox.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("GO "+dbname.getValue());
                
                for (refdb db : GUIController.dbmanager.getReferencedatabases()){
                if (db.getDbname().equals(dbname.getValue())){
                    System.out.println(db.getDbname());
                    for (header myheader : headers){
                        if (myheader.getHeaderstring().equals(label.getText())){
                            myheader.toggleEnabled();
                        }
                    }
                    db.headerupdate();
                }
            }
                    }
            });
            
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String newcolor = "";
                    for (refdb db : GUIController.dbmanager.getReferencedatabases()){
                        if (db.getDbname().equals(dbname.getValue())){
                            for (header myheader : headers){
                                if (myheader.getHeaderstring().equals(label.getText())){
                                    Integer cyclecolor = myheader.cyclecolor();
                                    button.setStyle("-fx-background-color: "+colors.get(myheader.getColorindex()));
                                    button.setVisible(myheader.getIndexable());
                        }
                    }
                        db.headerupdate();}}
                        
                }

            });


        }
        public void setdbname(String dbname){
            this.dbname.set(dbname);
        }

        @Override
        protected void updateItem(header item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);  // No text in label of super class
            if (empty) {
                lastItem = null;
                setGraphic(null);
            } else {
                lastItem = item.getHeaderstring();
                label.setText(item!=null ? item.getHeaderstring() : "<null>");
                setGraphic(hbox);
                checkbox.setSelected(item.getEnabled());
                dbname.set(item.getSourcedb());
                button.setStyle("-fx-background-color: "+colors.get(item.getColorindex()));
                button.setVisible(item.getIndexable());
                item.checkifindexed();
                if (item.getIndexed()){
                    hbox.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                } else {
                    hbox.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
                }
            }
        }
    }}
