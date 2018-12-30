package core;

public class CardOverlay {
    public static final byte HIGHLIGHT = 0x1<<0;
    public static final byte UNDERLINE = 0x1<<1;

    private byte[] overlayPositions;
    private byte[] overlayType;

    public CardOverlay(byte[] overlayPositions, byte[] overlayType){
        this.overlayPositions = overlayPositions;
        this.overlayType = overlayType;
    }

    public byte[] getOverlayPositions() {
        return overlayPositions;
    }

    public void setOverlayPositions(byte[] overlayPositions) {
        this.overlayPositions = overlayPositions;
    }

    public byte[] getOverlayType() {
        return overlayType;
    }

    public void setOverlayType(byte[] overlayType) {
        this.overlayType = overlayType;
    }
}
