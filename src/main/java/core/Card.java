package core;

import io.IOUtil;
import io.componentio.ComponentIOManager;

import javax.imageio.IIOException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class Card extends SpeechComponent {
    protected Cite cite;
    protected String text;
    /**
     * The time the card text was last modified.
     */
    protected long timeStamp;

    public Card(){

    }

    public Card(Cite cite, String text) {
        setCite(cite);
        setText(text);
    }

    public Card(DataInputStream in) throws IOException {
        loadFromInput(in, true);
    }

    public Cite getCite() {
        return cite;
    }

    public void setCite(Cite cite) {
        this.cite = cite;
        // set the hash to be recalculated
        hash = null;
    }

    public void setCite(String author, String date, String additionalInfo){
        this.cite = new Cite(author, date, additionalInfo);
        // set the hash to be recalculated
        hash = null;
    }

    public String getText() {
        return text;
    }

    public void setText(String text){
        this.text = text;
        formatText();
        timeStamp = System.currentTimeMillis();
        // set the hash to be recalculated
        hash = null;
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
    public ArrayList<String>[] toLabelledLists() {
        ArrayList<String>[] labelledLists = new ArrayList[2];
        labelledLists[0] = new ArrayList<>(5);
        labelledLists[1] = new ArrayList<>(5);

        labelledLists[0].add("Author");
        labelledLists[0].add("Date");
        labelledLists[0].add("Info");
        labelledLists[0].add("Text");
        labelledLists[0].add("Timestamp");

        labelledLists[1].add(getCite().getAuthor());
        labelledLists[1].add(getCite().getDate());
        labelledLists[1].add(getCite().getAdditionalInfo());
        labelledLists[1].add(getText());
        labelledLists[1].add(new String(IOUtil.longToBytes(timeStamp)));

        return labelledLists;
    }

    @Override
    public void importFromLabelledLists(ArrayList<String> labels, ArrayList<String> values) {
        String author = values.get(0);
        String date = values.get(1);
        String info = values.get(2);
        String text = values.get(3);
        String timestampString = values.get(4);
        timeStamp = IOUtil.bytesToLong(timestampString.getBytes());
        setCite(author,date,info);
        setText(text);
    }

    @Override
    public void load(ComponentIOManager manager) throws IOException {
        if (!isLoaded()){
            throw new UnsupportedOperationException("Dynamic card loading is not implemented");
        }
    }

    @Override
    public String getHashedString() {
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
        text = text.replaceAll("\n", "");
        text = text.replaceAll("\0", "");
    }
}
