package org.debatetool.core;

import org.debatetool.io.iocontrollers.IOController;

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
            contentsBuilder.append("<p><n>"+(i+1)+") </n>");
            SpeechComponent component = contents.get(i);
            if (component.getClass().isAssignableFrom(Card.class)){
                contentsBuilder.append("<n>"+((Card) component).getActiveTag() + "</n>");
            }
            contentsBuilder.append("</p>");
            contentsBuilder.append(component.getDisplayContent());
        }
        return contentsBuilder.toString();
    }

    @Override
    public String getStorageString() {
        return null;
    }

    @Override
    public String getStateString() {
        return null;
    }

    public boolean isLoaded(){
        return loaded;
    }

    public void load() throws IOException {
        IOController.getIoController().getComponentIOManager().loadAll(this);
        loaded = true;
    }

    public void clearContents(){contents.clear();}

    public void addComponent(SpeechComponent component){
        contents.add(component);
        setModified(true);
    }

    public void removeComponent(SpeechComponent component){
        contents.remove(component);
        setModified(true);
    }

    public void reload() throws IOException {
        // TODO more elegant fix than querying the database?
        setModified(true);
        Speech newSpeech = (Speech) IOController.getIoController().getComponentIOManager().retrieveSpeechComponent(getHash());
        loaded = false;
        this.contents = newSpeech.contents;
        load();
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
            String state = component.getStateString();
            if (state!=null){
                labelledLists[0].add("STATE");
                labelledLists[1].add(state);
            }
        }
        return labelledLists;
    }

    @Override
    public void importFromLabelledLists(ArrayList<String> labels, ArrayList<String> values) {
        this.name = values.get(0);
        for (int i = 0; i < labels.size(); i++){
            try {
                contents.add(SpeechComponent.importFromData(labels.get(i),values.get(i+1)));
                // check for optional state string
                if (i < labels.size()-1 && labels.get(i+1).equals("STATE")){
                    contents.get(contents.size()-1).restoreState(values.get(i+2));
                    i++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getHashString() {
        return HashIdentifiedSpeechComponent.getPositionalHashString(path, name);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setModified(true);
    }

    public List<String> getPath() {
        return path;
    }
}
