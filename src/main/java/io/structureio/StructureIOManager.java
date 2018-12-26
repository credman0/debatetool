package io.structureio;

import java.io.Closeable;
import java.util.List;

public interface StructureIOManager extends Closeable, AutoCloseable {
    List<String> getChildren(List<String> path);
}
