package io.filters;

import java.util.ArrayList;
import java.util.List;

/**
 * Objects that abstract types of filter operations, and provides static methods that filter requests to all IO based
 * on passed Filter objects
 */
public class Filter {
    enum Type{DIRECTORY}
    private static List<Filter> activeFilters = new ArrayList<>();
    public static void addFilter(Filter filter){
        activeFilters.add(filter);
    }
}
