package io.cardio;

import core.Card;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

public interface CardIOManager extends Closeable, AutoCloseable {
    Card retrieveCard(int hash) throws IOException;
    ArrayList<Card> retrieveCards(int[] hashes) throws IOException;
    void storeCard(Card card) throws IOException;
    void deleteCard(int hash) throws IOException;
    void generateIndex() throws IOException;
    void retrieveIndex() throws IOException;
    void storeIndex() throws IOException;
}
