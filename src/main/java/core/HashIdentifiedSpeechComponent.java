package core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public abstract class HashIdentifiedSpeechComponent extends SpeechComponent{
    private boolean modified = false;
    public boolean isModified() {
        return modified;
    }

    protected void setModified(boolean modified) {
        this.modified = modified;
        if (modified){
            hash =null;
        }
    }
    public abstract long getTimeStamp();

    /**
     * A human readable label for the object to display in GUIs
     * @return Human readable label representative of the object
     */
    public abstract String getLabel();

    protected byte[] hash = null;
    public abstract ArrayList<String>[] toLabelledLists();
    public abstract void importFromLabelledLists(ArrayList<String> labels, ArrayList<String> values);
    public static HashIdentifiedSpeechComponent createFromLabelledLists(String type, ArrayList<String> labels, ArrayList<String> values){
        if (type.equals(Card.class.getName())){
            Card card = new Card((byte[])null);
            card.importFromLabelledLists(labels,values);
            return card;
        }else if (type.equals(Block.class.getName())) {
            Block block = new Block();
            block.importFromLabelledLists(labels, values);
            return block;
        }else{
            throw new IllegalArgumentException("Unrecognized type: " + type);
        }
    }
    public abstract String getHashedString();
    protected byte[] generateHash(){
        MessageDigest dg = null;
        try {
            dg = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return dg.digest(getHashedString().getBytes(StandardCharsets.UTF_8));
    }
    public final byte[] getHash(){
        if (hash == null){
            hash = generateHash();
        }
        return hash;
    }
}
