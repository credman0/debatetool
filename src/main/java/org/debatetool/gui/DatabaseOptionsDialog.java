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

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.util.Pair;

import java.io.IOException;
import java.util.Optional;

public class DatabaseOptionsDialog {
    @FXML
    public TextField addressField;
    @FXML
    public TextField portField;
    @FXML
    public DialogPane pane;

    public static Pair<String,Integer> showDialog() {
        FXMLLoader dialogLoader = new FXMLLoader(DatabaseOptionsDialog.class.getClassLoader().getResource("database_options.fxml"));

        try {
            dialogLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        DatabaseOptionsDialog optionsDialog = dialogLoader.getController();
        optionsDialog.addressField.setText(SettingsHandler.getSetting("mongod_ip"));
        optionsDialog.portField.setText(SettingsHandler.getSetting("mongod_port"));
        Dialog dialog = new Dialog();
        dialog.setDialogPane(optionsDialog.pane);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            return new Pair<>(optionsDialog.addressField.getText(), Integer.parseInt(optionsDialog.portField.getText()));
        } else {
            return null;
        }
    }

    public static void updateSettings(){
        Pair<String, Integer> settings = showDialog();
        if (settings == null){
            return;
        }else{
            SettingsHandler.setSetting("mongod_ip", settings.getKey());
            SettingsHandler.setSetting("mongod_port", String.valueOf(settings.getValue()));
            try {
                SettingsHandler.store();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
