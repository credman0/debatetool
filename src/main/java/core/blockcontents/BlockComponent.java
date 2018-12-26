package core.blockcontents;

import io.componentio.ComponentIOManager;

import java.io.IOException;

public interface BlockComponent {
    String getDisplayContent();
    String getBlockStorageString();
    void loadExternal(ComponentIOManager manager) throws IOException;
    public static BlockComponent importFromData(String type, String storageString){
        if (type.equals(BlockCardPlaceholder.class.getName())){
            return new BlockCardPlaceholder(storageString);
        }else if (type.equals(BlockAnalytic.class.getName())){
            return new BlockAnalytic(storageString);
        }else{
            throw new IllegalArgumentException("Unrecognized type: " + type);
        }
    }
}
