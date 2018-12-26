package core;

import io.componentio.ComponentIOManager;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public abstract class SpeechComponent {
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

    protected byte[] hash = null;
    public abstract ArrayList<String>[] toLabelledLists();
    public abstract void importFromLabelledLists(ArrayList<String> labels, ArrayList<String> values);
    public abstract void load(ComponentIOManager manager) throws IOException;
    public static SpeechComponent createFromLabelledLists(String type, ArrayList<String> labels, ArrayList<String> values){
        if (type.equals(Card.class.getName())){
            Card card = new Card();
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

    public abstract boolean isLoaded();
}
