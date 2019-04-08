package org.debatetool.gui.timer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.Timer;
import java.util.TimerTask;

public class Stopwatch {
    @FXML
    private Text timerContents;
    @FXML
    private  Pane pane;
    private Timer timer;
    private StopwatchTimerTask timerTask;
    private boolean isRunning = false;
    private long lastAbsoluteTime = 0;
    private long duration = 30000;
    private long time = duration;

    public void setTimerDuration(long duration){
        this.duration = duration;
    }

    public void resetTimer(){
        time = duration;
        setTimerDisplayTime(time);
    }

    public Pane getPane() {
        return pane;
    }

    public void toggleTimer(ActionEvent actionEvent) {
        if (isRunning){
            timer.cancel();
            isRunning = false;
            timerContents.setDisable(false);
        }else{
            timer= new Timer(true);
            lastAbsoluteTime = System.currentTimeMillis();
            timerTask = new StopwatchTimerTask();
            timer.schedule(timerTask, 0, 100);
            isRunning = true;
            timerContents.setDisable(true);
        }
    }

    private void setTimerDisplayTime(long time){
        long displayTime = Math.abs(time);
        long minutes = displayTime/(60*1000);
        long remainder = displayTime%(60*1000);
        long seconds = remainder/1000;
        long centiseconds = (remainder%1000)/100;
        String prefix;
        if (time<0){
            prefix = "-";
            timerContents.setFill(Color.RED);
        }else{
            prefix = "";
            timerContents.setFill(Color.BLACK);
        }
        timerContents.setText(prefix + minutes+":"+DebateTime.secondsFormat.format(seconds)+"."+centiseconds);
    }

    private class StopwatchTimerTask extends TimerTask{
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            long timeDifference = currentTime - lastAbsoluteTime;
            lastAbsoluteTime = currentTime;
            time -= timeDifference;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    setTimerDisplayTime(time);
                }
            });
        }
    }
}
