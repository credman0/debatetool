package gui.cardediting;

import core.Card;
import core.Cite;
import javafx.scene.layout.Pane;

import java.util.List;

public abstract class CardViewer {
    private byte[] currentHash;

    public void swapTo(CardViewer viewer){
        viewer.open(createCard());
    }

    public void clear(){
        setAuthor("");
        setDate("");
        setAdditionalInfo("");
        setText("");
    }
    public void open(Card card){
        setAuthor(card.getCite().getAuthor());
        setDate(card.getCite().getDate());
        setAdditionalInfo(card.getCite().getAdditionalInfo());
        setText(card.getText());
        updateHash();
    }

    public byte[] getCurrentHash(){
        return currentHash;
    }

    public void updateHash(){
        currentHash = createCard().getHash();
    }

    public abstract void save(List<String> path);

    protected Card createCard(){
        return new Card(new Cite(getAuthor(), getDate(), getAdditionalInfo()), getText());
    }

    public abstract void setAuthor(String author);
    public abstract void setDate(String date);
    public abstract void setAdditionalInfo(String additionalInfo);
    public abstract void setText(String text);

    public abstract String getAuthor();
    public abstract String getDate();
    public abstract String getAdditionalInfo();
    public abstract String getText();

    public abstract Pane getPane();
}
