package io.cardio.CardSplitFiles;

import core.Card;
import gnu.trove.map.hash.TIntLongHashMap;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.IOUtil;
import io.cardio.CardIOManager;
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
     * The key is the hash of the card. The value is a long where the most significant 4 bytes correspond to the file
     * identifier, and the least significant 4 bytes refer to the position in the file.
     */
    protected CardSplitFilesIndexer index;

    public CardSplitFilesStreamer(int maxSize, File baseDirectory, String baseName) {
        index = new CardSplitFilesIndexer(maxSize, baseDirectory, baseName);
    }

    @Override
    public Card retrieveCard(byte[] hash) throws IOException {
        if (!index.containsKey(hash)){
            return null;
        }
        Pair<File, Integer> indexLocation = index.fetchIndexLocation(hash);
        Card card;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(indexLocation.getKey())))){
            in.skipBytes(indexLocation.getValue());
            card = new Card(in);
        }
        return card;
    }

    @Override
    public ArrayList<Card> retrieveCards(byte[][] hashes) throws IOException {
        // TODO: Make this more efficient at fetching several cards
        ArrayList<Card> cards = new ArrayList<>(hashes.length);
        for (int i = 0; i < hashes.length; i++){
            cards.add(retrieveCard(hashes[i]));
        }
        return cards;
    }

    @Override
    public void storeCard(Card card) throws IOException {
        if (index.containsKey(card.getHash())){
            return;
        }
        // Add to the index the current location of the end of the file before we do the write
        File file = index.addToIndex(card.getHash());
        // FileOutputStream opened in append mode, so we write to end of file
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file, true)))){
            card.writeToOutput(out);
        }
    }



    @Override
    public void deleteCard(byte[] hash) throws IOException {
        throw new UnsupportedOperationException("deleteCard not implemented");
    }

    @Override
    public void close() throws IOException {
        index.storeIndex();
    }
}
