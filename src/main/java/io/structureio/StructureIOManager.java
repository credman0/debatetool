package io.structureio;

import java.io.Closeable;
import java.util.List;

public interface StructureIOManager extends Closeable, AutoCloseable {
    List<String> getChildren(List<String> path);

    List<String> getChildren(List<String> path, boolean filtered);

    List<byte[]> getContent(List<String> path);
    List<String> getRoot();
    void addChild(List<String> path, String name);

    void replaceContent(List<String> path, byte[] oldHash, byte[] newHash);

    void addContent(List<String> path, byte[] contentID);

    List<String> getBlockPath(byte[] hash);

    void renameDirectory(List<String> path, String name, String newName);

    void getSafeChildRename(List<String> path, String base);
}
