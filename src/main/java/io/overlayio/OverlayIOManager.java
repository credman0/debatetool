package io.overlayio;

import core.CardOverlay;

import java.io.Closeable;
import java.util.List;

public interface OverlayIOManager extends Closeable, AutoCloseable {
    List<CardOverlay> getOverlays(byte[] cardHash);
    void saveOverlays(byte[] cardHash, List<CardOverlay> overlays);
}
