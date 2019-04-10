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

package org.debatetool.gui.locationtree;

import org.debatetool.core.HashIdentifiedSpeechComponent;
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
