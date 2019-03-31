package io.iocontrollers;

import io.accounts.AdminManager;
import io.componentio.ComponentIOManager;
import io.iocontrollers.mongodb.MongoDBIOController;
import io.overlayio.OverlayIOManager;
import io.structureio.StructureIOManager;

import java.io.Closeable;

public interface IOController extends Closeable, AutoCloseable {
    IOController ioController = new MongoDBIOController();

    static IOController getIoController(){
        return ioController;
    }

    ComponentIOManager getComponentIOManager();
    StructureIOManager getStructureIOManager();
    OverlayIOManager getOverlayIOManager();

    AdminManager getAdminManager();
}
