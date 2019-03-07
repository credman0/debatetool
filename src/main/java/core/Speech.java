package core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Speech extends HashIdentifiedSpeechComponent{
    private List<String> path;
    private String name;
    private ArrayList<SpeechComponent> contents;
    private boolean loaded;

    public Speech(List<String> path, String name){
        this.path = path;
        this.name = name;
        contents = new ArrayList<>();
    }

    public Speech(List<String> path){
        this.path = path;
        contents = new ArrayList<>();
    }

    public String getDisplayContent(){
        StringBuilder contentsBuilder = new StringBuilder();
        for (int i = 0; i < contents.size(); i++) {
            contentsBuilder.append("<p>"+(i)+"</p>");
            SpeechComponent component = contents.get(i);
            contentsBuilder.append(component.getDisplayContent());
        }
        return contentsBuilder.toString();
    }

    @Override
    public String getStorageString() {
        return null;
    }

    public boolean isLoaded(){
        return loaded;
    }

    public void load() throws IOException {
        for (SpeechComponent content:contents){
            content.load();
        }
        loaded = true;
    }

    public void clearContents(){contents.clear();}

    public void addComponent(SpeechComponent component){
        contents.add(component);
        setModified(true);
    }

    public int size(){
        return contents.size();
    }

    @Override
    public long getTimeStamp() {
        return 0;
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
        for (SpeechComponent component:contents){
            labelledLists[0].add(component.getClass().getName());
            labelledLists[1].add(component.getStorageString());
        }
        return labelledLists;
    }

    @Override
    public void importFromLabelledLists(ArrayList<String> labels, ArrayList<String> values) {
        this.name = values.get(0);
        for (int i = 0; i < labels.size(); i++){
            try {
                contents.add(SpeechComponent.importFromData(labels.get(i),values.get(i+1)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getHashedString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.join("/"+path.size(),path));
        builder.append(name);
        return builder.toString();
    }

    @Override
    public HashIdentifiedSpeechComponent clone() {
        Speech clone =  new Speech(path, name);
        // copy the list
        clone.contents.addAll(contents);
        clone.loaded = loaded;
        clone.hash = hash;
        return clone;
    }

    public SpeechComponent getComponent(int i) {
        return contents.get(i);
    }
}
