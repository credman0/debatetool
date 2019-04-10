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

package org.debatetool.gui.timer;

import java.text.DecimalFormat;

public class DebateTime {
    private long time;
    private String name;

    public static DecimalFormat secondsFormat = new DecimalFormat("00");

    public DebateTime(String name, long time){
        this.name = name;
        this.time = time;
    }

    public String timeToString(){
        long minutes = time/(60*1000);
        long remainder = time%(60*1000);
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
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
