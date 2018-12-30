package io.iocontrollers;

import io.componentio.ComponentIOManager;
import io.overlayio.OverlayIOManager;
import io.structureio.StructureIOManager;

public interface IOController {
    ComponentIOManager getComponentIOManager();
    StructureIOManager getStructureIOManager();
    OverlayIOManager getOverlayIOManager();
}
