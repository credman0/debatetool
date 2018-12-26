package core;

import core.blockcontents.BlockComponent;
import io.componentio.ComponentIOManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.ArrayList;

public class Block extends SpeechComponent {
    protected ObservableList<BlockComponent> contents;
    protected boolean loaded = false;


    public Block (){
        contents = FXCollections.observableArrayList();
    }

    public void addComponent(BlockComponent component){
        contents.add(component);
    }

    public BlockComponent getComponent(int i){
        return contents.get(i);
    }

    @Override
    public ArrayList<String>[] toLabelledLists() {
        ArrayList<String>[] labelledLists = new ArrayList[2];
        for (BlockComponent component:contents){
            labelledLists[0].add(component.getClass().getName());
            labelledLists[1].add(component.getBlockStorageString());
        }
        return labelledLists;
    }

    @Override
    public void importFromLabelledLists(ArrayList<String> labels, ArrayList<String> values) {
        for (int i = 0; i < labels.size(); i++){
            contents.add(BlockComponent.importFromData(labels.get(i),values.get(i)));
        }
    }

    @Override
    public void load(ComponentIOManager manager) throws IOException {
        for (BlockComponent component:contents){
            component.loadExternal(manager);
        }
        loaded = true;
    }

    @Override
    public String getHashedString() {
        return null;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }
}
