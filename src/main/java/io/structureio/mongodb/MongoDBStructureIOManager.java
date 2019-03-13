package io.structureio.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import io.filters.Filter;
import io.structureio.StructureIOManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public List<String> getChildren(List<String> path){
        return getChildren(path, true);
    }
    @Override
    public List<String> getChildren(List<String> path, boolean filtered) {
        FindIterable<Document> documents;
        Bson filter;
        if (path.isEmpty()){
            filter = Filters.exists("Path."+path.size());
        }else {
            // Because a filter might match something deep in the tree, we need to return if there is a wanted node deeper
            // on the path as well (so build a query that is matches anywhere the first elements on the path)
            ArrayList<Bson> pathFiltersList = new ArrayList<>(path.size());
            for (int i = 0; i < path.size(); i++) {
                pathFiltersList.add(Filters.eq("Path." + i, path.get(i)));
            }
            // also only include paths that are longer than the current one (exclude self)
            pathFiltersList.add(Filters.exists("Path."+path.size()));
            filter = Filters.and(pathFiltersList);
        }
        if (filtered){
            Bson contentFilters = Filter.generateFilters();
            if (contentFilters!=null){
                filter = Filters.and(filter, contentFilters);
            }
        }
        documents = collection.find(filter);

        // for each document, return only the next step in the path after the query path
        //  -- uses a set, so we don't get duplicates if multiple nodes match
        Set<String> children = new HashSet<>();
        if (documents != null) {
            for (Document document : documents){
                children.add(((List<String>)document.get("Path")).get(path.size()));
            }
        }

        return new ArrayList<>(children);

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
        return getChildren(new ArrayList<>());
    }

    @Override
    public void addChild(List<String> path, String name) {
        Document child = new Document();
        // TODO adding then removing not thread safe/generally ugly
        path.add(name);
        child.put("Path", path);
        collection.insertOne(child);
        path.remove(path.size()-1);
    }

    @Override
    public void replaceContent(List<String> path, byte[] oldHash, byte[] newHash){
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
        // before changing the paths, we need to make sure to update the hashes of any components whose hash will change
        // TODO try to reduce the number of transactions here
        /*FindIterable<Document> changedDirectories = collection.find(pathFilter);
        List<WriteModel<Document>> writes = new ArrayList<WriteModel<Document>>();
        for (Document document:changedDirectories){
            List<Bson> pipeline = new ArrayList<>();
            List<Variable<Object>> let = new ArrayList<>();
            pipeline.
            UpdateOneModel update = new UpdateOneModel(document, Aggregates.lookup("SpeechComponents",);
            writes.add(update);
        }*/
        // find documents that are prefixed with every element of the path and replace those path elements
        collection.updateMany(pathFilter,pathUpdate);
    }

    @Override
    public void close() throws IOException {
        mongoClient.close();
    }
}
