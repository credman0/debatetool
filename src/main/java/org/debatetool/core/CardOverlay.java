package org.debatetool.core;


import gnu.trove.list.array.TShortArrayList;
import gnu.trove.list.array.TByteArrayList;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class CardOverlay implements Serializable {
    public static final byte HIGHLIGHT = 0x1<<0;
    public static final byte UNDERLINE = 0x1<<1;

    private String name;
    private TShortArrayList overlayPositions;
    private TByteArrayList overlayTypes;

    public CardOverlay(String name, byte[] overlayPositions, byte[] overlayTypes){
        this.name = name;
        this.overlayPositions = new TShortArrayList(fromBytes(overlayPositions));
        this.overlayTypes = new TByteArrayList(overlayTypes);
    }

    public CardOverlay(String name){
        this.name = name;
        this.overlayPositions = new TShortArrayList();
        this.overlayTypes = new TByteArrayList();
    }

    public TShortArrayList getOverlayPositions() {
        return overlayPositions;
    }

    public TByteArrayList getOverlayTypes(){
        return overlayTypes;
    }

    public String generateHTML(String plainText){
        StringBuilder htmlBuilder = new StringBuilder();
        int position = 0;
        for (int i = 0; i < overlayPositions.size(); i++){
            byte type = overlayTypes.get(i);
            boolean highlight = (type&HIGHLIGHT)!=0;
            boolean underline = (type&UNDERLINE)!=0;
            if (highlight){
                htmlBuilder.append("<h>");
            }
            if (underline){
                htmlBuilder.append("<u>");
            }
            htmlBuilder.append(plainText.substring(position,position+overlayPositions.get(i)));
            if (underline){
                htmlBuilder.append("</u>");
            }
            if (highlight){
                htmlBuilder.append("</h>");
            }
            position+=overlayPositions.get(i);
        }
        htmlBuilder.append(plainText.substring(position));
        return sanitizeHTML(htmlBuilder.toString());
    }

    private String sanitizeHTML(String html){
        return html.replaceAll("\\\"", "&quot;");
    }

    public void updateOverlay(int start, int end, byte overlayType){
        int position = 0;
        int overlayIndex = 0;
        while (position<start){
            if (overlayIndex<overlayPositions.size()){
                position+=Short.toUnsignedInt(overlayPositions.get(overlayIndex));
                overlayIndex++;
            }else{
                if (start-position <= (1<<(Short.SIZE+1)-1)){
                    int distance = (start-position);
                    addOverlay((short) distance);
                    position+=distance;
                }else{
                    int distance = (1<<(Short.SIZE+1)-1);
                    addOverlay((short) distance);
                    position+=distance;
                }
                overlayIndex++;
            }
        }
        if (position>start) {
            // in this case we have gone past the start, and we need to at least split the previous element

            // calculate the absolute starting position of the previous element
            int splitPosition = position-overlayPositions.get(overlayIndex-1);

            // now subtract from where we want to start
            splitPosition = start - splitPosition;

            // the result is the relative distance from the start of the element where we want a split

            splitOverlay(overlayIndex-1, splitPosition);

            // now our overlayIndex points to our first element, as it should, but we need to correct the position
            position = start;
        }

        // at this point, we expect the overlayIndex to point to an element within our range, and position to be at
        // our range start point
        byte oldType = 0x0;
        while (position<end){
            if (overlayIndex < overlayPositions.size()){
                position+=overlayPositions.get(overlayIndex);
                oldType = overlayTypes.get(overlayIndex);
                overlayTypes.set(overlayIndex, (byte) (oldType|overlayType));
                overlayIndex++;
            }else{
                if (end-position <= (1<<(Short.SIZE+1)-1)){
                    int distance = (end-position);
                    addOverlay((short) distance,overlayType);
                    position+=distance;
                }else {
                    int distance = (1 << (Short.SIZE + 1) - 1);
                    addOverlay((short) distance,overlayType);
                    position += distance;
                    overlayIndex++;
                }
            }
        }

        // if we overshot, we need to split the end and revert the elements past our end to our old overlayType
        if (position>end){
            // calculate the absolute starting position of the previous element
            int splitPosition = position-overlayPositions.get(overlayIndex-1);

            // now subtract from where we want to end
            splitPosition = end - splitPosition;

            // the result is the relative distance from the start of the element where we want a split

            splitOverlay(overlayIndex-1, splitPosition);

            overlayTypes.set(overlayIndex, oldType);
        }
    }

    public static CardOverlay combineOverlays(CardOverlay... overlays){
        // TODO do a better job of this
        CardOverlay combinedOverlay = new CardOverlay("");
        for (CardOverlay overlay:overlays){
            if (overlay == null){
                continue;
            }
            int position = 0;
            for (int i = 0; i < overlay.overlayPositions.size(); i++){
                short overlayPosition = overlay.overlayPositions.get(i);
                byte overlayType = overlay.overlayTypes.get(i);
                combinedOverlay.updateOverlay(position,position+overlayPosition,overlayType);
                position+=overlayPosition;
            }
        }
        return combinedOverlay;
    }

    private void splitOverlay(int index, int position){
        int originalElement = Short.toUnsignedInt(overlayPositions.get(index));
        int firstElement = position;
        int secondElement = originalElement-position;
        byte type = overlayTypes.get(index);

        overlayPositions.replace(index, (short) secondElement);
        overlayPositions.insert(index, (short) firstElement);

        overlayTypes.insert(index, type);
    }

    private void addOverlay(short length, byte type){
        overlayPositions.add(length);
        overlayTypes.add(type);
    }

    private void addOverlay(short length){
        addOverlay(length, (byte) 0x0);
    }

    private short[] fromBytes(byte[] bytes){
        short[] shorts = new short[bytes.length/2];
        ByteBuffer.wrap(bytes).asShortBuffer().get(shorts);
        return shorts;
    }

    private byte[] fromShorts(short[] shorts){
        byte[] bytes = new byte[shorts.length*2];
        ByteBuffer.wrap(bytes).asShortBuffer().put(shorts);
        return bytes;
    }

    public byte[] getOverlayPositionBytes() {
        return fromShorts(overlayPositions.toArray());
    }
    public byte[] getOverlayTypeBytes() {
        return overlayTypes.toArray();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString(){
        return getName();
    }
}
