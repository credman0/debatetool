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

import com.jfoenix.controls.JFXButton;
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
    private JFXButton toggleButton;
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
            toggleButton.setText("Start");
            timer.cancel();
            isRunning = false;
            timerContents.setDisable(false);
        }else{
            toggleButton.setText("Stop");
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
