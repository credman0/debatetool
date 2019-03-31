package io.accounts.mongodb;

import com.mongodb.*;
import gui.LoginDialog;
import gui.SettingsHandler;
import io.accounts.AdminManager;
import io.componentio.mongodb.MongoDBComponentIOManager;
import io.overlayio.mongodb.MongoDBOverlayIOManager;
import io.structureio.mongodb.MongoDBStructureIOManager;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.util.Pair;
import org.bson.conversions.Bson;

import java.util.Optional;

public class MongoDBAdminManager implements AdminManager {

    MongoClient mongoClient;
    @Override
    public boolean authenticateAsAdmin() {
        Pair<String, String> credentialStrings = LoginDialog.showDialog();
        if (credentialStrings == null){
            return false;
        }
        MongoCredential credential = MongoCredential.createCredential(credentialStrings.getKey(),
                "admin",
                credentialStrings.getValue().toCharArray());
        mongoClient = new MongoClient(new ServerAddress(SettingsHandler.getSetting("mongod_ip"), Integer.parseInt(SettingsHandler.getSetting("mongod_port"))), credential, MongoClientOptions.builder().build());

        if (!checkIsAuthenticated()) {
            new Alert(Alert.AlertType.ERROR, "Authentication failed!", ButtonType.OK).showAndWait();
            return  false;
        }else {
            return true;
        }
    }


    @Override
    public boolean checkIsAuthenticated() {
        try {
            mongoClient.getDatabase("admin").getCollection("System").find().first();
        }catch (MongoSecurityException e){
            // -4 is error authenticating
            if (e.getCode()==-4){
                return false;
            }else {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public boolean createUser() {
        BasicDBObject createCommand = new BasicDBObject();
        TextInputDialog dialog = new TextInputDialog("user");
        dialog.setTitle("Username selection");
        dialog.setHeaderText("Please select a username");
        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent()){
            return false;
        }
        createCommand.append("createUser", result.get());
        dialog = new TextInputDialog("");
        dialog.setTitle("Password selection");
        dialog.setHeaderText("Please select a password");
        result = dialog.showAndWait();
        if (!result.isPresent() || result.get().equals("")) {
            return false;
        }
        String password = result.get();
        createCommand.append("pwd", password);
        String[] roles = { "readWrite"};
        createCommand.put("roles", roles);
        mongoClient.getDatabase("UDT").runCommand(createCommand);
        return true;
    }
}
