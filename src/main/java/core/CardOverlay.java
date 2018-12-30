package core;

public class CardOverlay {
    public static final byte HIGHLIGHT = 0x1<<0;
    public static final byte UNDERLINE = 0x1<<1;

    protected byte[] overlayPositions;
    protected byte[] overlayType;
}
