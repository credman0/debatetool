package core.blockcontents;

import core.Card;
import io.componentio.ComponentIOManager;

import java.io.IOException;

public class BlockCardPlaceholder implements BlockComponent {
    protected byte[] hash;
    protected Card cardContent;


    public BlockCardPlaceholder(String hashString){
        hash = hashString.getBytes();
    }

    @Override
    public String getDisplayContent() {
        if (cardContent == null){
            throw new IllegalStateException("Attempted to read card contents before loading");
        }else{
            return cardContent.getCite().toString() + "\n" + cardContent.getText();
        }
    }

    @Override
    public String getBlockStorageString() {
        return new String(hash);
    }

    @Override
    public void loadExternal(ComponentIOManager manager) throws IOException {
        cardContent = (Card) manager.retrieveSpeechComponent(hash);
    }
}
