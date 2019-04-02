package org.debatetool.core;

import org.debatetool.io.IOUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

public class Cite implements Serializable {
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
        IOUtil.writeSerializeString(getAuthor(),out);
        IOUtil.writeSerializeString(getDate(),out);
        IOUtil.writeSerializeString(getAdditionalInfo(),out);
    }

    public void loadFromInput(DataInput in) throws IOException{
        author = IOUtil.readDeserializeString(in);
        date = IOUtil.readDeserializeString(in);
        additionalInfo = IOUtil.readDeserializeString(in);
    }

    public String toString(){
        return getAuthor()+getDate()+getAdditionalInfo();
    }

    public String getDisplayContent(){
        return "<c>"+getAuthor() + " " + getDate() + "</c> ("+getAdditionalInfo()+")";
    }
}
