package gui.locationtree;

import core.SpeechComponent;

/**
 * For use inside database trees. When card is null, the display is used instead.
 */
public class LocationTreeItemContent {
    final protected SpeechComponent component;
    final protected String display;


    public LocationTreeItemContent(SpeechComponent component) {
        this.component = component;
        display = null;
    }

    public LocationTreeItemContent(String display) {
        component = null;
        this.display = display;
    }

    public String toString(){
        if (component == null){
            return display;
        }else{
            return component.getLabel();
        }
    }

    public SpeechComponent getSpeechComponent(){
        return component;
    }
}
