package gui;

import core.Card;
import core.Cite;
import core.Main;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.List;

public abstract class CardViewer {
    public void swapTo(CardViewer viewer){
        viewer.setAuthor(getAuthor());
        viewer.setDate(getDate());
        viewer.setAdditionalInfo(getAdditionalInfo());
        viewer.setText(getText());
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
    }

    public void save(List<String> path){
        Card card = createCard();
        try {
            Main.getIoController().getComponentIOManager().storeSpeechComponent(card);
            Main.getIoController().getStructureIOManager().addContent(path,card.getHash());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
