package org.debatetool.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.util.Pair;

import java.io.IOException;
import java.util.Optional;

public class LoginDialog {
    @FXML
    public DialogPane pane;
    @FXML
    public TextField usernameField;
    @FXML
    public PasswordField passwordField;

    public static Pair<String,String> showDialog() {
        FXMLLoader dialogLoader = new FXMLLoader(LoginDialog.class.getClassLoader().getResource("login_dialog.fxml"));
        try {
            dialogLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LoginDialog loginDialog = dialogLoader.getController();
        Dialog dialog = new Dialog();
        dialog.setDialogPane(loginDialog.pane);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            return new Pair<>(loginDialog.usernameField.getText(), loginDialog.passwordField.getText());
        }else{
            return null;
        }
    }

    public void showDatabaseSettings(ActionEvent actionEvent) {
        DatabaseOptionsDialog.updateSettings();
    }
}
