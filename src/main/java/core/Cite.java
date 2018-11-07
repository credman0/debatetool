package core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Cite {
    protected String author;
    protected String date;
    protected String additionalInfo;

    public Cite(String author, String date, String additionalInfo) {
        this.author = author;
        this.date = date;
        this.additionalInfo = additionalInfo;
    }

    /**
     * Loads the cite from the given DataInput, given that the DataInput is pointed at the beginning of a serialized
     * cite.
     * @param in
     */
    public Cite (DataInput in) throws IOException {
        loadFromInput(in);
    }

    public String getAuthor() {
        return author;
    }
    public String getDate() {
        return date;
    }
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void writeToOutput(DataOutput out) throws IOException {
        out.writeUTF(author);
        out.writeUTF(date);
        out.writeUTF(additionalInfo);
    }

    public void loadFromInput(DataInput in) throws IOException{
        author = in.readUTF();
        date = in.readUTF();
        additionalInfo = in.readUTF();
    }

    public String toString(){
        return getAuthor()+getDate();
    }
}
