package org.debatetool.core;

import org.debatetool.io.iocontrollers.IOController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public abstract class HashIdentifiedSpeechComponent extends SpeechComponent {
    private boolean modified = false;

    public boolean isModified() {
        return modified;
    }

    protected void setModified(boolean modified) {
        this.modified = modified;
        if (modified) {
            hash = null;
        }
    }

    public abstract long getTimeStamp();

    /**
     * A human readable label for the object to display in GUIs
     *
     * @return Human readable label representative of the object
     */
    public abstract String getLabel();

    protected byte[] hash = null;

    public abstract ArrayList<String>[] toLabelledLists();

    public abstract void importFromLabelledLists(ArrayList<String> labels, ArrayList<String> values);

    public static HashIdentifiedSpeechComponent createFromLabelledLists(String type, ArrayList<String> labels, ArrayList<String> values, byte[] hash) {
        if (type.equals(Card.class.getName())) {
            Card card = new Card((byte[]) null);
            card.importFromLabelledLists(labels, values);
            return card;
        } else if (type.equals(Block.class.getName())) {
            Block block = new Block(IOController.getIoController().getStructureIOManager().getBlockPath(hash));
            block.importFromLabelledLists(labels, values);
            return block;
        }else if (type.equals(Speech.class.getName())) {
            Speech speech = new Speech(IOController.getIoController().getStructureIOManager().getBlockPath(hash));
            speech.importFromLabelledLists(labels, values);
            return speech;
        } else {
            throw new IllegalArgumentException("Unrecognized type: " + type);
        }
    }

    public abstract String getHashString();

    protected byte[] generateHash() {
        return performHash(getHashString());
    }

    public final byte[] getHash() {
        if (hash == null) {
            hash = generateHash();
        }
        return hash;
    }

    public static byte[] performHash(String hashedString){
        MessageDigest dg = null;
        try {
            dg = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return dg.digest(hashedString.getBytes(StandardCharsets.UTF_8));
    }

    public static String getPositionalHashString(List<String> path, String name){
        StringBuilder builder = new StringBuilder();
        builder.append(String.join("/"+path.size(),path));
        builder.append(name);
        return builder.toString();
    }

    public abstract HashIdentifiedSpeechComponent clone();
}
