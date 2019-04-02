package org.debatetool.io.filters;

import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;

public class StructureFilter {
    private String field;
    private String value;
    private Filter.Type type;
    public StructureFilter(String field, String value, Filter.Type type) {
        this.field = field;
        this.value = value;
        this.type = type;
    }
    public Bson toBson(){
        switch (type){
            case CONTAINS:
                return Filters.regex(field, value);
            default:
                throw new IllegalStateException("Type not recognized: " + type);
        }
    }
}
