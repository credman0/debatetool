package gui.speechtools;

import core.*;
import io.iocontrollers.IOController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class SpeechEditor {
    @FXML protected
    ScrollPane scrollpane;
    @FXML protected
    BorderPane mainPane;
    @FXML protected
    TreeView speechTreeView;
    @FXML protected
    GridPane viewerArea;
    private Speech speech;
    final static String WEBVIEW_HTML = SpeechEditor.class.getClassLoader().getResource("BlockViewer.html").toExternalForm();

    public void open(Speech speech) {
        if (this.speech == null || speech.getHash()!=this.speech.getHash()) {
            this.speech = speech;
            try {
                // reloading speeches allows us to make sure the blocks didn't change
                speech.reload();
            } catch (IOException e) {
                e.printStackTrace();
            }
            populateTree();
            generateContents();
        }
    }

    public Speech getSpeech(){
        return speech;
    }

    private void generateContents(){
        viewerArea.getChildren().clear();
        for (int i = 0; i < speechTreeView.getRoot().getChildren().size(); i++){
            SpeechComponent child = (SpeechComponent) ((TreeItem) speechTreeView.getRoot().getChildren().get(i)).getValue();
            VBox componentBox = new VBox();
            HBox tagLine = new HBox();
            tagLine.getChildren().add(new Label((i+1) + ")"));
            WebView speechContentView = new WebView();
            speechContentView.getEngine().getLoadWorker().stateProperty().addListener(new ContentLoader(child,speechContentView));
            speechContentView.getEngine().load(WEBVIEW_HTML);
            speechContentView.setDisable(true);
            if (child.getClass().isAssignableFrom(Card.class)){
                ComboBox<String> tagsBox = new ComboBox<>();
                tagsBox.setItems(FXCollections.observableList(((Card) child).getTags()));
                tagLine.getChildren().add(tagsBox);
                tagsBox.getSelectionModel().select(((Card) child).getTagIndex());
                tagsBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                        ((Card) child).setTagIndex((Integer) t1);
                    }
                });
                componentBox.getChildren().add(tagLine);

                // TODO database access optimization

                HBox overlaySelectors = new HBox();

                ComboBox<CardOverlay> underlinesBox = new ComboBox<>();
                ObservableList<CardOverlay> underliningOverlayList = FXCollections.observableArrayList();
                underliningOverlayList.addAll(IOController.getIoController().getOverlayIOManager().getOverlays(((Card) child).getHash(), "Underline"));
                underlinesBox.setItems(FXCollections.observableList((underliningOverlayList)));
                overlaySelectors.getChildren().add(underlinesBox);
                underlinesBox.getSelectionModel().select(((Card) child).getPreferredUnderlineIndex());
                underlinesBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                        ((Card) child).setPreferredUnderlineIndex((Integer) t1);
                        speechContentView.getEngine().getLoadWorker().stateProperty().addListener(new ContentLoader(child,speechContentView));
                        speechContentView.getEngine().load(WEBVIEW_HTML);
                    }
                });

                ComboBox<CardOverlay> highlightsBox = new ComboBox<>();
                ObservableList<CardOverlay> highlightingOverlaysList = FXCollections.observableArrayList();
                highlightingOverlaysList.addAll(IOController.getIoController().getOverlayIOManager().getOverlays(((Card) child).getHash(), "Highlight"));
                highlightsBox.setItems(FXCollections.observableList((highlightingOverlaysList)));
                overlaySelectors.getChildren().add(highlightsBox);
                highlightsBox.getSelectionModel().select(((Card) child).getPreferredHighlightIndex());
                highlightsBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                        ((Card) child).setPreferredHighlightIndex((Integer) t1);
                        speechContentView.getEngine().getLoadWorker().stateProperty().addListener(new ContentLoader(child,speechContentView));
                        speechContentView.getEngine().load(WEBVIEW_HTML);
                    }
                });
                componentBox.getChildren().add(overlaySelectors);
            }else {
                componentBox.getChildren().add(tagLine);
            }
            // http://stackoverflow.com/questions/11206942/how-to-hide-scrollbars-in-the-javafx-webview
            speechContentView.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {
                @Override public void onChanged(Change<? extends Node> change) {
                    Set<Node> scrolls = speechContentView.lookupAll(".scroll-bar");
                    for (Node scroll : scrolls) {
                        scroll.setVisible(false);
                    }
                }
            });
            componentBox.getChildren().add(speechContentView);
            viewerArea.add(componentBox,0,i);
        }
    }

    public void save() {
        List<TreeItem> children = speechTreeView.getRoot().getChildren();
        speech.clearContents();
        for (TreeItem child:children){
            speech.addComponent((SpeechComponent) child.getValue());
        }
        try {
            IOController.getIoController().getComponentIOManager().storeSpeechComponent(speech);
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
        speechTreeView.setCellFactory(new SpeechComponentCellFactory(false, Card.class, Block.class, Analytic.class));
    }

    private void setRoot(TreeItem<SpeechComponent> root){
        speechTreeView.setRoot(root);
        root.getChildren().addListener(new ListChangeListener<TreeItem<SpeechComponent>>() {
            @Override
            public void onChanged(Change<? extends TreeItem<SpeechComponent>> change) {
                generateContents();
            }
        });

    }

    private void populateTree(){
        TreeItem<SpeechComponent> root = new TreeItem<>();
        for (int i = 0; i < speech.size(); i++){
            SpeechComponent component = speech.getComponent(i);
            root.getChildren().add(SpeechComponentCellFactory.createTreeItem(component));
        }
        setRoot(root);
    }
}
