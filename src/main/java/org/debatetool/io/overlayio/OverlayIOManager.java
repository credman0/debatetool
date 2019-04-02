package org.debatetool.io.overlayio;

import org.debatetool.core.CardOverlay;
import org.bson.types.Binary;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;

public interface OverlayIOManager extends Closeable, AutoCloseable {
    HashMap<String,List<CardOverlay>> getOverlays(byte[] cardHash);

    void saveOverlays(byte[] cardHash, List<CardOverlay> overlays, String type);
    HashMap<Binary, HashMap<String,List<CardOverlay>>> getAllOverlays(List<byte[]> cardHashes);
}
