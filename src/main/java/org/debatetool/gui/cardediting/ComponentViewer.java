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

import javafx.stage.FileChooser;
import org.debatetool.core.*;
import org.debatetool.gui.SettingsHandler;
import org.debatetool.gui.blockediting.BlockEditor;
import org.debatetool.gui.speechtools.DOCXExporter;
import org.debatetool.gui.speechtools.SpeechEditor;
import org.debatetool.gui.speechtools.SpeechViewer;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ComponentViewer {
    private CardEditor cardEditor;
    private CardCutter cardCutter;
    private SpeechEditor speechEditor;
    private SpeechViewer speechViewer;
    private BorderPane viewerPane;
    private BlockEditor blockEditor;
    private SimpleBooleanProperty editMode = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty preventContainerActions = new SimpleBooleanProperty(true);
    private enum ViewType {BLOCK, CARD, SPEECH};
    private ViewType currentViewMode = ViewType.CARD;

    public void bindEditMode(Property<Boolean> property){
        property.bindBidirectional(editMode);
    }
    public void bindPreventContainerActions(Property<Boolean> property){
        property.bindBidirectional(preventContainerActions);
    }

    public void open(HashIdentifiedSpeechComponent component){
        save();

        if (component.getClass().isAssignableFrom(Card.class)){
            currentViewMode = ViewType.CARD;
            if (editMode.get()){
                cardCutter.open((Card) component);
            }else{
                cardEditor.open((Card) component);
            }
        }else if (component.getClass().isAssignableFrom(Block.class)){
            currentViewMode = ViewType.BLOCK;
            blockEditor.open((Block) component);
        }else if (component.getClass().isAssignableFrom(Speech.class)){
            currentViewMode = ViewType.SPEECH;
            if (editMode.get()) {
                speechEditor.open((Speech) component);
            }else{
                speechViewer.open((Speech) component);
            }
        }else{
            throw new IllegalArgumentException("Unrecognized type: " + component.getClass());
        }
        updateViewerPane();

    }
    public void init(BorderPane viewerPane){
        this.viewerPane = viewerPane;
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

            FXMLLoader blockLoader = new FXMLLoader(getClass().getClassLoader().getResource("block_editor.fxml"));
            blockLoader.load();
            blockEditor = blockLoader.getController();
            blockEditor.init();

            FXMLLoader speechEditLoader = new FXMLLoader(getClass().getClassLoader().getResource("speech_editor.fxml"));
            speechEditLoader.load();
            speechEditor = speechEditLoader.getController();
            speechEditor.init();

            FXMLLoader speechViewLoader = new FXMLLoader(getClass().getClassLoader().getResource("speech_viewer.fxml"));
            speechViewLoader.load();
            speechViewer = speechViewLoader.getController();

            editMode.set(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SpeechElementContainer getCurrentSpeechElementContainer(){
        if (currentViewMode==ViewType.SPEECH){
            if (editMode.get()){
                return speechEditor.getSpeech();
            }else{
                return speechViewer.getSpeech();
            }
        }else if (currentViewMode==ViewType.BLOCK){
            return blockEditor.getBlock();
        }else{
            throw new IllegalStateException("Speech Element Container not active");
        }
    }

    public void exportToDOCX() throws IOException {
        SpeechElementContainer container = getCurrentSpeechElementContainer();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export speech to DOCX");
        fileChooser.setInitialFileName(container.getName()+".docx");
        File file = fileChooser.showSaveDialog(getPane().getScene().getWindow());
        if (!file.getName().endsWith(".docx")){
            file = new File(file.getPath()+".docx");
        }
        DOCXExporter.export(container.getExportDisplayContent(SettingsHandler.getExportAnalytics()), file);
    }

    public Pane getPane() {
        switch (currentViewMode) {
            case BLOCK:
                return blockEditor.getPane();
            case CARD:
                if (editMode.get()) {
                    return cardCutter.getPane();
                } else {
                    return cardEditor.getPane();
                }
            case SPEECH:
                if (editMode.get()) {
                    return speechEditor.getPane();
                } else {
                    return speechViewer.getPane();
                }

            default:
                throw new IllegalStateException("Invalid View Mode");
        }
    }

    public void save(){
        save(null);
    }

    public void save(List<String> path){
        Task task = new Task<>() {
            @Override
            protected Object call() throws Exception {
                MainGui.getActiveGUI().getScene().getRoot().setCursor(Cursor.WAIT);
                switch (currentViewMode) {
                    case BLOCK:
                        blockEditor.save();
                        break;

                    case CARD:
                        if (editMode.get()) {
                            cardCutter.save(path);
                        } else {
                            cardEditor.save(path);
                        }
                        break;

                    case SPEECH:
                        speechEditor.save();
                        break;

                    default:
                        throw new IllegalStateException("Invalid View Mode");
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

    public void clear() {
    }

    public void refresh(){
        switch (currentViewMode) {
            case BLOCK:
                blockEditor.refresh();
                break;

            case CARD:
                if (editMode.get()) {
                    cardCutter.refresh();
                } else {
                    cardEditor.refresh();
                }
                break;

            case SPEECH:
                if (editMode.get()) {
                    speechEditor.refresh();
                } else {
                    speechEditor.refresh();
                }

                break;

            default:
                throw new IllegalStateException("Invalid View Mode");
        }
    }

    public void updateViewerPane(){
        switch (currentViewMode) {
            case BLOCK:
                preventContainerActions.set(false);
                viewerPane.setCenter(blockEditor.getPane());
                break;

            case CARD:
                preventContainerActions.set(true);
                if (editMode.get()) {
                    viewerPane.setCenter(cardCutter.getPane());
                } else {
                    viewerPane.setCenter(cardEditor.getPane());
                }
                break;

            case SPEECH:
                preventContainerActions.set(false);
                if (editMode.get()) {
                    viewerPane.setCenter(speechEditor.getPane());
                } else {
                    viewerPane.setCenter(speechViewer.getPane());
                }
                break;

            default:
                throw new IllegalStateException("Invalid View Mode");
        }

    }

    public void togglePanes() {
        editMode.set(!editMode.get());
        updateEdit();
    }

    public void updateEdit(){
        if (currentViewMode==ViewType.CARD) {
            if (editMode.get()) {
                cardEditor.swapTo(cardCutter);
            } else {
                cardCutter.swapTo(cardEditor);
            }
        }else if (currentViewMode==ViewType.SPEECH) {
            if (editMode.get()){
                speechEditor.open((Speech) speechViewer.getSpeech());
            }else{
                speechViewer.open(speechEditor.getSpeech());
            }
        }
        updateViewerPane();
    }

    public void newCard() {
        editMode.set(false);
        currentViewMode = ViewType.CARD;
        updateViewerPane();
    }
}
