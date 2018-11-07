package core.locationtree;

import core.Card;

/**
 * For use inside database trees. When card is null, the display is used instead.
 */
public class LocationTreeItemContent {
    final protected Card card;
    final protected String display;


    public LocationTreeItemContent(Card card) {
        this.card = card;
        display = null;
    }

    public LocationTreeItemContent(String display) {
        card = null;
        this.display = display;
    }

    public String toString(){
        if (card == null){
            return display;
        }else{
            return card.getCite().toString();
        }
    }

    public Card getCard(){
        return card;
    }
}
