package io.iocontrollers;

import io.componentio.ComponentIOManager;
import io.iocontrollers.mongodb.MongoDBIOController;
import io.overlayio.OverlayIOManager;
import io.structureio.StructureIOManager;

public interface IOController {
    IOController ioController = new MongoDBIOController();

    static IOController getIoController(){
        return ioController;
    }

    ComponentIOManager getComponentIOManager();
    StructureIOManager getStructureIOManager();
    OverlayIOManager getOverlayIOManager();
}
