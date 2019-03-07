package io.iocontrollers.mongodb;

import com.mongodb.MongoClient;
import io.componentio.ComponentIOManager;
import io.componentio.mongodb.MongoDBComponentIOManager;
import io.iocontrollers.IOController;
import io.overlayio.OverlayIOManager;
import io.overlayio.mongodb.MongoDBOverlayIOManager;
import io.structureio.StructureIOManager;
import io.structureio.mongodb.MongoDBStructureIOManager;

import java.io.IOException;

public class MongoDBIOController implements IOController {
    MongoClient mongoClient = new MongoClient("127.0.0.1", 27017);
    private ComponentIOManager componentIOManager = new MongoDBComponentIOManager(mongoClient);
    private StructureIOManager structureIOManager = new MongoDBStructureIOManager(mongoClient);
    private OverlayIOManager overlayIOManager = new MongoDBOverlayIOManager(mongoClient);

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
