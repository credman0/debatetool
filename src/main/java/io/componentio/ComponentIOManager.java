package io.componentio;

import core.HashIdentifiedSpeechComponent;
import org.bson.types.Binary;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public interface ComponentIOManager extends Closeable, AutoCloseable {
    HashIdentifiedSpeechComponent retrieveSpeechComponent(byte[] hash) throws IOException;
    HashMap<Binary, HashIdentifiedSpeechComponent> retrieveSpeechComponents(List<byte[]> hashes) throws IOException;
    void storeSpeechComponent(HashIdentifiedSpeechComponent speechComponent) throws IOException;
    void deleteSpeechComponent(byte[] hash) throws IOException;
    void loadAll(HashIdentifiedSpeechComponent component) throws IOException;
}
