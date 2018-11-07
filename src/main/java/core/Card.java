package core;

import io.IOUtil;

import javax.imageio.IIOException;
import java.io.*;

public class Card {
    protected Cite cite;
    protected String text;
    protected int hash = 0;
    /**
     * The time the card text was last modified.
     */
    protected long timeStamp;

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
        hash = 0;
    }

    public void setCite(String author, String date, String additionalInfo){
        this.cite = new Cite(author, date, additionalInfo);
        // set the hash to be recalculated
        hash = 0;
    }

    public String getText() {
        return text;
    }

    public void setText(String text){
        this.text = text;
        formatText();
        timeStamp = System.currentTimeMillis();
        // set the hash to be recalculated
        hash = 0;
    }

    public void writeToOutput(DataOutput out) throws IOException {
        out.writeInt(hash);
        out.writeLong(timeStamp);
        cite.writeToOutput(out);
        IOUtil.writeSerializeString(text,out);
        // write null terminating byte
        out.writeByte(0);
    }

    public void loadFromInput(DataInput in, boolean checkHash) throws IOException{
        hash = in.readInt();
        timeStamp = in.readLong();
        cite = new Cite(in);
        text = IOUtil.readDeserializeString(in);
        byte nullTerm = in.readByte();
        if (nullTerm!=0){
            throw new IIOException("Card missing null terminator");
        }
        if (checkHash){
            int validHash = generateHash();
            if (hash!=validHash){
                hash = validHash;
                throw new IIOException("Hash validation failed for card load");
            }
        }
    }

    // adapted from String.hashCode()
    protected int generateHash(){
        int h = 0;
        for (int i = 0; i < text.length(); i++) {
            h = 31 * h + text.charAt(i);
        }
        for (int i = 0; i < getCite().getAuthor().length(); i++) {
            h = 31 * h + getCite().getAuthor().charAt(i);
        }
        for (int i = 0; i < getCite().getDate().length(); i++) {
            h = 31 * h + getCite().getDate().charAt(i);
        }
        for (int i = 0; i < getCite().getAdditionalInfo().length(); i++) {
            h = 31 * h + getCite().getAdditionalInfo().charAt(i);
        }
        return h;
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = generateHash();
        }
        return hash;
    }

    protected void formatText(){
        text = text.replaceAll("\n", "");
        text = text.replaceAll("\0", "");
    }
}
