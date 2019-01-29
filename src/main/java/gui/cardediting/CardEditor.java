package gui.cardediting;

import core.Card;
import core.Cite;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class CardEditor extends CardViewer{
    @FXML protected BorderPane mainPane;
    @FXML protected TextField authorField;
    @FXML protected TextField dateField;
    @FXML protected TextField additionalField;
    @FXML protected TextArea cardTextArea;

    public void init(){
        authorField.textProperty().addListener((observableValue, s, t1) -> {
            getCard().setCite(authorField.getText(), dateField.getText(), additionalField.getText());
        });
        dateField.textProperty().addListener((observableValue, s, t1) -> {
            getCard().setCite(authorField.getText(), dateField.getText(), additionalField.getText());
        });
        additionalField.textProperty().addListener((observableValue, s, t1) -> {
            getCard().setCite(authorField.getText(), dateField.getText(), additionalField.getText());
        });
        cardTextArea.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    // verify only legal characters are used in card text
                    ((StringProperty)observable).setValue(Card.cleanForCard(newValue));

                    getCard().setText(observable.getValue());
                }
        );
    }

    @Override
    public void open(Card card){
        super.open(card);
        Cite cite = card.getCite();
        authorField.setText(cite.getAuthor());
        dateField.setText(cite.getDate());
        additionalField.setText(cite.getAdditionalInfo());
        cardTextArea.setText(card.getText());
    }

    @Override
    public Pane getPane() {
        return mainPane;
    }

}
