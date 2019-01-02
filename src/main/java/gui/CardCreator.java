package gui;

import core.Card;
import core.Main;
import gui.locationtree.LocationTreeItem;
import gui.locationtree.LocationTreeItemContent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class CardCreator{
    @FXML protected BorderPane viewerPane;
    @FXML protected Label currentPathLabel;
    @FXML protected TreeTableView directoryView;
    private LocationTreeItem currentNode;
    private StringProperty currentPathString = new SimpleStringProperty("");
    private CardEditor cardEditor;
    private CardCutter cardCutter;
    private CardViewer cardViewer;
    private boolean editMode;
    public void init(){
        populateDirectoryView();
        // Selection listener for tracking current node
        directoryView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldV, newV) -> {
            LocationTreeItem newItem = ((LocationTreeItem)newV);
            if (newItem==null){
                return;
            }
            if (newItem.isLeaf()){
                setCurrentNode((LocationTreeItem) newItem.getParent());
            }else{
                setCurrentNode(newItem);
            }
        });
        // double click listener for opening cards
        directoryView.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getClickCount() == 2) {
                LocationTreeItem node = (LocationTreeItem) directoryView.getSelectionModel().getSelectedItem();
                if (node != null && node.isLeaf()){
                    cardViewer.open((Card) node.getValue().getSpeechComponent());
                }
            }
        });
        currentPathLabel.textProperty().bind(currentPathString);

        // load the card viewers
        try {
            FXMLLoader editorLoader = new FXMLLoader(getClass().getClassLoader().getResource("card_editor.fxml"));
            editorLoader.load();
            cardEditor = editorLoader.getController();
            cardEditor.init();

            FXMLLoader cutterLoader = new FXMLLoader(getClass().getClassLoader().getResource("card_cutter.fxml"));
            cutterLoader.load();
            cardCutter = cutterLoader.getController();
            cardCutter.init();

            cardViewer = cardEditor;
            viewerPane.setCenter(cardViewer.getPane());
            editMode = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateDirectoryView(){
        List<String> rootFolders = Main.getIoController().getStructureIOManager().getRoot();
        TreeItem<LocationTreeItemContent> root = new TreeItem<>();
        TreeTableColumn<LocationTreeItemContent, String> labelColumn = new TreeTableColumn<>("Name");
        TreeTableColumn<LocationTreeItemContent, Date> timeColumn = new TreeTableColumn<>("Date");
        directoryView.getColumns().setAll(labelColumn,timeColumn);
        labelColumn.setCellValueFactory(new TreeItemPropertyValueFactory("display"));
        timeColumn.setCellFactory(column -> {
            TreeTableCell<LocationTreeItemContent, Date> cell = new TreeTableCell<>() {
                private SimpleDateFormat format = new SimpleDateFormat("MMM dd h:mm a ''yy");
                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if(empty || item==null || item.getTime()<=0) {
                        setText(null);
                    } else {
                        setText(format.format(item));
                    }
                }
            };

            return cell;
        });
        timeColumn.setCellValueFactory(new TreeItemPropertyValueFactory("date"));

        for (String name:rootFolders){
            root.getChildren().add(new LocationTreeItem(new LocationTreeItemContent(name)));
        }
        directoryView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        directoryView.setShowRoot(false);
        directoryView.setRoot(root);
        directoryView.setSortPolicy(new Callback<TreeTableView<LocationTreeItemContent>, Boolean>() {
            @Override
            public Boolean call(TreeTableView treeTableView) {
                Comparator<TreeItem> comparator = new Comparator<TreeItem>(){

                    @Override
                    public int compare(TreeItem first, TreeItem second) {
                        Comparator<TreeItem> treeComparator = directoryView.getComparator();
                        if (treeComparator==null){
                            return 0;
                        }
                        boolean firstIsDir = first.isLeaf();
                        boolean secondIsDir = second.isLeaf();
                        if (firstIsDir && !secondIsDir){
                            return 1;
                        }
                        if (secondIsDir && !firstIsDir){
                            return -1;
                        }
                        return treeComparator.compare(first,second);
                    }
                };
                recurseSort(directoryView.getRoot(), comparator);
                return true;

            }
            private void recurseSort(TreeItem item, Comparator<TreeItem> comparator){
                item.getChildren().sort(comparator);
                ObservableList<TreeItem> children = item.getChildren();
                for (TreeItem child:children) {
                    recurseSort(child, comparator);
                }
            }
        });
        directoryView.treeColumnProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object t1) {
                directoryView.sort();
            }
        });
    }

    @FXML
    public void saveCard() {
        if (currentNode == null){
            currentPathString.set("Please select a location");
            return;
        }
        if (editMode) {
            cardEditor.save(currentNode.getPath());
        }else {
            cardCutter.save(currentNode.getPath());
        }
        // TODO don't need to do a full reload here
        currentNode.reloadChildren();
    }

    public void newCardAction(){
        cardViewer.clear();
    }
    public String getCurrentPathString() {
        return currentPathString.get();
    }

    public StringProperty currentPathStringProperty() {
        return currentPathString;
    }

    public LocationTreeItem getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(LocationTreeItem currentNode) {
        this.currentNode = currentNode;
        this.currentPathString.set(String.join("/",currentNode.getPath()));
    }

    public void exit() throws IOException {
        // need some handle to the stage, so the viewerPane chosen arbitrarily
        ((Stage)viewerPane.getScene().getWindow()).close();
    }

    public void togglePanes() {
        if (editMode){
            cardEditor.swapTo(cardCutter);
            cardViewer = cardCutter;
            viewerPane.setCenter(cardCutter.getPane());
            editMode = false;
        }else{
            cardCutter.swapTo(cardEditor);
            cardViewer = cardEditor;
            viewerPane.setCenter(cardEditor.getPane());
            editMode = true;
        }
    }
}
