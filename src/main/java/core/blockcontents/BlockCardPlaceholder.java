package core.blockcontents;

import core.Card;
import io.componentio.ComponentIOManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class BlockCardPlaceholder implements BlockComponent {
    protected byte[] hash;
    protected Card cardContent;


    public BlockCardPlaceholder(String hashString){
        try {
            hash = hashString.getBytes("IBM437");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public BlockCardPlaceholder (Card card){
        this.cardContent = card;
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
        try {
            return new String(getHash(), "IBM437");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] getHash(){
        if (cardContent==null) {
            return hash;
        }else{
            return cardContent.getHash();
        }
    }

    @Override
    public void loadExternal(ComponentIOManager manager) throws IOException {
        cardContent = (Card) manager.retrieveSpeechComponent(getHash());
    }
}
