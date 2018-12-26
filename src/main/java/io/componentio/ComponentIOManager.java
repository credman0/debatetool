package io.componentio;

import core.SpeechComponent;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

public interface ComponentIOManager extends Closeable, AutoCloseable {
    SpeechComponent retrieveSpeechComponent(byte[] hash) throws IOException;
    ArrayList<SpeechComponent> retrieveSpeechComponents(byte[][] hashes) throws IOException;
    void storeSpeechComponent(SpeechComponent speechComponent) throws IOException;
    void deleteSpeechComponent(byte[] hash) throws IOException;
}
