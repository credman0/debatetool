package org.debatetool.core;

import org.debatetool.io.IOUtil;
import org.debatetool.io.iocontrollers.IOController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Block extends HashIdentifiedSpeechComponent {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setModified(true);
    }

    private List<String> path;
    protected String name;
    private List <SpeechComponent> contents;
    protected boolean loaded = false;


    public Block(List<String> path, String name){
        this.path = path;
        this.name = name;
        contents = new ArrayList<>();
    }

    public Block(List<String> path){
        this(path, "");
    }

    public String getDisplayContent(){
        StringBuilder contentsBuilder = new StringBuilder();
        for (int i = 0; i < contents.size(); i++) {
            contentsBuilder.append("<n>"+toAlphabet(i) + ") </n>");
            SpeechComponent component = contents.get(i);
            if (component.getClass().isAssignableFrom(Card.class)){
                contentsBuilder.append("<n>"+((Card) component).getActiveTag() + "</n><br>");
            }
            contentsBuilder.append(component.getDisplayContent() + "<br>");
        }
        return contentsBuilder.toString();
    }

    @Override
    public String getStorageString() {
        return IOUtil.encodeString(getHash());
    }

    @Override
    public String getStateString() {
        return null;
    }

    @Override
    public HashIdentifiedSpeechComponent clone() {
        Block clone =  new Block(path, name);
        // copy the list
        clone.contents.addAll(contents);
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

    public void addComponent(SpeechComponent component){
        if (component.getClass().isAssignableFrom(Speech.class) || component.getClass().isAssignableFrom(Block.class)){
            throw new IllegalArgumentException("Attempted to add component of illegal type: " + component.getClass());
        }
        contents.add(component);
        setModified(true);
    }

    public void removeComponent(SpeechComponent component){
        contents.remove(component);
        setModified(true);
    }

    public void removeComponent(int index){
        contents.remove(index);
        setModified(true);
    }

    public void insertComponentAbove(SpeechComponent component1, SpeechComponent toInsert){
        int index = contents.indexOf(component1);
        if (index>=0){
            contents.add(index, toInsert);
        }
    }

    public SpeechComponent getComponent(int i){
        return contents.get(i);
    }

    public void clearContents(){
        contents.clear();
        setModified(true);
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
    public void load() throws IOException {
        IOController.getIoController().getComponentIOManager().loadAll(this);
        loaded = true;
    }
    @Override
    public String getHashString() {
        return HashIdentifiedSpeechComponent.getPositionalHashString(path,name);
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }

    public List<String> getPath() {
        return path;
    }
}
