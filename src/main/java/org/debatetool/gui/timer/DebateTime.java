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
