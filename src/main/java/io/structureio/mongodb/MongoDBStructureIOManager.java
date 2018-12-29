package io.structureio.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import io.structureio.StructureIOManager;
import org.bson.Document;
import org.bson.types.Binary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// TODO restructure so the mongo io managers take a client and don't each open their own
public class MongoDBStructureIOManager implements StructureIOManager {
    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> collection;
    public static final UpdateOptions upsertOption = new UpdateOptions().upsert(true);
    public MongoDBStructureIOManager(){
        mongoClient = new MongoClient();
        database = mongoClient.getDatabase("UDT");
        collection = database.getCollection("StructureElements");
        collection.createIndex(Indexes.ascending("Path"));
    }

    @Override
    public List<String> getChildren(List<String> path) {
        List<String> children = (List<String>) collection.find(Filters.eq("Path", path)).first().get("Children");
        if (children==null){
            return new ArrayList<>();
        }else {
            return children;
        }
    }

    @Override
    public List<byte[]> getContent(List<String> path) {
        List<Binary> binaryList = (List<Binary>) collection.find(Filters.eq("Path", path)).first().get("Content");
        if (binaryList == null){
            return new ArrayList<>();
        }
        // Todo find a better way to do this casting
        List<byte[]> byteList = new ArrayList<>(binaryList.size());
        for (Binary binary:binaryList){
            byteList.add(binary.getData());
        }
        return byteList;
    }

    @Override
    public List<String> getRoot() {
        return (List<String>) collection.find(Filters.size("Path", 0)).first().get("Children");
    }

    @Override
    public void addChild(List<String> path, String name) {
        collection.updateOne(Filters.eq("Path", path), Updates.addToSet("Children", name),upsertOption);
    }

    @Override
    public void addContent(List<String> path, byte[] contentID) {
        // TODO add checks against adding content before creating a tree element
        collection.updateOne(Filters.eq("Path", path), Updates.addToSet("Content", contentID),upsertOption);
    }

    @Override
    public void close() throws IOException {
        mongoClient.close();
    }
}
