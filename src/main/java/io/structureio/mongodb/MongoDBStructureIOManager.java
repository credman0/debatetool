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
import org.bson.conversions.Bson;
import org.bson.types.Binary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MongoDBStructureIOManager implements StructureIOManager {
    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> collection;
    public static final UpdateOptions upsertOption = new UpdateOptions().upsert(true);
    public MongoDBStructureIOManager(MongoClient mongoClient){
        this.mongoClient = mongoClient;
        database = mongoClient.getDatabase("UDT");
        collection = database.getCollection("StructureElements");
        collection.createIndex(Indexes.ascending("Path"));
    }

    @Override
    public List<String> getChildren(List<String> path) {
        Document document = collection.find(Filters.eq("Path", path)).first();
        List<String> children = null;
        if (document != null) {
            children = (List<String>) document.get("Children");
        }
        if (children == null) {
            return new ArrayList<>();
        } else {
            return children;
        }
    }

    @Override
    public List<byte[]> getContent(List<String> path) {
        Document document = collection.find(Filters.eq("Path", path)).first();
        List<Binary> binaryList = null;
        if (document != null) {
            binaryList = (List<Binary>) document.get("Content");
        }
        if (binaryList == null){
            return new ArrayList<>();
        }
        // Todo find a better way to cast from list of Binary to list of byte[]
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
    public void replaceContent(List<String> path, byte[] oldHash, byte[] newHash){
        System.out.println(Arrays.toString(oldHash));
        System.out.println(Arrays.toString(newHash));
        System.out.println(path);
        // TODO combine these statements
        collection.updateOne(Filters.eq("Path", path), Updates.pull("Content",oldHash));
        collection.updateOne(Filters.eq("Path", path), Updates.push("Content",newHash));
    }

    @Override
    public void addContent(List<String> path, byte[] contentID) {
        // TODO add checks against adding content before creating a tree element
        collection.updateOne(Filters.eq("Path", path), Updates.addToSet("Content", contentID),upsertOption);
    }

    @Override
    public List<String> getBlockPath(byte[] hash){
        // TODO replace this search (or at least index it)
        Document directoryDoc =  collection.find(Filters.in("Content", hash)).first();
        if (directoryDoc==null){
            return new ArrayList<>();
        }else {
            return (List<String>) directoryDoc.get("Path");
        }
    }

    @Override
    public void renameDirectory(List<String> path, String name, String newName) {
        // generate a filter that will catch the path in the chosen directory as well as all subdirectories, to update all at once
        ArrayList<Bson>pathFiltersList = new ArrayList<>(path.size());
        ArrayList<Bson>pathUpdateList = new ArrayList<>(path.size());
        for (int i = 0; i < path.size(); i++){
            pathFiltersList.add(Filters.eq("Path."+i, path.get(i)));
            pathUpdateList.add(Updates.set("Path."+i, path.get(i)));
        }
        pathFiltersList.add(Filters.eq("Path."+path.size(), name));
        pathUpdateList.add(Updates.set("Path."+path.size(), newName));
        Bson pathFilter = Filters.and(pathFiltersList);
        Bson pathUpdate = Updates.combine(pathUpdateList);
        // find documents that are prefixed with every element of the path and replace those path elements
        collection.updateMany(pathFilter,pathUpdate);

        // now change the way that the document is found by its parent
        Bson parentFilter = Filters.eq("Path", path);
        collection.updateOne(parentFilter, Updates.pull("Children",name));
        collection.updateOne(parentFilter, Updates.push("Children",newName));
    }

    @Override
    public void close() throws IOException {
        mongoClient.close();
    }
}
