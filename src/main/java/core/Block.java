package core;

import java.util.ArrayList;

public class Block extends SpeechComponent {
    @Override
    public byte[] getHash() {
        return new byte[0];
    }


    @Override
    public ArrayList<String>[] toLabelledLists() {
        return new ArrayList[0];
    }

    @Override
    public void importFromLabelledLists(ArrayList<String> labels, ArrayList<String> values) {

    }

    @Override
    public String getHashedString() {
        return null;
    }
}
