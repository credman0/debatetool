package org.debatetool.io.filters;

public class ComponentFilter {

    private String field;
    private String value;
    private Filter.Type type;
    public ComponentFilter(String field, String value, Filter.Type type) {
        this.field = field;
        this.value = value;
        this.type = type;
    }
}
