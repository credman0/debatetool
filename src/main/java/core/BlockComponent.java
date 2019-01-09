package core;

import io.IOUtil;

import java.io.IOException;
import java.io.Serializable;

public interface BlockComponent extends Serializable {
    /**
     * The actual html content to be sent to the view when the block is presented
     * @return
     */
    String getDisplayContent();

    /**
     * The string stored in mongo when the block is stored, so the content can be restored
     * @return
     */
    String getBlockStorageString();
    void load() throws IOException;
    static BlockComponent importFromData(String type, String storageString){
        if (type.equals(Card.class.getName())){
            // empty card with hash used to dynamically load it later
            return new Card(IOUtil.decodeString(storageString));
        }else if (type.equals(Analytic.class.getName())){
            return new Analytic(storageString);
        }else{
            throw new IllegalArgumentException("Unrecognized type: " + type);
        }
    }

    String getLabel();
}
