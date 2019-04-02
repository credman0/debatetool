package org.debatetool.io.iocontrollers;

import org.debatetool.io.accounts.AdminManager;
import org.debatetool.io.accounts.DBLock;
import org.debatetool.io.componentio.ComponentIOManager;
import org.debatetool.io.iocontrollers.mongodb.MongoDBIOController;
import org.debatetool.io.overlayio.OverlayIOManager;
import org.debatetool.io.structureio.StructureIOManager;

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

    DBLock getDBLock();
}
