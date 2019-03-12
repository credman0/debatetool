package gui.cardediting;

import core.*;
import gui.SettingsHandler;
import gui.locationtree.LocationTreeItem;
import gui.locationtree.LocationTreeItemContent;
import gui.speechtools.SpeechComponentCellFactory;
import io.iocontrollers.IOController;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import scripting.JythonScripter;

import javax.swing.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class CardCreator{
    @FXML private Menu scriptsMenu;
    @FXML protected BorderPane viewerPane;
    @FXML protected Label currentPathLabel;
    @FXML protected TreeTableView directoryView;
    private LocationTreeItem currentNode;
    private StringProperty currentPathString = new SimpleStringProperty("");
    private ComponentViewer componentViewer;
    private LocationTreeItem openedNode;
    private SpeechComponent openedComponent;

    public LocationTreeItem getOpenedNode(){
        return openedNode;
    }
    public void init(){
        componentViewer = new ComponentViewer();
        componentViewer.init(viewerPane);
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
                    open(node.getValue().getSpeechComponent().clone());
                }
            }
        });
        currentPathLabel.textProperty().bind(currentPathString);
        viewerPane.setCenter(componentViewer.getPane());
        scriptsMenu.setOnShowing(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                refreshScripts();
            }
        });
    }

    private void open(HashIdentifiedSpeechComponent component){
        componentViewer.open(component);
        openedNode = currentNode;
        openedComponent = component;
    }

    private void populateDirectoryView(){
        List<String> rootFolders = IOController.getIoController().getStructureIOManager().getRoot();
        TreeItem<LocationTreeItemContent> root = new TreeItem<>();
        TreeTableColumn<LocationTreeItemContent, String> labelColumn = new TreeTableColumn<>("Name");
        TreeTableColumn<LocationTreeItemContent, Date> timeColumn = new TreeTableColumn<>("Date");
        directoryView.getColumns().setAll(labelColumn,timeColumn);
        labelColumn.setCellValueFactory(new TreeItemPropertyValueFactory("display"));

        // This anonymous class is pretty hideous but slightly inconvenient to put into its own class so I'm slgihtly sorry
        labelColumn.setCellFactory(new Callback<TreeTableColumn<LocationTreeItemContent, String>, TreeTableCell<LocationTreeItemContent, String>>() {
            private void setIcon (TreeTableCell cell, Image iconImage){
                ImageView icon = new ImageView(iconImage);
                icon.setFitWidth(24);
                icon.setFitHeight(24);
                cell.setGraphic(icon);
            }
            @Override
            public TreeTableCell<LocationTreeItemContent, String> call(TreeTableColumn<LocationTreeItemContent, String> locationTreeItemContentStringTreeTableColumn) {
                TreeTableCell<LocationTreeItemContent, String> cell = new TreeTableCell<>() {
                    {
                        setContextMenu(new ContextMenu());
                    }
                    @Override
                    protected void updateItem(String content, boolean empty){
                        super.updateItem(content, empty);
                        if(empty || content==null) {
                            setText(null);
                        } else {
                            setText(content);
                            LocationTreeItem item = (LocationTreeItem) getTreeTableRow().getTreeItem();
                            if (!item.isLeaf()) {
                                setIcon(this, LocationTreeItem.DIRECTORY_CLOSED);

                                // cannot use "this" inside the listener
                                TreeTableCell cell1 = this;
                                item.expandedProperty().addListener(new ChangeListener<Boolean>() {
                                    @Override
                                    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                                        if (t1){
                                            setIcon(cell1, LocationTreeItem.DIRECTORY_OPEN);
                                        }else{
                                            setIcon(cell1, LocationTreeItem.DIRECTORY_CLOSED);
                                        }
                                    }
                                });
                            }else{
                                if (item.getValue().getSpeechComponent().getClass().isAssignableFrom(Block.class)){
                                    setIcon(this, LocationTreeItem.LETTER_B);
                                }else if (item.getValue().getSpeechComponent().getClass().isAssignableFrom(Card.class)){
                                    setIcon(this, LocationTreeItem.LETTER_C);
                                }else if (item.getValue().getSpeechComponent().getClass().isAssignableFrom(Speech.class)){
                                    setIcon(this, LocationTreeItem.LETTER_S);
                                }
                            }
                        }

                    }
                };



                cell.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                    @Override
                    public void handle(ContextMenuEvent contextMenuEvent) {
                        ContextMenu localMenu = new ContextMenu();


                        MenuItem newDirectoryItem = new MenuItem("New Directory");
                        newDirectoryItem.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                List<String> effectivePath;
                                if (cell.isEmpty()){
                                    // if we are on an empty cell, create a top-level directory
                                    root.getChildren().add(new LocationTreeItem(new LocationTreeItemContent("New Directory")));
                                    effectivePath = new ArrayList<>();
                                }else{
                                    currentNode.getChildren().add(new LocationTreeItem(new LocationTreeItemContent("New Directory")));
                                    effectivePath = getCurrentNode().getPath();
                                }
                                IOController.getIoController().getStructureIOManager().addChild(effectivePath, "New Directory");
                                localMenu.hide();
                            }
                        });
                        localMenu.getItems().add(newDirectoryItem);

                        MenuItem newBlockItem = new MenuItem("New Block");
                        newBlockItem.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                Block newBlock = new Block(getCurrentNode().getPath(), "New Block");
                                currentNode.getChildren().add(new LocationTreeItem(new LocationTreeItemContent(newBlock)));
                                IOController.getIoController().getStructureIOManager().addContent(getCurrentNode().getPath(), newBlock.getHash());
                                try {
                                    IOController.getIoController().getComponentIOManager().storeSpeechComponent(newBlock);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                localMenu.hide();
                            }
                        });
                        localMenu.getItems().add(newBlockItem);

                        MenuItem newSpeechItem = new MenuItem("New Speech");
                        newSpeechItem.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                Speech newSpeech = new Speech(getCurrentNode().getPath(),"New Speech");
                                currentNode.getChildren().add(new LocationTreeItem(new LocationTreeItemContent(newSpeech)));
                                IOController.getIoController().getStructureIOManager().addContent(getCurrentNode().getPath(), newSpeech.getHash());
                                try {
                                    IOController.getIoController().getComponentIOManager().storeSpeechComponent(newSpeech);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                localMenu.hide();
                            }
                        });
                        localMenu.getItems().add(newSpeechItem);

                        if (!cell.isEmpty()){
                            if (cell.getTreeTableRow().getTreeItem().getValue().getSpeechComponent()==null) {
                                // Add directory actions only to directories
                                localMenu.getItems().add(new SeparatorMenuItem());

                                MenuItem changeDirectoryName = new MenuItem("Change Directory Name");
                                changeDirectoryName.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent actionEvent) {
                                        localMenu.hide();
                                        String name = JOptionPane.showInputDialog("Enter name");

                                        List<String> path = currentNode.getPath();
                                        path.remove(path.size() - 1);
                                        IOController.getIoController().getStructureIOManager().renameDirectory(path, getCurrentNode().getValue().getDisplay(), name);
                                        getCurrentNode().getValue().setDisplay(name);
                                        currentPathString.set(String.join("/", currentNode.getPath()));

                                    }
                                });
                                localMenu.getItems().add(changeDirectoryName);
                            }
                            // add block rename action only to block
                            if ((cell.getTreeTableRow().getTreeItem().getValue().getSpeechComponent() != null) && Block.class.isInstance(cell.getTreeTableRow().getTreeItem().getValue().getSpeechComponent())) {
                                localMenu.getItems().add(new SeparatorMenuItem());

                                MenuItem changeBlockName = new MenuItem("Rename Block");
                                changeBlockName.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent actionEvent) {
                                        localMenu.hide();
                                        Block cellBlock = (Block) cell.getTreeTableRow().getTreeItem().getValue().getSpeechComponent();
                                        String name = JOptionPane.showInputDialog("Enter name", cellBlock.getName());
                                        byte[] oldHash = cellBlock.getHash();
                                        cellBlock.setName(name);
                                        try {
                                            IOController.getIoController().getComponentIOManager().storeSpeechComponent(cellBlock);
                                            IOController.getIoController().getStructureIOManager().replaceContent(cellBlock.getPath(), oldHash, cellBlock.getHash());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        // force change the text of the cell
                                        cell.setText(name);
                                        componentViewer.open(cellBlock);
                                    }
                                });
                                localMenu.getItems().add(changeBlockName);
                            }
                        }
                        cell.setContextMenu(localMenu);
                        cell.getContextMenu().show(cell.getTreeTableView(),contextMenuEvent.getScreenX(),contextMenuEvent.getScreenY());
                    }
                });
                cell.setOnDragDetected(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        Dragboard db = cell.startDragAndDrop(TransferMode.COPY);

                        ClipboardContent content = new ClipboardContent();
                        content.put(SpeechComponentCellFactory.speechComponentFormat, cell.getTreeTableRow().getTreeItem().getValue().getSpeechComponent());
                        db.setContent(content);
                        mouseEvent.consume();
                    }
                });
                return cell;
            }
        });
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
                // always sort root, otherwise don't force children to load
                if (!(LocationTreeItem.class.isInstance(item)) || ((LocationTreeItem)item).isChildrenLoaded()) {
                    item.getChildren().sort(comparator);
                    ObservableList<TreeItem> children = item.getChildren();
                    for (TreeItem child : children) {
                        recurseSort(child, comparator);
                    }
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
    public void save() {
        if (currentNode == null){
            currentPathString.set("Please select a location");
            return;
        }
        componentViewer.save(currentNode);
        currentNode.reloadChildren();
        // TODO don't need to do a full reload here
        openedNode.reloadChildren();
    }

    public void newCardAction(){
        componentViewer.newCard();
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

    public void toggleEdit(ActionEvent actionEvent) {
        componentViewer.togglePanes();
    }

    public void showSettings(ActionEvent actionEvent) {
        SettingsHandler.showDialog();
    }

    public void refreshScripts() {
        String[] scripts = JythonScripter.getScripts();
        scriptsMenu.getItems().clear();
        for (String script:scripts){
            MenuItem item = new MenuItem(script);
            item.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    try {
                        JythonScripter.runScript(script, openedComponent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            scriptsMenu.getItems().add(item);
        }
    }
}
