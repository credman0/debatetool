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

package org.debatetool.gui.speechtools;

import org.debatetool.core.Speech;
import org.debatetool.core.SpeechElementContainer;
import org.debatetool.core.html.HtmlEncoder;
import org.debatetool.gui.SettingsHandler;
import org.debatetool.gui.cardediting.MainGui;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
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
    private SpeechElementContainer speech;
    final static String WEBVIEW_HTML = SpeechViewer.class.getClassLoader().getResource("BlockViewer.html").toExternalForm();

    public void open(SpeechElementContainer speech) {
        if (!speech.isLoaded()){
            Task task = new Task() {
                @Override
                protected Object call() {
                    MainGui.getActiveGUI().getScene().getRoot().setCursor(Cursor.WAIT);
                    try {
                        speech.load();
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

        }
        this.speech = speech;
        generateContents();
    }

    public void refresh(){
        generateContents();
    }


    private void generateContents(){
        String contents = speech.getDisplayContent();
        webview.getEngine().load(WEBVIEW_HTML);
        webview.getEngine().getLoadWorker().stateProperty().addListener(new ContentLoader(contents));
        webview.setDisable(true);
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

    public SpeechElementContainer getSpeech() {
        return speech;
    }

    private class ContentLoader implements ChangeListener<Worker.State>{

        private String content;
        private ContentLoader(String content) {
            this.content = content;
        }

        @Override
        public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState) {
            if (newState == Worker.State.SUCCEEDED) {
                webview.getEngine().executeScript("document.getElementById('style').sheet.cssRules[0].style.backgroundColor = '"+ SettingsHandler.getColorTag()+"';");
                webview.getEngine().executeScript("document.getElementById('textarea').innerHTML = \""+ content+"\";");
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
