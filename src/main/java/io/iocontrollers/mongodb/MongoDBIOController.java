package io.iocontrollers.mongodb;

import com.mongodb.*;
import gui.LoginDialog;
import gui.SettingsHandler;
import io.accounts.AdminManager;
import io.accounts.DBLock;
import io.accounts.mongodb.MongoDBAdminManager;
import io.accounts.mongodb.MongoDBLock;
import io.componentio.ComponentIOManager;
import io.componentio.mongodb.MongoDBComponentIOManager;
import io.iocontrollers.IOController;
import io.overlayio.OverlayIOManager;
import io.overlayio.mongodb.MongoDBOverlayIOManager;
import io.structureio.StructureIOManager;
import io.structureio.mongodb.MongoDBStructureIOManager;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;

public class MongoDBIOController implements IOController {
    private MongoClient mongoClient;
    private ComponentIOManager componentIOManager;
    private StructureIOManager structureIOManager;
    private OverlayIOManager overlayIOManager;
    private AdminManager adminManager;
    private DBLock dbLock;

    private void attemptAuthentication(){
        try {
            Pair<String, String> credentialStrings = LoginDialog.showDialog();
            if (credentialStrings == null){
                return;
            }
            MongoCredential credential = MongoCredential.createCredential(credentialStrings.getKey(),
                    "UDT",
                    credentialStrings.getValue().toCharArray());
            mongoClient = new MongoClient(new ServerAddress(SettingsHandler.getSetting("mongod_ip"), Integer.parseInt(SettingsHandler.getSetting("mongod_port"))), credential, MongoClientOptions.builder().build());
            // setup if the database authenticated properly
            componentIOManager = new MongoDBComponentIOManager(mongoClient);
            structureIOManager = new MongoDBStructureIOManager(mongoClient);
            overlayIOManager = new MongoDBOverlayIOManager(mongoClient);
            dbLock = new MongoDBLock(mongoClient);
        }catch (MongoSecurityException e){
            // -4 is error authenticating
            if (e.getCode()==-4){
                new Alert(Alert.AlertType.ERROR, "Authentication failed!", ButtonType.OK).showAndWait();
                attemptAuthentication();
            }else {
                e.printStackTrace();
            }
        }
    }
    public MongoDBIOController(){
        adminManager = new MongoDBAdminManager();
        attemptAuthentication();
    }

    @Override
    public ComponentIOManager getComponentIOManager() {
        return componentIOManager;
    }

    @Override
    public StructureIOManager getStructureIOManager() {
        return structureIOManager;
    }

    @Override
    public OverlayIOManager getOverlayIOManager() {
        return overlayIOManager;
    }

    @Override
    public AdminManager getAdminManager(){
        return adminManager;
    }

    @Override
    public DBLock getDBLock(){
        return dbLock;
    }

    @Override
    public void close() throws IOException {
        dbLock.unlockAll();
        mongoClient.close();
    }
}
