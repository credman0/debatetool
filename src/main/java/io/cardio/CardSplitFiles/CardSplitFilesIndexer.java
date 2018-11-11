package io.cardio.CardSplitFiles;

import gnu.trove.map.custom_hash.TObjectLongCustomHashMap;
import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.strategy.HashingStrategy;
import io.IOUtil;
import javafx.util.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardSplitFilesIndexer {


    /**
     * Soft limit on the number of bytes that should be contained in each file. Note that in general files will be
     * slightly larger than this size, because it does not check whether a write will take it over the max size, but
     * rather does the checks when the next data would be written.
     */
    protected int maxSize;


    protected ArrayList<File> filesList;
    protected TObjectLongCustomHashMap<byte[]> index;

    protected File baseDirectory;
    protected String baseName;

    protected int currentWriteFile = 0;

    public CardSplitFilesIndexer(int maxSize, File baseDirectory, String baseName){
        if (!baseDirectory.exists()) {
            baseDirectory.mkdirs();
        }
        this.maxSize = maxSize;
        this.baseDirectory = baseDirectory;
        this.baseName = baseName;

        index = new TObjectLongCustomHashMap<byte[]>(new CardHashingStrategy());
        // Initialize the file map
        Pattern relevantFilesPattern = Pattern.compile(baseName+"(d+).dcd");
        File[] files = baseDirectory.listFiles(
                (File dir, String name) -> relevantFilesPattern.matcher(name).matches()
        );
        if (files == null || files.length==0){
            filesList = new ArrayList<>();
        }else {
            filesList = new ArrayList<>(files.length);
            for (File file : files) {
                Matcher fileMatcher = relevantFilesPattern.matcher(file.getName());
                fileMatcher.find();
                int fileIdentifier = Integer.parseInt(fileMatcher.group(1));
                // TODO: I'm pretty sure this depends on OS sorting, which seems bad. Fix.
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

    /**
     * Find the file that should be written to next. This will either be the current file that was last written to, or
     * it will be a new file if the last file was full.
     * @return The file that should be written to by the next write operation.
     */
    protected Pair<Integer, File> getWriteFile() throws IOException {
        File file = new File(baseDirectory,baseName+currentWriteFile);

        while (file.length()>maxSize){
            if (currentWriteFile>=filesList.size()) {
                filesList.add(currentWriteFile,file);
            }
            currentWriteFile++;
            file = new File(baseDirectory,baseName+currentWriteFile);
        }

        if (currentWriteFile>=filesList.size()) {
            filesList.add(currentWriteFile,file);
        }

        if (!file.exists()){
            file.createNewFile();
            if (currentWriteFile>=filesList.size()) {
                filesList.add(currentWriteFile,file);
            }
        }

        return new Pair(currentWriteFile,file);
    }

    public void generateIndex() throws IOException {
        index.clear();

        for (int fileIdentifier = 0; fileIdentifier < filesList.size(); fileIdentifier++) {
            File file = filesList.get(fileIdentifier);
            try (DataInputStream in = new DataInputStream(new FileInputStream(file))){
                int filePosition = 0;
                int skipDistance;
                do {
                    byte[] hash = new byte[16];
                    if (in.read(hash)<hash.length){
                        // this means there is not another card left in the file
                        break;
                    }
                    addToIndex(hash);

                    skipDistance = IOUtil.skipToNextHash(in);

                    // add another 8 bytes to make up for the size of the hash long.
                    filePosition += skipDistance + 8;
                } while (skipDistance > 0);
            }
        }
    }

    public File addToIndex(byte[] hash) throws IOException {
        Pair<Integer, File> fileInfo = getWriteFile();
        int fileIdentifier = fileInfo.getKey();
        long filePosition = fileInfo.getValue().length();
        // convert the file identifier and file position to a single long for storage in the hashmap
        long value = fileIdentifier << 32 | filePosition & 0xFFFFFFFFL;
        long exValue = index.put(hash, value);
        return fileInfo.getValue();
    }

    public void retrieveIndex() throws IOException {
        index.clear();
        File indexFile = new File(baseDirectory,baseName+".index");
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(indexFile)))){
            int size = in.readInt();
            for (int i = 0; i < size; i++){
                byte[] hash= new byte[16];
                in.readFully(hash);
                long value = in.readLong();
                index.put(hash,value);
            }
        }
    }

    public void storeIndex() throws IOException {
        File tempFile = new File(baseDirectory,baseName+".index.temp");
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)))){
            out.writeInt(index.size());
            for (Object hash:index.keys()){
                byte[] hashBytes = (byte[]) hash;
                out.write(hashBytes);
                out.writeLong(index.get(hash));
            }
        }
        File indexFile = new File(baseDirectory,baseName+".index");
        // attempt to do an atomic move, so that the program cannot exit at a time when there is no index file
        // eg, if we delete and then create a new file, the program could exit at a time between the delete and create
        Files.move(tempFile.toPath(), indexFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
    }

    public Pair<File, Integer> fetchIndexLocation(byte[] hash){
        if (!index.containsKey(hash)){
            return null;
        }
        long value = index.get(hash);
        int fileIdentifier = (int) (value >> 32);
        int filePosition = (int) (value);
        return new Pair<File, Integer>(filesList.get(fileIdentifier), filePosition);
    }

    public boolean containsKey(byte[] hash){
        return index.containsKey(hash);
    }

    private class CardHashingStrategy implements HashingStrategy<byte[]> {
        @Override
        public int computeHashCode(byte[] bytes) {
            return Arrays.hashCode(bytes);
        }

        @Override
        public boolean equals(byte[] bytes, byte[] t1) {
            return Arrays.equals(bytes, t1);
        }
    }
}
