package gui.blockediting;

import gui.cardediting.CardCreator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BlockEditorLauncher extends Application {
    @Override
    public void start(Stage primaryStage) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("block_editor.fxml"));
                    Parent root = loader.load();
                    Scene scene = new Scene(root);
                    primaryStage.setScene(scene);
                    primaryStage.show();
                    ((CardCreator)loader.getController()).init();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
