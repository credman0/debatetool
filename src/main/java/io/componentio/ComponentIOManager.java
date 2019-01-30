package io.componentio;

import core.HashIdentifiedSpeechComponent;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

public interface ComponentIOManager extends Closeable, AutoCloseable {
    HashIdentifiedSpeechComponent retrieveSpeechComponent(byte[] hash) throws IOException;
    ArrayList<HashIdentifiedSpeechComponent> retrieveSpeechComponents(byte[][] hashes) throws IOException;
    void storeSpeechComponent(HashIdentifiedSpeechComponent speechComponent) throws IOException;

    void deleteSpeechComponent(byte[] hash) throws IOException;
}
