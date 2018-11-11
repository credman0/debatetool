package io.cardio;

import core.Card;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

public interface CardIOManager extends Closeable, AutoCloseable {
    Card retrieveCard(byte[] hash) throws IOException;
    ArrayList<Card> retrieveCards(byte[][] hashes) throws IOException;
    void storeCard(Card card) throws IOException;
    void deleteCard(byte[] hash) throws IOException;
}
