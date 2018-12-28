package gui;

import core.Card;
import core.Cite;
import io.componentio.ComponentIOManager;
import io.componentio.mongodb.MongoDBComponentIOManager;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class CardCreator extends Application {
    @FXML protected TextField authorField;
    @FXML protected TextField dateField;
    @FXML protected TextField additionalField;
    @FXML protected TextArea cardTextArea;

    ComponentIOManager ioManager;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            ioManager = new MongoDBComponentIOManager();
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("card_creator.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleMenuKeyInput(KeyEvent keyEvent) {

    }

    public void saveCard(ActionEvent actionEvent) {
        Card card = new Card(new Cite(authorField.getText(), dateField.getText(), additionalField.getText()), cardTextArea.getText());
        try {
            ioManager.storeSpeechComponent(card);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
