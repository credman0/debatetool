package gui.cardediting;

import core.Card;
import core.Cite;
import io.iocontrollers.IOController;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.List;

public abstract class CardViewer {
    private Card card;

    public void swapTo(CardViewer viewer){
        viewer.open(getCard());
    }

    public void clear(){
        card.setCite(new Cite("","",""));
        card.setText("");
    }

    public void open(Card card){
        this.card = card;
    }

    public byte[] getCurrentHash(){
        return card.getHash();
    }

    public void save(List<String> path) {
        try {
            IOController.getIoController().getComponentIOManager().storeSpeechComponent(card);
            IOController.getIoController().getStructureIOManager().addContent(path, card.getHash());
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    public Card getCard(){
            if (card==null){
            card = new Card(new Cite("","",""),"");
        }
        return card;
    }

    public abstract Pane getPane();
}
