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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

    public void init(){
        cardTextArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if (!s.equals(t1)){
                    ((StringProperty) observableValue).set(Card.cleanForCard(t1));
                }
            }
        });
    }

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
