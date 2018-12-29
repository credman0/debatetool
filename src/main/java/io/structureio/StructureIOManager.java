package io.structureio;

import java.io.Closeable;
import java.util.List;

public interface StructureIOManager extends Closeable, AutoCloseable {
    List<String> getChildren(List<String> path);
    List<byte[]> getContent(List<String> path);
    List<String> getRoot();
    void addChild(List<String> path, String name);
    void addContent(List<String> path, byte[] contentID);
}
