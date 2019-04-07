package org.debatetool.gui.blockediting;

import org.debatetool.core.*;
import org.debatetool.core.html.HtmlEncoder;
import org.debatetool.gui.SettingsHandler;
import org.debatetool.gui.cardediting.MainGui;
import org.debatetool.gui.speechtools.SpeechComponentCellFactory;
import org.debatetool.io.iocontrollers.IOController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BlockEditor {
    @FXML protected
    ScrollPane scrollpane;
    @FXML protected
    BorderPane mainPane;
    @FXML protected
    TreeView blockTreeView;
    @FXML protected
    GridPane viewerArea;
    Block block;
    final static String WEBVIEW_HTML = BlockEditor.class.getClassLoader().getResource("BlockViewer.html").toExternalForm();

    public void open(Block block) {
        // TODO try to not reload blocks when unnecessary
        Task task = new Task() {
            @Override
            protected Object call() {
                MainGui.getActiveGUI().getScene().getRoot().setCursor(Cursor.WAIT);
                try {
                    block.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MainGui.getActiveGUI().getScene().getRoot().setCursor(Cursor.DEFAULT);
                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        this.block = block;
        populateTree();
        generateContents();
    }

    private void generateContents(){
        viewerArea.getChildren().clear();
        for (int i = 0; i < blockTreeView.getRoot().getChildren().size(); i++){
            SpeechComponent child = (SpeechComponent) ((TreeItem) blockTreeView.getRoot().getChildren().get(i)).getValue();
            VBox componentBox = new VBox();
            HBox tagLine = new HBox();;
            Text label = new Text(Block.toAlphabet(i) + ")");
            label.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            tagLine.getChildren().add(label);
            WebView blockContentsView = new WebView();
            blockContentsView.getEngine().getLoadWorker().stateProperty().addListener(new ContentLoader(child,blockContentsView));
            blockContentsView.getEngine().load(WEBVIEW_HTML);
            if (child.getClass().isAssignableFrom(Card.class)){
                ComboBox<String> tagsBox = new ComboBox<>();
                tagsBox.setItems(FXCollections.observableList(((Card) child).getTags()));
                tagLine.getChildren().add(tagsBox);
                tagsBox.getSelectionModel().select(((Card) child).getTagIndex());
                tagsBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                        ((Card) child).setTagIndex((Integer) t1);
                        blockTreeView.refresh();
                    }
                });
                componentBox.getChildren().add(tagLine);
                tagsBox.prefWidthProperty().bind(((Region)tagsBox.getParent()).widthProperty());

                HBox overlaySelectors = new HBox();

                ComboBox<CardOverlay> underlinesBox = new ComboBox<>();
                ObservableList<CardOverlay> underliningOverlayList = FXCollections.observableArrayList();
                underliningOverlayList.addAll(((Card) child).getUnderlining());
                underlinesBox.setItems(FXCollections.observableList((underliningOverlayList)));
                overlaySelectors.getChildren().add(underlinesBox);
                underlinesBox.getSelectionModel().select(((Card) child).getPreferredUnderlineIndex());
                underlinesBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                        ((Card) child).setPreferredUnderlineIndex((Integer) t1);
                        blockContentsView.getEngine().getLoadWorker().stateProperty().addListener(new ContentLoader(child,blockContentsView));
                        blockContentsView.getEngine().load(WEBVIEW_HTML);
                    }
                });

                ComboBox<CardOverlay> highlightsBox = new ComboBox<>();
                ObservableList<CardOverlay> highlightingOverlaysList = FXCollections.observableArrayList();
                highlightingOverlaysList.addAll(((Card) child).getHighlighting());
                highlightsBox.setItems(FXCollections.observableList((highlightingOverlaysList)));
                overlaySelectors.getChildren().add(highlightsBox);
                highlightsBox.getSelectionModel().select(((Card) child).getPreferredHighlightIndex());
                highlightsBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                        ((Card) child).setPreferredHighlightIndex((Integer) t1);
                        blockContentsView.getEngine().getLoadWorker().stateProperty().addListener(new ContentLoader(child,blockContentsView));
                        blockContentsView.getEngine().load(WEBVIEW_HTML);
                    }
                });
                componentBox.getChildren().add(overlaySelectors);
                overlaySelectors.prefWidthProperty().bind(((Region)overlaySelectors.getParent()).widthProperty());
            }else {
                componentBox.getChildren().add(tagLine);
            }
            // http://stackoverflow.com/questions/11206942/how-to-hide-scrollbars-in-the-javafx-webview
            blockContentsView.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {
                @Override public void onChanged(ListChangeListener.Change<? extends Node> change) {
                    Set<Node> scrolls = blockContentsView.lookupAll(".scroll-bar");
                    for (Node scroll : scrolls) {
                        scroll.setVisible(false);
                    }
                }
            });

            ContextMenu localMenu = new ContextMenu();
            MenuItem newAnalyticItem =new MenuItem("Insert Analytic Above");
            newAnalyticItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    updateBlockContents();
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Insert Analytic");
                    dialog.setHeaderText("Enter the analytic text");
                    Optional<String> result = dialog.showAndWait();
                    if(!result.isPresent()){
                        return;
                    }
                    block.insertComponentAbove(child, new Analytic(result.get()));
                    refresh();
                }
            });
            localMenu.getItems().add(newAnalyticItem);
            blockContentsView.setContextMenuEnabled(false);
            blockContentsView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if(mouseEvent.getButton().equals(MouseButton.SECONDARY)){
                        localMenu.show(blockContentsView, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                    }else{
                        localMenu.hide();
                    }
                }
            });
            componentBox.getChildren().add(blockContentsView);
            viewerArea.add(componentBox,0,i);
        }
    }

    public void save() {
        updateBlockContents();
        try {
            IOController.getIoController().getComponentIOManager().storeSpeechComponent(block);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> fullpath = block.getPath();
    }

    private void updateBlockContents(){
        List<TreeItem> children = blockTreeView.getRoot().getChildren();
        block.clearContents();
        for (TreeItem child:children){
            block.addComponent((SpeechComponent) child.getValue());
        }
    }

    private class ContentLoader implements ChangeListener<Worker.State>{
        private final WebView contentView;
        private final SpeechComponent component;

        private ContentLoader(SpeechComponent component, WebView contentView) {
            this.component = component;
            this.contentView = contentView;
        }

        @Override
        public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState) {
            if (newState == Worker.State.SUCCEEDED) {
                contentView.getEngine().executeScript("document.getElementById('style').sheet.cssRules[0].style.backgroundColor = '"+ SettingsHandler.getColorTag()+"';");
                contentView.getEngine().executeScript("document.getElementById('textarea').innerHTML = \""+ component.getDisplayContent()+"\";");
                // put the resizing code in a runLater because otherwise for some reason the size is way too large
                // adapted from http://java-no-makanaikata.blogspot.com/2012/10/javafx-webview-size-trick.html
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        viewerArea.setPrefHeight(-1);
                        Object heightO = contentView.getEngine().executeScript("document.getElementById('textarea').clientHeight");
                        if (heightO instanceof Integer) {
                            Integer heightI = (Integer) heightO;
                            double heightD = Double.valueOf(heightI) + 15;
                            contentView.setPrefHeight(heightD);
                        }else{
                            throw new IllegalStateException("Document height returned is not an Integer");
                        }
                    }
                });

            }
        }
    }

    public Pane getPane() {
        return mainPane;
    }

    public void init() {
        blockTreeView.setCellFactory(new SpeechComponentCellFactory(true, Card.class, Analytic.class));
        blockTreeView.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode().equals(KeyCode.DELETE)){
                    TreeItem<SpeechComponent> item = (TreeItem<SpeechComponent>) blockTreeView.getSelectionModel().getSelectedItem();
                    if (item!=null){
                        updateBlockContents();
                        block.removeComponent(item.getValue());
                        refresh();
                    }
                }
            }
        });
    }

    private void setRoot(TreeItem<SpeechComponent> root){
        blockTreeView.setRoot(root);
        root.getChildren().addListener(new ListChangeListener<TreeItem<SpeechComponent>>() {
            @Override
            public void onChanged(Change<? extends TreeItem<SpeechComponent>> change) {
                generateContents();
            }
        });
    }

    public void refresh(){
        populateTree();
        generateContents();
    }

    private void populateTree(){
        TreeItem<SpeechComponent> root = new TreeItem<>();
        for (int i = 0; i < block.size(); i++){
            root.getChildren().add(new TreeItem<>(block.getComponent(i)));
        }
        setRoot(root);
    }
}
