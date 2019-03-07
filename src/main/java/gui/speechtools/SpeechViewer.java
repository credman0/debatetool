package gui.speechtools;

import core.Speech;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.util.Set;

public class SpeechViewer {
    public WebView webview;
    @FXML protected
    ScrollPane scrollpane;
    @FXML protected
    BorderPane mainPane;
    private Speech speech;
    final static String WEBVIEW_HTML = SpeechViewer.class.getClassLoader().getResource("BlockViewer.html").toExternalForm();

    public void open(Speech speech) {
        if (!speech.isLoaded()){
            try {
                speech.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.speech = speech;
        generateContents();
    }

    private void generateContents(){
        String contents = speech.getDisplayContent();
        webview.getEngine().getLoadWorker().stateProperty().addListener(new ContentLoader(contents));
        // http://stackoverflow.com/questions/11206942/how-to-hide-scrollbars-in-the-javafx-webview
        webview.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {
            @Override public void onChanged(Change<? extends Node> change) {
                Set<Node> scrolls = webview.lookupAll(".scroll-bar");
                for (Node scroll : scrolls) {
                    scroll.setVisible(false);
                }
            }
        });
    }

    private class ContentLoader implements ChangeListener<Worker.State>{

        private String content;
        private ContentLoader(String content) {
            this.content = content;
        }

        @Override
        public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState) {
            if (newState == Worker.State.SUCCEEDED) {
                webview.getEngine().executeScript("document.getElementById('textarea').innerHTML = \""+content+"\";");
                // put the resizing code in a runLater because otherwise for some reason the size is way too large
                // adapted from http://java-no-makanaikata.blogspot.com/2012/10/javafx-webview-size-trick.html
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        scrollpane.setPrefHeight(-1);
                        Object heightO = webview.getEngine().executeScript("document.getElementById('textarea').clientHeight");
                        if (heightO instanceof Integer) {
                            Integer heightI = (Integer) heightO;
                            double heightD = Double.valueOf(heightI) + 15;
                            webview.setPrefHeight(heightD);
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
}
