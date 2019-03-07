package gui.blockediting;

import core.Analytic;
import core.Block;
import core.Card;
import core.SpeechComponent;
import gui.speechtools.SpeechComponentCellFactory;
import io.iocontrollers.IOController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.util.List;
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
        if (!block.isLoaded()){
            try {
                block.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            HBox tagLine = new HBox();
            tagLine.getChildren().add(new Label(Block.toAlphabet(i) + ")"));
            WebView blockContentsView = new WebView();
            blockContentsView.getEngine().load(WEBVIEW_HTML);
            blockContentsView.getEngine().getLoadWorker().stateProperty().addListener(new ContentLoader(child,blockContentsView));
            blockContentsView.setDisable(true);
            if (child.getClass().isAssignableFrom(Card.class)){
                ComboBox<String> tagsBox = new ComboBox<>();
                tagsBox.setItems(FXCollections.observableList(((Card) child).getTags()));
                tagLine.getChildren().add(tagsBox);
            }
            componentBox.getChildren().add(tagLine);
            // http://stackoverflow.com/questions/11206942/how-to-hide-scrollbars-in-the-javafx-webview
            blockContentsView.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {
                @Override public void onChanged(ListChangeListener.Change<? extends Node> change) {
                    Set<Node> scrolls = blockContentsView.lookupAll(".scroll-bar");
                    for (Node scroll : scrolls) {
                        scroll.setVisible(false);
                    }
                }
            });
            componentBox.getChildren().add(blockContentsView);
            viewerArea.add(componentBox,0,i);
        }
    }

    public void save() {
        List<TreeItem> children = blockTreeView.getRoot().getChildren();
        block.clearContents();
        for (TreeItem child:children){
            block.addComponent((SpeechComponent) child.getValue());
        }
        try {
            IOController.getIoController().getComponentIOManager().storeSpeechComponent(block);
        } catch (IOException e) {
            e.printStackTrace();
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
                contentView.getEngine().executeScript("document.getElementById('textarea').innerHTML = \""+component.getDisplayContent()+"\";");
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

    private void populateTree(){
        TreeItem<SpeechComponent> root = new TreeItem<>();
        for (int i = 0; i < block.size(); i++){
            root.getChildren().add(new TreeItem<>(block.getComponent(i)));
        }
        setRoot(root);
    }
}
