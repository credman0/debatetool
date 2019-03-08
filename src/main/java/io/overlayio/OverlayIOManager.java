package io.overlayio;

import core.CardOverlay;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;

public interface OverlayIOManager extends Closeable, AutoCloseable {
    HashMap<String,List<CardOverlay>> getOverlays(byte[] cardHash);

    void saveOverlays(byte[] cardHash, List<CardOverlay> overlays, String type);
}
