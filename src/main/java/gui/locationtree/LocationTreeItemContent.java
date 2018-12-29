package gui.locationtree;

import core.HashIdentifiedSpeechComponent;
import javafx.beans.property.*;

import java.util.Date;

/**
 * For use inside database trees. When card is null, the display is used instead.
 */
public class LocationTreeItemContent {
    final protected HashIdentifiedSpeechComponent component;
    private StringProperty display;
    public void setDisplay(String value) { displayProperty().set(value); }
    public String getDisplay() { return displayProperty().get(); }
    public StringProperty displayProperty() {
        if (display == null) display = new SimpleStringProperty(this, "display");
        return display;
    }


    private ObjectProperty<Date> date;
    public ObjectProperty<Date> dateProperty() {
        if (date==null) date = new SimpleObjectProperty<>(this,"date");
        return date;
    }

    private LongProperty timestamp;
    public void setTimestamp(Long value) {
        timestampProperty().set(value);
        dateProperty().setValue(new Date(value));
    }
    public Long getTimestamp() { return timestampProperty().get(); }
    public LongProperty timestampProperty() {
        if (timestamp == null) timestamp = new SimpleLongProperty(this, "timestamp");
        return timestamp;
    }


    public LocationTreeItemContent(HashIdentifiedSpeechComponent component) {
        this.component = component;
        setDisplay(component.getLabel());
        setTimestamp(component.getTimeStamp());
    }

    public LocationTreeItemContent(String display) {
        component = null;
        setDisplay(display);
        setTimestamp(Long.valueOf(0));
    }

    public String toString(){
        return getDisplay();
    }

    public HashIdentifiedSpeechComponent getSpeechComponent(){
        return component;
    }

}
