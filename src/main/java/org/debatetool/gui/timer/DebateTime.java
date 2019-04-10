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

package org.debatetool.gui.timer;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

import java.text.DecimalFormat;

public class DebateTime {
    private SimpleLongProperty time = new SimpleLongProperty();
    private String name;

    public static DecimalFormat secondsFormat = new DecimalFormat("00");

    public DebateTime(String name, long time){
        this.name = name;
        this.time.set(time);
    }

    public String timeToString(){
        long minutes = time.get()/(60*1000);
        long remainder = time.get()%(60*1000);
        long seconds = remainder/1000;
        return minutes+":"+secondsFormat.format(seconds);
    }

    public String toString(){
        return name + " ("+timeToString()+")";
    }

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTime() {
        return time.get();
    }

    public void setTime(long time) {
        this.time.set(time);
    }

    public LongProperty getTimeProperty(){
        return time;
    }
}
