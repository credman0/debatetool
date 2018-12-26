package core;

import core.blockcontents.BlockComponent;
import io.componentio.ComponentIOManager;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.ArrayList;

public class Block extends SpeechComponent {
    protected ObservableList<BlockComponent> contents;
    protected boolean loaded = false;


    public Block (){
        contents = FXCollections.observableArrayList();
        contents.addListener(new ListChangeListener<BlockComponent>() {
            @Override
            public void onChanged(Change<? extends BlockComponent> change) {
                setModified(true);
            }
        });
    }

    public void addComponent(BlockComponent component){
        contents.add(component);
    }

    public BlockComponent getComponent(int i){
        return contents.get(i);
    }

    public int size(){
        return contents.size();
    }

    @Override
    public ArrayList<String>[] toLabelledLists() {
        ArrayList<String>[] labelledLists = new ArrayList[2];
        labelledLists[0] = new ArrayList<>(contents.size());
        labelledLists[1] = new ArrayList<>(contents.size());
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
        StringBuilder builder = new StringBuilder();
        // add some uniqueness in case all the contents are the same on blocks of different length
        builder.append(contents.size());
        for (BlockComponent component:contents){
            builder.append(component.getBlockStorageString());
        }
        return builder.toString();
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }
}
