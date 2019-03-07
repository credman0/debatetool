package gui.cardediting;

import core.Block;
import core.Card;
import core.HashIdentifiedSpeechComponent;
import core.Speech;
import gui.blockediting.BlockEditor;
import gui.locationtree.LocationTreeItem;
import gui.speechtools.SpeechEditor;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class ComponentViewer {
    private CardEditor cardEditor;
    private CardCutter cardCutter;
    private SpeechEditor speechEditor;
    private CardViewer cardViewer;
    private BorderPane viewerPane;
    private BlockEditor blockEditor;
    private boolean editMode;
    private enum ViewType {BLOCK, CARD, SPEECH};
    private ViewType currentViewMode = ViewType.CARD;
    public void open(HashIdentifiedSpeechComponent component){
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
            speechEditor.open((Speech) component);
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

            cardViewer = cardEditor;
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
                return speechEditor.getPane();

            default:
                throw new IllegalStateException("Invalid View Mode");
        }
    }

    public void save(LocationTreeItem currentNode){
        switch (currentViewMode) {
            case BLOCK:
                blockEditor.save();
                break;

            case CARD:
                if (editMode) {
                    cardEditor.save(currentNode.getPath());
                } else {
                    cardCutter.save(currentNode.getPath());
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

    public void updateViewerPane(){
        switch (currentViewMode) {
            case BLOCK:
                viewerPane.setCenter(blockEditor.getPane());
                break;

            case CARD:
                if (editMode) {
                    cardViewer = cardEditor;
                    viewerPane.setCenter(cardEditor.getPane());
                } else {
                    cardViewer = cardCutter;
                    viewerPane.setCenter(cardCutter.getPane());
                }
                break;

            case SPEECH:
                viewerPane.setCenter(speechEditor.getPane());
                break;

            default:
                throw new IllegalStateException("Invalid View Mode");
        }

    }

    public void togglePanes() {
        if (editMode) {
            cardEditor.swapTo(cardCutter);
            editMode = false;
        } else {
            cardCutter.swapTo(cardEditor);
            editMode = true;
        }
        updateViewerPane();
    }

    public void newCard() {
        editMode = true;
        currentViewMode = ViewType.CARD;
        updateViewerPane();
    }
}
