package io.iocontrollers.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import gui.SettingsHandler;
import io.componentio.ComponentIOManager;
import io.componentio.mongodb.MongoDBComponentIOManager;
import io.iocontrollers.IOController;
import io.overlayio.OverlayIOManager;
import io.overlayio.mongodb.MongoDBOverlayIOManager;
import io.structureio.StructureIOManager;
import io.structureio.mongodb.MongoDBStructureIOManager;

import java.io.IOException;
import java.util.ArrayList;

public class MongoDBIOController implements IOController {
    MongoClient mongoClient;
    private ComponentIOManager componentIOManager;
    private StructureIOManager structureIOManager;
    private OverlayIOManager overlayIOManager;

    public MongoDBIOController(){
        // TODO add handling for missing/wrong credentials
        String username = SettingsHandler.getSetting("username");
        char[] password = SettingsHandler.getSetting("password").toCharArray();
        MongoCredential credential = MongoCredential.createCredential(username,
                "UDT",
                password);
        ArrayList<MongoCredential> credentialList = new ArrayList<>();
        credentialList.add(credential);
        mongoClient = new MongoClient(new ServerAddress(SettingsHandler.getSetting("mongod_ip"), Integer.parseInt(SettingsHandler.getSetting("mongod_port"))),credentialList);
        componentIOManager = new MongoDBComponentIOManager(mongoClient);
        structureIOManager = new MongoDBStructureIOManager(mongoClient);
        overlayIOManager = new MongoDBOverlayIOManager(mongoClient);
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
    public void close() throws IOException {
        mongoClient.close();
    }
}
