package gui.cardediting;

import core.Block;
import core.Card;
import core.HashIdentifiedSpeechComponent;
import core.Speech;
import gui.blockediting.BlockEditor;
import gui.speechtools.SpeechEditor;
import gui.speechtools.SpeechViewer;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.List;

public class ComponentViewer {
    private CardEditor cardEditor;
    private CardCutter cardCutter;
    private SpeechEditor speechEditor;
    private SpeechViewer speechViewer;
    private BorderPane viewerPane;
    private BlockEditor blockEditor;
    private boolean editMode;
    private enum ViewType {BLOCK, CARD, SPEECH};
    private ViewType currentViewMode = ViewType.CARD;
    public void open(HashIdentifiedSpeechComponent component){
        save();
        if (component.getClass().isAssignableFrom(Card.class)){
            currentViewMode = ViewType.CARD;
            if (editMode){
                cardEditor.open((Card) component);
            }else{
                cardCutter.open((Card) component);
            }
        }else if (component.getClass().isAssignableFrom(Block.class)){
            currentViewMode = ViewType.BLOCK;
            blockEditor.open((Block) component);
        }else if (component.getClass().isAssignableFrom(Speech.class)){
            currentViewMode = ViewType.SPEECH;
            if (editMode) {
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

            editMode = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Pane getPane() {
        switch (currentViewMode) {
            case BLOCK:
                return blockEditor.getPane();
            case CARD:
                if (editMode) {
                    return cardEditor.getPane();
                } else {
                    return cardCutter.getPane();
                }
            case SPEECH:

                if (editMode) {
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
        switch (currentViewMode) {
            case BLOCK:
                blockEditor.save();
                break;

            case CARD:
                if (editMode) {
                    cardEditor.save(path);
                } else {
                    cardCutter.save(path);
                }
                break;

            case SPEECH:
                speechEditor.save();
                break;

            default:
                throw new IllegalStateException("Invalid View Mode");
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
                if (editMode) {
                    cardEditor.refresh();
                } else {
                    cardCutter.refresh();
                }
                break;

            case SPEECH:
                if (editMode) {
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
                viewerPane.setCenter(blockEditor.getPane());
                break;

            case CARD:
                if (editMode) {
                    viewerPane.setCenter(cardEditor.getPane());
                } else {
                    viewerPane.setCenter(cardCutter.getPane());
                }
                break;

            case SPEECH:
                if (editMode) {
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
        if (currentViewMode==ViewType.CARD) {
            if (editMode) {
                cardEditor.swapTo(cardCutter);
            } else {
                cardCutter.swapTo(cardEditor);
            }
        }else if (currentViewMode==ViewType.SPEECH) {
            if (editMode){
                speechViewer.open(speechEditor.getSpeech());
            }else{
                speechEditor.open(speechViewer.getSpeech());
            }
        }
        editMode = !editMode;
        updateViewerPane();
    }

    public void newCard() {
        editMode = true;
        currentViewMode = ViewType.CARD;
        updateViewerPane();
    }
}
