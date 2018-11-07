package io.cardio;

import core.Card;
import gnu.trove.map.hash.TIntLongHashMap;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.IOUtil;
import javafx.util.Pair;


/**
 * Card io system based on a number of files each containing a relatively small number of cards, so that each can be
 * written and rewritten relatively easily.
 *
 * The file structure is files with a set prefix, and a postfix that defines their identification (an integer).
 *
 * Each file is simply a series of serialized Cards.
 */
public class CardSplitFilesStreamer implements CardIOManager {
    /**
     * Soft limit on the number of bytes that should be contained in each file. Note that in general files will be
     * slightly larger than this size, because it does not check whether a write will take it over the max size, but
     * rather does the checks when the next data would be written.
     */
    protected int maxSize;
    protected File baseDirectory;
    protected String baseName;

    protected int currentWriteFile = 0;

    /**
     * The key is the hash of the card. The value is a long where the most significant 4 bytes correspond to the file
     * identifier, and the least significant 4 bytes refer to the position in the file.
     */
    protected TIntLongHashMap index;
    protected ArrayList<File> filesList;

    public CardSplitFilesStreamer(int maxSize, File baseDirectory, String baseName) {
        this.maxSize = maxSize;
        this.baseDirectory = baseDirectory;
        this.baseName = baseName;
        if (!baseDirectory.exists()) {
            baseDirectory.mkdirs();
        }
        // Initialize the file map
        Pattern relevantFilesPattern = Pattern.compile(baseName+"(d+).dcd");
        File[] files = baseDirectory.listFiles(
                (File dir, String name) -> relevantFilesPattern.matcher(name).matches()
        );
        if (files == null){
            filesList = new ArrayList<>();
        }else {
            filesList = new ArrayList<>(files.length);
            for (File file : files) {
                Matcher fileMatcher = relevantFilesPattern.matcher(file.getName());
                fileMatcher.find();
                int fileIdentifier = Integer.parseInt(fileMatcher.group(1));
                filesList.add(fileIdentifier, file);
            }
            File indexFile = new File(baseDirectory, baseName + ".index");
            try {
                if (!indexFile.exists()) {
                    generateIndex();
                } else {
                    retrieveIndex();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Card retrieveCard(int hash) throws IOException {
        Pair<File, Integer> indexLocation = fetchIndexLocation(hash);
        Card card;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(indexLocation.getKey())))){
            in.skipBytes(indexLocation.getValue());
            card = new Card(in);
        }
        return card;
    }

    @Override
    public ArrayList<Card> retrieveCards(int[] hashes) throws IOException {
        // TODO: Make this more efficient at fetching several cards
        ArrayList<Card> cards = new ArrayList<>(hashes.length);
        for (int i = 0; i < hashes.length; i++){
            cards.add(retrieveCard(hashes[i]));
        }
        return cards;
    }

    @Override
    public void storeCard(Card card) throws IOException {
        if (index.containsKey(card.hashCode())){
            return;
        }
        int currentFile = getWriteFile();
        File file = new File(baseDirectory,baseName+currentFile);
        // FileOutputStream opened in append mode, so we write to end of file
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, true)))){
            // Add to the index the current location of the end of the file before we do the write
            writeToIndex(card.hashCode(),currentFile,(int)file.length());
            card.writeToOutput(out);
        }
    }

    /**
     * Find the file that should be written to next. This will either be the current file that was last written to, or
     * it will be a new file if the last file was full.
     * @return The file that should be written to by the next write operation.
     */
    protected int getWriteFile() throws IOException {
        File file = new File(baseDirectory,baseName+currentWriteFile);

        while (file.length()>maxSize){
            if (currentWriteFile>=filesList.size()) {
                filesList.add(currentWriteFile,file);
            }
            currentWriteFile++;
            file = new File(baseDirectory,baseName+currentWriteFile);
        }

        if (!file.exists()){
            file.createNewFile();
            if (currentWriteFile>=filesList.size()) {
                filesList.add(currentWriteFile,file);
            }
        }

        return currentWriteFile;
    }

    @Override
    public void deleteCard(int hash) throws IOException {
        throw new UnsupportedOperationException("deleteCard not implemented");
    }

    protected Pair<File, Integer> fetchIndexLocation(int hash){
        long value = index.get(hash);
        int fileIdentifier = (int) (value >> 32);
        int filePosition = (int) (value);
        return new Pair<>(filesList.get(fileIdentifier), filePosition);
    }

    @Override
    public void generateIndex() throws IOException {
        TIntLongHashMap index = new TIntLongHashMap();

        for (int fileIdentifier = 0; fileIdentifier < filesList.size(); fileIdentifier++) {
            File file = filesList.get(fileIdentifier);
            try (DataInputStream in = new DataInputStream(new FileInputStream(file))){
                int filePosition = 0;
                int skipDistance;
                do {
                    int hash;
                    try {
                        hash = in.readInt();
                    }catch(EOFException e){
                        // so this looks like an anti-pattern, but it seems like the best way to exit on EOF
                        break;
                    }
                    writeToIndex(hash,fileIdentifier,filePosition);

                    skipDistance = IOUtil.skipToNextHash(in);

                    // add another 8 bytes to make up for the size of the hash long.
                    filePosition += skipDistance + 8;
                } while (skipDistance > 0);
            }
        }
        this.index = index;
    }

    protected void writeToIndex(int hash, int fileIdentifier, int filePosition){
        // convert the file identifier and file position to a single long for storage in the hashmap
        long value = fileIdentifier << 32 | filePosition & 0xFFFFFFFFL;
        long exValue = index.put(hash, value);
    }

    @Override
    public void retrieveIndex() throws IOException {
        File indexFile = new File(baseDirectory,baseName+".index");
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(indexFile)))){
            int size = in.readInt();
            index = new TIntLongHashMap(size);
            for (int i = 0; i < size; i++){
                int hash= in.readInt();
                long value = in.readLong();
                index.put(hash,value);
            }
        }
    }

    @Override
    public void storeIndex() throws IOException {
        File tempFile = new File(baseDirectory,baseName+".index.temp");
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)))){
            out.writeInt(index.size());
            for (int hash:index.keys()){
                out.writeInt(hash);
                out.writeLong(index.get(hash));
            }
        }
        File indexFile = new File(baseDirectory,baseName+".index");
        // attempt to do an atomic move, so that the program cannot exit at a time when there is no index file
        // eg, if we delete and then create a new file, the program could exit at a time between the delete and create
        Files.move(tempFile.toPath(), indexFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
    }

    @Override
    public void close() throws IOException {
        storeIndex();
    }
}
