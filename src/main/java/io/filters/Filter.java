package io.filters;

import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * provides static methods that filter requests to all IO based on passed ComponentFilter objects
 */
public class Filter {

    private Filter(){}

    public enum Type{CONTAINS}
    private static List<ComponentFilter> componentFilters = new ArrayList<>();
    private static List<StructureFilter> structureFilters = new ArrayList<>();
    private static Set<byte[]> matchingComponents;
    public static void addComponentFilter(ComponentFilter filter){
        componentFilters.add(filter);
    }

    public static void addStructureFilter(StructureFilter filter){
        structureFilters.add(filter);
    }

    public static void updateComponentFilter(){
        matchingComponents = null;
    }

    public static Bson generateFilters(){
        if (structureFilters.isEmpty()){
            return null;
        }
        List<Bson> filtersList = new ArrayList<>();
        for (StructureFilter filter:structureFilters){
            filtersList.add(filter.toBson());
        }
        return Filters.or(filtersList);
    }

    public static void resetFilters(){
        componentFilters.clear();
        structureFilters.clear();
        updateComponentFilter();
    }
}
