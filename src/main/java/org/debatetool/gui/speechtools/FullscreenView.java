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

package org.debatetool.gui.speechtools;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.debatetool.core.SpeechElementContainer;
import org.debatetool.gui.timer.Stopwatch;

import java.io.IOException;

public class FullscreenView {
    public BorderPane pane;

    public void keyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ESCAPE) || (keyEvent.isShortcutDown()&&keyEvent.getCode().equals(KeyCode.F))){
            ((Stage)pane.getScene().getWindow()).close();
        }
    }

    public static void showFullscreen (Window parentWindow, SpeechElementContainer speech) throws IOException {
        FXMLLoader viewLoader = new FXMLLoader(FullscreenView.class.getClassLoader().getResource("fullscreen_view.fxml"));
        viewLoader.load();
        FullscreenView fullscreenView = viewLoader.getController();

        FXMLLoader speechViewerLoader = new FXMLLoader(SpeechViewer.class.getClassLoader().getResource("speech_viewer.fxml"));
        speechViewerLoader.load();
        SpeechViewer speechViewer = speechViewerLoader.getController();

        fullscreenView.pane.setCenter(speechViewer.getPane());
        speechViewer.open(speech);

        Scene scene = new Scene(fullscreenView.pane);
        Stage newWindow = new Stage();
        newWindow.setScene(scene);
        newWindow.initOwner(parentWindow);
        newWindow.setFullScreen(true);
        newWindow.setTitle("Speech Viewer");
        scene.getRoot().requestFocus();
        newWindow.show();
    }
}
