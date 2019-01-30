package core;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Block extends HashIdentifiedSpeechComponent {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private List<String> path;
    protected String name;
    protected ObservableList<BlockComponent> contents;
    protected boolean loaded = false;


    public Block(List<String> path, String name){
        this.path = path;
        this.name = name;
        contents = FXCollections.observableArrayList();
        contents.addListener(new ListChangeListener<BlockComponent>() {
            @Override
            public void onChanged(Change<? extends BlockComponent> change) {
                setModified(true);
            }
        });
    }

    public Block(List<String> path){
        this(path, "");
    }

    public String getDisplayContent(){
        StringBuilder contentsBuilder = new StringBuilder();
        for (int i = 0; i < contents.size(); i++) {
            contentsBuilder.append("<n>"+toAlphabet(i) + ") </n>");
            BlockComponent component = contents.get(i);
            contentsBuilder.append(component.getDisplayContent() + "<br>");
        }
        return contentsBuilder.toString();
    }

    @Override
    public HashIdentifiedSpeechComponent clone() {
        Block clone =  new Block(path, name);
        // copy the list
        clone.contents = FXCollections.observableArrayList(contents);
        clone.contents.addListener(new ListChangeListener<BlockComponent>() {
            @Override
            public void onChanged(Change<? extends BlockComponent> change) {
                clone.setModified(true);
            }
        });
        clone.loaded = loaded;
        clone.hash = hash;
        return clone;
    }

    /**
     * convert an integer to an alphabetic index (a,b,...aa,ab,etc)
     * @param i
     * @return
     */
    public static String toAlphabet(int i){
        if (i<0){
            return "";
        }else {
            return toAlphabet((i / 26) - 1) + (char)(65 + i % 26);
        }
    }

    public void addComponent(BlockComponent component){
        contents.add(component);
    }

    public BlockComponent getComponent(int i){
        return contents.get(i);
    }

    public void clearContents(){
        contents.clear();
    }

    public int size(){
        return contents.size();
    }

    @Override
    public String getLabel() {
        return name;
    }

    @Override
    public ArrayList<String>[] toLabelledLists() {
        ArrayList<String>[] labelledLists = new ArrayList[2];
        labelledLists[0] = new ArrayList<>(contents.size());
        labelledLists[1] = new ArrayList<>(contents.size());
        labelledLists[1].add(name);
        for (BlockComponent component:contents){
            labelledLists[0].add(component.getClass().getName());
            labelledLists[1].add(component.getBlockStorageString());
        }
        return labelledLists;
    }

    @Override
    public void importFromLabelledLists(ArrayList<String> labels, ArrayList<String> values) {
        this.name = values.get(0);
        for (int i = 0; i < labels.size(); i++){
            contents.add(BlockComponent.importFromData(labels.get(i),values.get(i+1)));
        }
    }

    @Override
    public void load() throws IOException {
        for (BlockComponent component:contents){
            component.load();
        }
        loaded = true;
    }

    @Override
    public String getHashedString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.join("/"+path.size(),path));
        builder.append(name);
        return builder.toString();
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }
}
