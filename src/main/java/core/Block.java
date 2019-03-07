package core;

import io.IOUtil;

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
            contentsBuilder.append(component.getDisplayContent() + "<br>");
        }
        return contentsBuilder.toString();
    }

    @Override
    public String getStorageString() {
        return IOUtil.encodeString(getHash());
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
        contents.add(component);
        setModified(true);
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
    public void load() throws IOException {
        for (SpeechComponent component:contents){
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

    public List<String> getPath() {
        return path;
    }
}
