package org.debatetool.gui.cardediting;

import org.debatetool.core.Card;
import org.debatetool.core.Cite;
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

    public void init(){}

    @Override
    protected Card getCard() {
        return new Card(new Cite(authorField.getText(), dateField.getText(), additionalField.getText()), cardTextArea.getText());
    }

    @Override
    protected void setCard(Card card) {
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

    @Override
    public void refresh() {
        // TODO do something? (if necessary)
    }

}
