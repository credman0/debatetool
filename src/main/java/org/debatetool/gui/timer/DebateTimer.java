package org.debatetool.gui.timer;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTabPane;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class DebateTimer {
    @FXML
    public JFXTabPane pane;
    public BorderPane tab1Pane;
    public BorderPane negPrepPane;
    public BorderPane affPrepPane;
    public JFXListView <DebateTime> timesListView;
    private ObservableList<DebateTime> debateTimes = FXCollections.observableArrayList();

    public void init() throws IOException {
        debateTimes.add(new DebateTime("Constructive", 540*1000));
        debateTimes.add(new DebateTime("Rebuttal", 360*1000));
        timesListView.setItems(debateTimes);


        FXMLLoader stopwatchLoader = new FXMLLoader(Stopwatch.class.getClassLoader().getResource("stopwatch.fxml"));
        stopwatchLoader.load();
        Stopwatch speechTimer = stopwatchLoader.getController();
        speechTimer.setTimerDuration(timesListView.getItems().get(0).getTime());
        speechTimer.resetTimer();
        tab1Pane.setCenter(speechTimer.getPane());
        timesListView.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getClickCount() == 2) {
                DebateTime debateTime = (DebateTime) timesListView.getSelectionModel().getSelectedItem();
                if (debateTime != null){
                    speechTimer.setTimerDuration(debateTime.getTime());
                    speechTimer.resetTimer();
                }
            }
        });

        stopwatchLoader = new FXMLLoader(Stopwatch.class.getClassLoader().getResource("stopwatch.fxml"));
        stopwatchLoader.load();
        Stopwatch negPrepTimer = stopwatchLoader.getController();
        negPrepTimer.resetTimer();
        negPrepPane.setCenter(negPrepTimer.getPane());

        stopwatchLoader = new FXMLLoader(Stopwatch.class.getClassLoader().getResource("stopwatch.fxml"));
        stopwatchLoader.load();
        Stopwatch affPrepTimer = stopwatchLoader.getController();
        affPrepTimer.resetTimer();
        affPrepPane.setCenter(affPrepTimer.getPane());
    }

    public static void openTimer(Window parentWindow, BooleanProperty blockOthersProperty) throws IOException {
        blockOthersProperty.setValue(true);
        FXMLLoader timerLoader = new FXMLLoader(DebateTimer.class.getClassLoader().getResource("timer.fxml"));
        timerLoader.load();
        DebateTimer timer = timerLoader.getController();
        timer.init();
        Scene scene = new Scene(timer.pane);
        scene.setFill(Color.TRANSPARENT);
        Stage newWindow = new Stage();
        newWindow.initOwner(parentWindow);
        newWindow.setTitle("Debate Timer");
        newWindow.setScene(scene);
        newWindow.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                blockOthersProperty.setValue(false);
            }
        });
        newWindow.show();
    }
}
