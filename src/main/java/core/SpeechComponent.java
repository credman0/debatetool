package core;

import io.IOUtil;
import io.iocontrollers.IOController;

import java.io.IOException;
import java.io.Serializable;

public abstract class SpeechComponent implements Serializable {
    public abstract void load() throws IOException;
    public abstract boolean isLoaded();
    public abstract String getDisplayContent();
    public abstract String getStorageString();
    public abstract SpeechComponent clone();
    public abstract String getLabel();

    public static SpeechComponent importFromData(String type, String storageString) throws IOException {
        if (type.equals(Block.class.getName())){
            // empty card with hash used to dynamically load it later
            return IOController.getIoController().getComponentIOManager().retrieveSpeechComponent(IOUtil.decodeString(storageString));
        }else if (type.equals(Card.class.getName())){
            // empty card with hash used to dynamically load it later
            return new Card(IOUtil.decodeString(storageString));
        }else if (type.equals(Analytic.class.getName())){
            return new Analytic(storageString);
        }else{
            throw new IllegalArgumentException("Unrecognized type: " + type);
        }
    }
}
