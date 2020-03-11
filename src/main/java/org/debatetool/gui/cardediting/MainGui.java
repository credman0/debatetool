/*
 *                               This program is free software: you can redistribute it and/or modify
 *                                it under the terms of the GNU General Public License as published by
 *                                the Free Software Foundation, version 3 of the License.
 *
 *                                This program is distributed in the hope that it will be useful,
 *                                but WITHOUT ANY WARRANTY; without even the implied warranty of
 *                                MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *                                GNU General Public License for more details.
 *
 *                                You should have received a copy of the GNU General Public License
 *                                along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *                                Copyright (c) 2019 Colin Redman
 */

package org.debatetool.gui.cardediting;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.debatetool.core.*;
import org.debatetool.gui.LoginDialog;
import org.debatetool.gui.SettingsHandler;
import org.debatetool.gui.locationtree.LocationTreeItem;
import org.debatetool.gui.locationtree.LocationTreeItemContent;
import org.debatetool.gui.speechtools.FullscreenView;
import org.debatetool.gui.speechtools.SpeechComponentCellFactory;
import org.debatetool.gui.timer.DebateTimer;
import org.debatetool.io.accounts.DBLockResponse;
import org.debatetool.io.filesystemio.FileSystemIOController;
import org.debatetool.io.filters.Filter;
import org.debatetool.io.initializers.DatabaseInitializer;
import org.debatetool.io.initializers.IOInitializer;
import org.debatetool.io.iocontrollers.IOController;
import org.debatetool.io.iocontrollers.mongodb.MongoDBIOController;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class MainGui {
    @FXML private MenuItem showFullscreenMenuItem;
    @FXML private MenuItem exportDocxMenuItem;
    @FXML private Button timerButton;
    @FXML private MenuItem authAdminMenuItem;
    @FXML private MenuItem createUserMenuItem;
    @FXML private ToggleButton editToggle;
    @FXML private Text viewerLabel;
    @FXML private Button backButton;
    @FXML private Button forwardButton;
    @FXML private ListView <String> filterChipView;
    @FXML private Menu scriptsMenu;
    @FXML private BorderPane viewerPane;
    @FXML private Label currentPathLabel;
    @FXML private TreeTableView directoryView;
    private LocationTreeItem currentNode;
    private StringProperty currentPathString = new SimpleStringProperty("");
    private ComponentViewer componentViewer;
    private LocationTreeItem openedNode;
    private SpeechComponent openedComponent;
    private ObservableList<HashIdentifiedSpeechComponent> editHistory = FXCollections.observableArrayList();
    private SimpleIntegerProperty editHistoryIndex = new SimpleIntegerProperty(this, "editHistoryIndex", -1);
    private SimpleObjectProperty<DebateTimer> timerProperty = new SimpleObjectProperty<>(null);

    private static MainGui activeGUI;

    public static MainGui getActiveGUI() {
        return activeGUI;
    }

    public static boolean listContainsString(List<TreeItem<LocationTreeItemContent>> list, String name){
        for (TreeItem<LocationTreeItemContent> treeItem: list){
            if (name.equals(treeItem.getValue().toString())){
                return true;
            }
        }
        return false;
    }

    public void refreshDirectories(){
        ((LocationTreeItem)directoryView.getRoot()).reloadChildrenRecursive();
    }

    public void refreshViewer(){
        componentViewer.refresh();
    }

    public boolean treeContainsPath(List<String> path){
        return ((LocationTreeItem)directoryView.getRoot()).contains(path);
    }

    public Scene getScene(){
        // viewer pane chosen arbitrarily
        return viewerPane.getScene();
    }

    public LocationTreeItem getOpenedNode(){
        return openedNode;
    }

    public void init(){
        backButton.disableProperty().bind(Bindings.lessThanOrEqual(editHistoryIndex,0));
        forwardButton.disableProperty().bind(Bindings.greaterThanOrEqual(editHistoryIndex,Bindings.subtract(Bindings.size(editHistory),1)));
        activeGUI = this;
        try {
            attemptLogin();
        } catch (IOException e) {
            e.printStackTrace();
        }
        populateDirectoryView();
        componentViewer = new ComponentViewer();
        componentViewer.init(viewerPane);
        // Selection listener for tracking current node
        directoryView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldV, newV) -> {
            if (newV==null){
                return;
            }
            LocationTreeItem newItem = ((LocationTreeItem)newV);
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
                    open(node.getValue().getSpeechComponent());
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

//        filterChipView.getChips().addListener((ListChangeListener<String>) change ->{
//            while (change.next()){
//                if(change.wasAdded()) {
//                    Filter.addParsed(change.getAddedSubList().get(0), change.getFrom());
//                    refreshDirectories();
//                }else if (change.wasRemoved()){
//                    Filter.removedParsed(change.getFrom());
//                    refreshDirectories();
//                }
//            }
//        });

        componentViewer.bindEditMode(editToggle.selectedProperty());
        componentViewer.bindPreventContainerActions(exportDocxMenuItem.disableProperty());
        componentViewer.bindPreventContainerActions(showFullscreenMenuItem.disableProperty());
        editToggle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                componentViewer.updateEdit();
            }
        });

        timerButton.disableProperty().bind(timerProperty.isNotNull());
    }

    private void attemptLogin() throws IOException {
        IOInitializer initializer = LoginDialog.showDialog();
        if (initializer == null){
            System.exit(0);
        }
        if (initializer instanceof DatabaseInitializer){
            IOController.setIoController(new MongoDBIOController());
            boolean success = IOController.getIoController().attemptInitialize(initializer);
            if (!success){
                new Alert(Alert.AlertType.ERROR, "Authentication failed! Either the software cannot connect to the database, or the username or password were incorrect.", ButtonType.OK).showAndWait();
                attemptLogin();
            }
        }else{
            IOController.setIoController(new FileSystemIOController());
            IOController.getIoController().attemptInitialize(initializer);
        }
    }

    private void open(HashIdentifiedSpeechComponent component, boolean track){
        DBLockResponse response = IOController.getIoController().getDBLock().tryLock(component.getHash());
        if (response.getResultType()!=DBLockResponse.ResultType.SUCCESS){
            Alert alert = new Alert(Alert.AlertType.ERROR, "That file is already being edited by " + response.getMessage()+". If you think that this message is a mistake, the lock should time out (default - 10 minutes).");
            alert.showAndWait();
            return;
        }
        // TODO change this to onyl the currnet document
        IOController.getIoController().getDBLock().unlockAllExcept(component.getHash());

        componentViewer.open(component);
        openedNode = currentNode;
        openedComponent = component;
        viewerLabel.setText(component.getLabel());
        if (track) {
            if (editHistoryIndex.get() < editHistory.size() - 1) {
                editHistory.remove(editHistoryIndex.get(), editHistory.size() - 1);
            }
            editHistoryIndex.set(editHistoryIndex.get()+1);
            editHistory.add(component);
        }
    }
    private void open(HashIdentifiedSpeechComponent component){
        open(component,true);
    }

    private void populateDirectoryView(){
        List<String> rootFolders = IOController.getIoController().getStructureIOManager().getRoot();
        LocationTreeItem root = new LocationTreeItem(null){
            public boolean isLeaf(){
                return false;
            }
        };
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
                    LocationTreeItem previousItem = null;
                    ChangeListener<Boolean> previousExpandedListener =  null;
                    ChangeListener<Boolean> previousLoadingListener =  null;
                    @Override
                    protected void updateItem(String content, boolean empty){
                        super.updateItem(content, empty);
                        if(empty || content==null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            if (getTreeTableRow().getTreeItem()==null || getTreeTableRow().getTreeItem().getValue() == null){
                                // there is an empty cell added if the root is otherwise empty -- ignore it
                                return;
                            }
                            if (previousExpandedListener !=null && previousItem!=null) {
                                previousItem.expandedProperty().removeListener(previousExpandedListener);
                                previousItem.removeLoadingListener(previousLoadingListener);
                                previousExpandedListener = null;
                                previousLoadingListener = null;
                            }
                            LocationTreeItem item = (LocationTreeItem) getTreeTableRow().getTreeItem();
                            previousItem = item;
                            setText(content);
                            if (!item.isLeaf()) {
                                if (item.isExpanded()) {
                                    setIcon(this, LocationTreeItem.DIRECTORY_OPEN);
                                }else{
                                    setIcon(this, LocationTreeItem.DIRECTORY_CLOSED);
                                }

                                // cannot use "this" inside the listener
                                TreeTableCell cell1 = this;
                                previousExpandedListener = (observableValue, aBoolean, t1) -> {
                                    if (t1){
                                        setIcon(cell1, LocationTreeItem.DIRECTORY_OPEN);
                                    }else{
                                        setIcon(cell1, LocationTreeItem.DIRECTORY_CLOSED);
                                    }
                                };
                                item.expandedProperty().addListener(previousExpandedListener);

                                previousLoadingListener = new ChangeListener<Boolean>() {
                                    @Override
                                    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                                        if (t1){
                                            cell1.setGraphic(new Spinner<>());
                                        }else{
                                            if (item.isExpanded()) {
                                                setIcon(cell1, LocationTreeItem.DIRECTORY_OPEN);
                                            }else{
                                                setIcon(cell1, LocationTreeItem.DIRECTORY_CLOSED);
                                            }
                                        }
                                    }
                                };
                                item.addLoadingListener(previousLoadingListener);
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
                                localMenu.hide();
                                List<String> effectivePath;
                                Optional<String> result = showTextDialog("Directory Name", "Enter a new directory name.", "New Directory");
                                String name = result.isPresent()? result.get() : null;
                                if (name == null){
                                    return;
                                }
                                // if the list was "empty", it will have one null element we want to remove
                                currentNode.getChildren().removeIf(
                                        locationTreeItemContentTreeItem -> locationTreeItemContentTreeItem.getValue()==null);
                                if (cell.isEmpty()){

                                    // if we are on an empty cell, create a top-level directory
                                    root.getChildren().add(new LocationTreeItem(new LocationTreeItemContent(name)));
                                    effectivePath = new ArrayList<>();
                                }else{;
                                    currentNode.getChildren().add(new LocationTreeItem(new LocationTreeItemContent(name)));
                                    effectivePath = getCurrentNode().getPath();
                                }
                                IOController.getIoController().getStructureIOManager().addChild(effectivePath, name);
                            }
                        });
                        localMenu.getItems().add(newDirectoryItem);

                        MenuItem newBlockItem = new MenuItem("New Block");
                        newBlockItem.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                localMenu.hide();
                                Optional<String> result = showTextDialog("Block Name", "Enter a new block name.", "New Block");
                                String name = result.isPresent()? result.get() : null;
                                if (name == null){
                                    return;
                                }
                                // if the list was "empty", it will have one null element we want to remove
                                currentNode.getChildren().removeIf(
                                        locationTreeItemContentTreeItem -> locationTreeItemContentTreeItem.getValue()==null);
                                Block newBlock = new Block(name);
                                currentNode.getChildren().add(new LocationTreeItem(new LocationTreeItemContent(newBlock)));
                                try {
                                    IOController.getIoController().getStructureIOManager().addContent(getCurrentNode().getPath(), newBlock);
                                    IOController.getIoController().getComponentIOManager().storeSpeechComponent(newBlock);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        localMenu.getItems().add(newBlockItem);

                        MenuItem newSpeechItem = new MenuItem("New Speech");
                        newSpeechItem.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                localMenu.hide();
                                Optional<String> result = showTextDialog("Speech Name", "Enter a new speech name.", "New Speech");
                                String name = result.isPresent()? result.get() : null;
                                if (name == null){
                                    return;
                                }
                                // if the list was "empty", it will have one null element we want to remove
                                currentNode.getChildren().removeIf(
                                        locationTreeItemContentTreeItem -> locationTreeItemContentTreeItem.getValue()==null);
                                Speech newSpeech = new Speech(name);
                                currentNode.getChildren().add(new LocationTreeItem(new LocationTreeItemContent(newSpeech)));
                                try {
                                    IOController.getIoController().getStructureIOManager().addContent(getCurrentNode().getPath(), newSpeech);
                                    IOController.getIoController().getComponentIOManager().storeSpeechComponent(newSpeech);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        localMenu.getItems().add(newSpeechItem);
                        localMenu.getItems().add(new SeparatorMenuItem());
                        if (!cell.isEmpty() && cell.getTreeTableRow().getTreeItem().getValue()!=null){
                            if (cell.getTreeTableRow().getTreeItem().getValue().getSpeechComponent()==null) {
                                MenuItem removeItem = new MenuItem("Delete");
                                removeItem.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent actionEvent) {
                                        Task task = new Task<>() {
                                            @Override
                                            protected Object call() throws Exception {
                                                IOController.getIoController().getStructureIOManager().removeNode(currentNode.getPath());
                                                return null;
                                            }
                                        };
                                        new Thread(task).start();
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                localMenu.hide();
                                                // force update current tree
                                                cell.getTreeTableRow().getTreeItem().getParent().getChildren().remove(cell.getTreeTableRow().getTreeItem());
                                            }
                                        });
                                    }
                                });
                                localMenu.getItems().add(removeItem);

                                MenuItem changeDirectoryName = new MenuItem("Change Directory Name");
                                changeDirectoryName.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent actionEvent) {
                                        localMenu.hide();
                                        Optional<String> result = showTextDialog("Directory Rename", "Enter a new name for the directory.", getCurrentNode().getValue().getDisplay());
                                        String name = result.isPresent()? result.get() : null;
                                        if (name == null){
                                            return;
                                        }

                                        List<String> path = currentNode.getPath();
                                        path.remove(path.size() - 1);
                                        IOController.getIoController().getStructureIOManager().renameDirectory(path, getCurrentNode().getValue().getDisplay(), name);
                                        getCurrentNode().getValue().setDisplay(name);
                                        currentPathString.set(String.join("/", currentNode.getPath()));

                                    }
                                });
                                localMenu.getItems().add(changeDirectoryName);
                            }else{
                                MenuItem removeItem = new MenuItem("Remove");
                                removeItem.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent actionEvent) {
                                        localMenu.hide();
                                        IOController.getIoController().getStructureIOManager().removeContent(currentNode.getPath(), cell.getTreeTableRow().getTreeItem().getValue().getSpeechComponent().getHash());
                                        // force update current tree
                                        cell.getTreeTableRow().getTreeItem().getParent().getChildren().remove(cell.getTreeTableRow().getTreeItem());
                                    }
                                });
                                localMenu.getItems().add(removeItem);
                            }
                            // add block rename action only to block
                            if ((cell.getTreeTableRow().getTreeItem().getValue().getSpeechComponent() != null) && Block.class.isInstance(cell.getTreeTableRow().getTreeItem().getValue().getSpeechComponent())) {
                                MenuItem changeBlockName = new MenuItem("Rename Block");
                                changeBlockName.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent actionEvent) {
                                        localMenu.hide();
                                        Block cellBlock = (Block) cell.getTreeTableRow().getTreeItem().getValue().getSpeechComponent();
                                        String name;
                                        Optional<String> baseNameResult = showTextDialog("Block Rename","Enter a new name for the block.", cellBlock.getName());
                                        if (!baseNameResult.isPresent()){
                                            return;
                                        }else{
                                            name = baseNameResult.get();
                                        }
                                        byte[] oldHash = cellBlock.getHash();
                                        cellBlock.setName(name);
                                        try {
                                            IOController.getIoController().getComponentIOManager().storeSpeechComponent(cellBlock);
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
                            // add speech rename action only to speech
                            if ((cell.getTreeTableRow().getTreeItem().getValue().getSpeechComponent() != null) && Speech.class.isInstance(cell.getTreeTableRow().getTreeItem().getValue().getSpeechComponent())) {
                                MenuItem changeSpeechName = new MenuItem("Rename Speech");
                                changeSpeechName.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent actionEvent) {
                                        localMenu.hide();
                                        Speech cellSpeech = (Speech) cell.getTreeTableRow().getTreeItem().getValue().getSpeechComponent();
                                        String name;
                                        Optional<String> baseNameResult = showTextDialog("Speech Rename","Enter a new name for the speech.", cellSpeech.getName());
                                        if (!baseNameResult.isPresent()){
                                            return;
                                        }else{
                                            name = baseNameResult.get();
                                        }
                                        cellSpeech.setName(name);
                                        try {
                                            IOController.getIoController().getComponentIOManager().storeSpeechComponent(cellSpeech);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        // force change the text of the cell
                                        cell.setText(name);
                                        componentViewer.open(cellSpeech);
                                    }
                                });
                                localMenu.getItems().add(changeSpeechName);

                            }
                        }
                        localMenu.setAutoHide(true);
                        cell.setContextMenu(localMenu);
                        cell.getContextMenu().show(getScene().getWindow(),contextMenuEvent.getScreenX(),contextMenuEvent.getScreenY());
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
        // we have to add something, or javafx adds a placeholder
        if (root.getChildren().isEmpty()){
            root.getChildren().add(new LocationTreeItem(null){
                public boolean isLeaf(){
                    return true;
                }
            });
        }
        currentNode = root;
    }

    @FXML
    public void save() {
        if (currentNode == null){
            currentPathString.set("Please select a location");
            return;
        }
        componentViewer.save(currentNode.getPath());
        currentNode.reloadChildren();
        // TODO don't need to do a full reload here
        if (openedNode!=null){
            openedNode.reloadChildren();
        }
    }

    public void newCardAction(){
        viewerLabel.setText("");
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
        IOController.getIoController().close();
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
//        String[] scripts = JythonScripter.getScripts();
//        if (scripts == null){
//            return;
//        }
//        scriptsMenu.getItems().clear();
//        for (String script:scripts){
//            MenuItem item = new MenuItem(script);
//            item.setOnAction(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent actionEvent) {
//                    try {
//                        scriptsMenu.hide();
//                        JythonScripter.runScript(script, openedComponent);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//            scriptsMenu.getItems().add(item);
//        }
    }

    public void historyBack(ActionEvent actionEvent) {
        editHistoryIndex.set(editHistoryIndex.get()-1);
        open(editHistory.get(editHistoryIndex.get()),false);
    }

    public void historyForward(ActionEvent actionEvent) {
        editHistoryIndex.set(editHistoryIndex.get()+1);
        open(editHistory.get(editHistoryIndex.get()),false);
    }

    public void adminAuthenticate() {
        // TODO Change this not to use the same login dialog
        DatabaseInitializer initializer = (DatabaseInitializer) LoginDialog.showDialog();
        if (initializer == null){
            return;
        }
        boolean success = IOController.getIoController().getAdminManager().authenticateAsAdmin(SettingsHandler.getSetting("mongod_ip"), Integer.parseInt(SettingsHandler.getSetting("mongod_port")), initializer.username, initializer.password);
        if (success){
            authAdminMenuItem.setDisable(true);
            createUserMenuItem.setDisable(false);
        }else{
            new Alert(Alert.AlertType.ERROR, "Authentication failed!", ButtonType.OK).showAndWait();
        }
    }

    public void adminCreateUser(ActionEvent actionEvent) {
        String username;
        String password;

        TextInputDialog dialog = new TextInputDialog("user");
        dialog.setTitle("Username selection");
        dialog.setHeaderText("Please select a username");
        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent()){
            return;
        }
        username = result.get();
        dialog = new TextInputDialog("");
        dialog.setTitle("Password selection");
        dialog.setHeaderText("Please select a password");
        result = dialog.showAndWait();
        if (!result.isPresent() || result.get().equals("")) {
            return;
        }
        password = result.get();
        boolean success = IOController.getIoController().getAdminManager().createUser(username,password);
    }

    public static Optional<String> showTextDialog(String title, String header, String defaultText){
        TextInputDialog dialog = new TextInputDialog(defaultText);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        return dialog.showAndWait();
    }

    private void openWebpage(String url){
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public void openWiki(ActionEvent actionEvent) {
        openWebpage("https://github.com/credman0/unnamed-debate-tool/wiki");
    }

    public void openGithub(ActionEvent actionEvent) {
        openWebpage("https://github.com/credman0/unnamed-debate-tool/");
    }

    public void openIssues(ActionEvent actionEvent) {
        openWebpage("https://github.com/credman0/unnamed-debate-tool/issues");
    }

    public void docxExport(ActionEvent actionEvent) {
        try {
            componentViewer.exportToDOCX();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Find a name that is not in the list, by taking base and adding numbers until it is not contained
     * @param base base name to append to
     * @param list list to check against
     * @return a name of the form <base>(\d)?
     */
    public static String getSafeNameAgainstList(String base, List<String> list){
        String trialName = base;
        int index = 1;
        while (list.contains(trialName)){
            trialName = base + " (" + index +")";
            index++;
        }
        return trialName;
    }

    public void spawnTimer(ActionEvent actionEvent) {
        try {
            DebateTimer.openTimer(getScene().getWindow(), timerProperty);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showFullscreen(ActionEvent actionEvent) {
        try {
            FullscreenView.showFullscreen(getScene().getWindow(), componentViewer.getCurrentSpeechElementContainer());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
