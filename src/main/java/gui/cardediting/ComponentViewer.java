package gui.cardediting;

import core.Block;
import core.Card;
import core.HashIdentifiedSpeechComponent;
import gui.blockediting.BlockEditor;
import gui.locationtree.LocationTreeItem;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class ComponentViewer {
    private CardEditor cardEditor;
    private CardCutter cardCutter;
    private CardViewer cardViewer;
    private BorderPane viewerPane;
    private BlockEditor blockEditor;
    private boolean editMode;
    private boolean blockMode;
    public void open(HashIdentifiedSpeechComponent component){
        if (component.getClass().isAssignableFrom(Card.class)){
            blockMode = false;
            if (editMode){
                cardEditor.open((Card) component);
            }else{
                cardCutter.open((Card) component);
            }
        }else if (component.getClass().isAssignableFrom(Block.class)){
            blockMode = true;
            blockEditor.open((Block) component);
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

            cardViewer = cardEditor;
            editMode = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Pane getPane() {
        if (blockMode){
            return blockEditor.getPane();
        }else if (editMode){
            return cardEditor.getPane();
        }else{
            return cardCutter.getPane();
        }
    }

    public void save(LocationTreeItem currentNode){
        if (editMode) {
            cardEditor.save(currentNode.getPath());
        }else {
            cardCutter.save(currentNode.getPath());
        }
    }

    public void clear() {
    }

    public void updateViewerPane(){
        if (blockMode){
            viewerPane.setCenter(blockEditor.getPane());
        } else {
            if (editMode) {
                cardViewer = cardEditor;
                viewerPane.setCenter(cardEditor.getPane());
            } else {
                cardViewer = cardCutter;
                viewerPane.setCenter(cardCutter.getPane());
            }
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
}
