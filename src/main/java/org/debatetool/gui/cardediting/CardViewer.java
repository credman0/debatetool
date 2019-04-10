/*
 *                               This program is free software: you can redistribute it and/or modify
 *                               it under the terms of the GNU General Public License as published by
 *                                the Free Software Foundation, either version 3 of the License, or
 *                                (at your option) any later version.
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
