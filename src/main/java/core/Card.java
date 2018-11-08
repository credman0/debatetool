package core;

import io.IOUtil;

import javax.imageio.IIOException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Card {
    protected Cite cite;
    protected String text;
    protected byte[] hash = null;
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

    protected byte[] generateHash(){MessageDigest dg = null;
        try {
            dg = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return dg.digest((text+cite.toString()).getBytes(StandardCharsets.UTF_8));
    }

    public byte[] getHash(){
        if (hash == null){
            hash = generateHash();
        }
        return hash;
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
