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

package org.debatetool.gui.cardediting;

import org.debatetool.io.iocontrollers.IOController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class MainGuiLauncher extends Application {
    @Override
    public void start(Stage primaryStage) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main_gui.fxml"));
                    Parent root = loader.load();
                    //root.setStyle();
                    Scene scene = new Scene(root);
                    primaryStage.setScene(scene);
                    primaryStage.show();
                    primaryStage.setTitle("Debate Tool");
                    ((MainGui)loader.getController()).init();
                    primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent windowEvent) {
                            try {
                                IOController.getIoController().close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
