package org.debatetool.core;

import org.debatetool.io.IOUtil;
import org.debatetool.io.iocontrollers.IOController;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import javax.imageio.IIOException;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Card extends HashIdentifiedSpeechComponent implements StateRecoverableComponent {
    /**
     * "tags" here used in the debate sense
     */
    protected List<String> tags = new ArrayList<>();
    protected int tagIndex = 0;
    protected Cite cite;
    protected String text;
    private int preferredHighlightIndex = 0;
    private int preferredUnderlineIndex = 0;
    private CardOverlay loadedOverlay = null;

    private List<CardOverlay> underlining;
    private List<CardOverlay> highlighting;

    public List<CardOverlay> getUnderlining() {
        if (underlining==null){
            loadOverlay();
        }
        return underlining;
    }

    public List<CardOverlay> getHighlighting() {
        if (highlighting==null){
            loadOverlay();
        }
        return highlighting;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * The time the card text was last modified.
     */
    protected long timeStamp;

    private Card(){

    }

    public Card(byte[] hash){
        this();
        this.hash = hash;
    }

    public Card(Cite cite, String text) {
        this();
        setCite(cite);
        setText(text);
    }

    public Card(DataInputStream in) throws IOException {
        this();
        loadFromInput(in, true);
    }

    public Cite getCite() {
        return cite;
    }

    public void setCite(Cite cite) {
        this.cite = cite;
        setModified(true);
    }

    public void setCite(String author, String date, String additionalInfo){
        this.cite = new Cite(author, date, additionalInfo);
        setModified(true);
    }

    public String getText() {
        return text;
    }

    public void setText(String text){
        this.text = text;
        formatText();
        timeStamp = System.currentTimeMillis();
        setModified(true);
    }

    public void writeToOutput(DataOutput out) throws IOException {
        out.write(getHash());
        out.writeLong(timeStamp);
        cite.writeToOutput(out);
        IOUtil.writeSerializeString(text,out);
        // write null terminating byte
        out.writeByte(0);
    }

    public void loadFromInput(DataInput in, boolean checkHash) throws IOException{
        hash = new byte[16];
        in.readFully(hash);
        timeStamp = in.readLong();
        cite = new Cite(in);
        text = IOUtil.readDeserializeString(in);
        byte nullTerm = in.readByte();
        if (nullTerm!=0){
            throw new IIOException("Card missing null terminator");
        }
        if (checkHash){
            byte[] validHash = generateHash();
            if (!Arrays.equals(hash,validHash)){
                hash = validHash;
                throw new IIOException("Hash validation failed for card load");
            }
        }
    }

    @Override
    public String getLabel() {
        return getActiveTag()+"\n"+getCite().author+" "+getCite().getDate();
    }

    @Override
    public ArrayList<String>[] toLabelledLists() {
        ArrayList<String>[] labelledLists = new ArrayList[2];
        labelledLists[0] = new ArrayList<>(5);
        labelledLists[1] = new ArrayList<>(5+tags.size());

        labelledLists[0].add("Author");
        labelledLists[0].add("Date");
        labelledLists[0].add("Info");
        labelledLists[0].add("Text");
        labelledLists[0].add("Timestamp");

        labelledLists[1].add(getCite().getAuthor());
        labelledLists[1].add(getCite().getDate());
        labelledLists[1].add(getCite().getAdditionalInfo());
        labelledLists[1].add(getText());
        try {
            labelledLists[1].add(new String(IOUtil.longToBytes(timeStamp),"IBM437"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (String tag:tags){
            labelledLists[1].add(tag);
        }

        return labelledLists;
    }

    @Override
    public void importFromLabelledLists(ArrayList<String> labels, ArrayList<String> values) {
        String author = values.get(0);
        String date = values.get(1);
        String info = values.get(2);
        String text = values.get(3);
        String timestampString = values.get(4);
        try {
            timeStamp = IOUtil.bytesToLong(timestampString.getBytes("IBM437"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        setCite(author,date,info);
        // update text without changing timestamp
        this.text = text;
        // now the rest should be tags
        for (int i = 5; i < values.size(); i++){
            tags.add(values.get(i));
        }
    }

    public int getTagIndex(){
        return tagIndex;
    }

    public void setTagIndex(int i){
        tagIndex = i;
    }

    public String getActiveTag(){
        if (tags.isEmpty()){
            return "<empty>";
        }
        return tags.get(tagIndex);
    }

    public String getTag(int i){
        return tags.get(i);
    }

    public List<String> getTags(){
        ObservableList<String> observableTags = FXCollections.observableList(tags);
        observableTags.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> change) {
                setModified(true);
            }
        });
        return observableTags;
    }

    public void setTags(List<String> tags){
        this.tags.clear();
        this.tags.addAll(tags);
        setModified(true);
    }

    /**
     * adds a tag but only if it does not already exist
     * @param tag
     */
    public void addTag(String tag){
        if (!tags.contains(tag)){
            tags.add(tag);
        }
        setModified(true);
    }

    @Override
    public String getDisplayContent() {
        if (text == null){
            throw new IllegalStateException("Attempted to display card before loading");
        }
        if (loadedOverlay==null){
            loadOverlay();
        }
        return getCite().getDisplayContent()+"<br>"+loadedOverlay.generateHTML(getText());
    }

    private void loadOverlay(){
        HashMap<String, List<CardOverlay>> overlayMap = IOController.getIoController().getOverlayIOManager().getOverlays(getHash());
        assignOverlaysFromMap(overlayMap);
    }

    public void assignOverlaysFromMap(HashMap<String, List<CardOverlay>> overlayMap){
        underlining = overlayMap.get("Underline");
        highlighting = overlayMap.get("Highlight");
        if (underlining == null){
            underlining = new ArrayList<>();
        }
        if (highlighting == null){
            highlighting = new ArrayList<>();
        }
        CardOverlay underline = null;
        CardOverlay highlight = null;
        if (!underlining.isEmpty()){
            underline = underlining.get(getPreferredUnderlineIndex());
        }
        if (!highlighting.isEmpty()){
            highlight = highlighting.get(getPreferredHighlightIndex());
        }
        loadedOverlay = CardOverlay.combineOverlays(underline, highlight);
    }

    @Override
    public HashIdentifiedSpeechComponent clone() {
        // cite is already read-only, so no need to clone it
        Card clone = new Card(getCite(), getText());
        clone.tags.addAll(tags);
        clone.tagIndex = tagIndex;
        clone.loadedOverlay = loadedOverlay;
        clone.timeStamp = timeStamp;
        clone.preferredHighlightIndex = preferredHighlightIndex;
        clone.preferredUnderlineIndex = preferredUnderlineIndex;
        return clone;
    }

    @Override
    public String getStorageString() {
        return IOUtil.encodeString(getHash());
    }

    @Override
    public String getStateString() {
        return getTagIndex() + ":" + getPreferredUnderlineIndex() + ":" + getPreferredHighlightIndex();
    }

    @Override
    public void restoreState(String stateString){
        String[] states = stateString.split(":");
        tagIndex = Integer.parseInt(states[0]);
        preferredUnderlineIndex = Integer.parseInt(states[1]);
        preferredHighlightIndex = Integer.parseInt(states[2]);
    }

    @Override
    public void load() throws IOException {
        Card self = (Card) IOController.getIoController().getComponentIOManager().retrieveSpeechComponent(hash);
        // TODO maybe a better way to import this information
        setTo(self);
    }

    public void setTo(Card card){
        this.text = card.text;
        this.cite = card.cite;
        this.timeStamp = card.timeStamp;
        this.tags = card.tags;

    }

    @Override
    public String getHashString() {
        return text+cite.toString();
    }

    @Override
    public boolean isLoaded() {
        return !(text==null);
    }


    @Override
    public int hashCode() {
        return Arrays.hashCode(getHash());
    }

    protected void formatText(){
        text = cleanForCard(text);
    }

    /**
     * removes illegal characters (new lines and null bytes) from the string, then returns it
     * @param s any string
     * @return s, minus illegal characters for cards
     */
    public static String cleanForCard(String s){
        return s.replaceAll("\n", "").replaceAll("\0", "");
    }

    public int getPreferredUnderlineIndex() {
        return preferredUnderlineIndex;
    }

    public void setPreferredUnderlineIndex(int preferredUnderlineIndex) {
        this.preferredUnderlineIndex = preferredUnderlineIndex;
        loadedOverlay = null;
    }

    public int getPreferredHighlightIndex() {
        return preferredHighlightIndex;
    }

    public void setPreferredHighlightIndex(int preferredHighlightIndex) {
        this.preferredHighlightIndex = preferredHighlightIndex;
        loadedOverlay = null;
    }
}
