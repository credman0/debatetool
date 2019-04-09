package org.debatetool.gui.cardediting;

import org.debatetool.core.Card;
import org.debatetool.core.Cite;
import org.debatetool.io.iocontrollers.IOController;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.List;

public abstract class CardViewer {
    public void swapTo(CardViewer viewer){
        viewer.open(getCard());
    }

    public void clear(){
        setCard(new Card(new Cite("","",""),""));
    }
    protected abstract Card getCard();
    protected abstract void setCard(Card card);

    public void open(Card card){
        setCard(card);
    }

    public byte[] getCurrentHash(){
        return getCard().getHash();
    }

    public void save(List<String> path) {
        try {
            Card card = getCard();
            if (card == null){
                return;
            }
            IOController.getIoController().getComponentIOManager().storeSpeechComponent(card);
            if (path==null){
                return;
            }
            IOController.getIoController().getStructureIOManager().addContent(path, card);
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    public abstract Pane getPane();

    public abstract void refresh();
}
