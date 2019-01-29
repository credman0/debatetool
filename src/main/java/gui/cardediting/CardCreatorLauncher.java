package gui.cardediting;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CardCreatorLauncher extends Application {
    @Override
    public void start(Stage primaryStage) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("card_creator.fxml"));
                    Parent root = loader.load();
                    //root.setStyle();
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
