package io.structureio;

import core.HashIdentifiedSpeechComponent;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public interface StructureIOManager extends Closeable, AutoCloseable {
    List<String> getChildren(List<String> path);

    List<String> getChildren(List<String> path, boolean filtered);

    List<HashIdentifiedSpeechComponent> getContent(List<String> path) throws IOException;
    List<String> getRoot();
    void addChild(List<String> path, String name);

    void replaceContent(List<String> path, byte[] oldHash, byte[] newHash);

    void addContent(List<String> path, HashIdentifiedSpeechComponent component);

    List<String> getBlockPath(byte[] hash);

    void renameDirectory(List<String> path, String name, String newName);

    void getSafeChildRename(List<String> path, String base);
}
