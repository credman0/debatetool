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

package org.debatetool.gui;

import com.jfoenix.controls.JFXToggleButton;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.util.Pair;
import org.debatetool.io.initializers.DatabaseInitializer;
import org.debatetool.io.initializers.FileSystemInitializer;
import org.debatetool.io.initializers.IOInitializer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

public class LoginDialog {
    @FXML
    public DialogPane pane;
    @FXML
    public TextField usernameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public JFXToggleButton useLocalFilesystemToggle;
    @FXML
    public Button databaseSettingsButton;
    @FXML
    public TextField fileField;

    private void init(){
        databaseSettingsButton.disableProperty().bind(useLocalFilesystemToggle.selectedProperty());
        usernameField.disableProperty().bind(useLocalFilesystemToggle.selectedProperty());
        passwordField.disableProperty().bind(useLocalFilesystemToggle.selectedProperty());
        fileField.disableProperty().bind(Bindings.not(useLocalFilesystemToggle.selectedProperty()));
        String rootDirectory = SettingsHandler.getSetting("base_directory");
        if (rootDirectory!=null){
            fileField.setText(rootDirectory);
        }else{
            fileField.setText(System.getProperty("user.home")+"/debatefiles");
        }
        String useLocal = SettingsHandler.getSetting("use_local");
        if (useLocal!=null){
            useLocalFilesystemToggle.setSelected(Boolean.parseBoolean(useLocal));
        }else{
            useLocalFilesystemToggle.setSelected(true);
        }

        String username = SettingsHandler.getSetting("username");
        if (username!=null){
            usernameField.setText(username);
        }else{
            usernameField.setText("");
        }
    }

    public static IOInitializer showDialog() {
        FXMLLoader dialogLoader = new FXMLLoader(LoginDialog.class.getClassLoader().getResource("login_dialog.fxml"));
        try {
            dialogLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LoginDialog loginDialog = dialogLoader.getController();
        loginDialog.init();
        Dialog dialog = new Dialog();
        dialog.setDialogPane(loginDialog.pane);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SettingsHandler.setSetting("base_directory",loginDialog.fileField.getText());
            SettingsHandler.setSetting("use_local", String.valueOf(loginDialog.useLocalFilesystemToggle.isSelected()));
            SettingsHandler.setSetting("username",loginDialog.usernameField.getText());
            try {
                SettingsHandler.store();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (loginDialog.useLocalFilesystemToggle.isSelected()){
                return new FileSystemInitializer(Paths.get(loginDialog.fileField.getText()));
            }
            String address = SettingsHandler.getSetting("mongod_ip");
            int port = Integer.parseInt(SettingsHandler.getSetting("mongod_port"));
            return new DatabaseInitializer(address,port,loginDialog.usernameField.getText(), loginDialog.passwordField.getText());
        }else{
            return null;
        }
    }

    public void showDatabaseSettings(ActionEvent actionEvent) {
        DatabaseOptionsDialog.updateSettings();
    }
}
