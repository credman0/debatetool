package core;

import io.IOUtil;

import java.io.IOException;

public interface BlockComponent {
    /**
     * The actual content to be sent to the screen when the block is presented
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
            return new Card(IOUtil.decodeString(storageString));
        }else if (type.equals(Analytic.class.getName())){
            return new Analytic(storageString);
        }else{
            throw new IllegalArgumentException("Unrecognized type: " + type);
        }
    }
}
