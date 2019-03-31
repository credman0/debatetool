package io.accounts.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import io.accounts.DBLock;
import org.bson.Document;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MongoDBLock implements DBLock {
    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> collection;

    public MongoDBLock(MongoClient mongoClient){
        this.mongoClient = mongoClient;
        database = mongoClient.getDatabase("UDT");
        collection = database.getCollection("Locks");
        collection.createIndex(Indexes.hashed("Hash"));
        // TODO don't drop indexes on every start
        collection.dropIndexes();
        // TODO prevent this from cleaning up locks when still connected
        collection.createIndex(Indexes.ascending("time"),
                new IndexOptions().expireAfter(10L, TimeUnit.MINUTES));
    }

    @Override
    public boolean tryLock(byte[] hash) {
        String username = mongoClient.getCredential().getUserName();
        Document setOnInsert = new Document();
        setOnInsert.put("time", new Date());
        setOnInsert.put("username", username);
        Document update = new Document("$setOnInsert", setOnInsert);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions();
        options.returnDocument(ReturnDocument.AFTER);
        options.upsert(true);
        Document document = collection.findOneAndUpdate(Filters.eq("Hash", hash), update, options);
        return document.get("username").equals(username);
    }

    @Override
    public void unlock(byte[] hash) {
        collection.findOneAndDelete(Filters.and(Filters.eq("Hash", hash),Filters.eq("username", mongoClient.getCredential().getUserName())));
    }

    @Override
    public void unlockAll() {
        collection.deleteMany(Filters.eq("username", mongoClient.getCredential().getUserName()));
    }

    @Override
    public void unlockAllExcept(byte[] hash) {
        collection.deleteMany(Filters.and(Filters.eq("username", mongoClient.getCredential().getUserName()), Filters.not(Filters.eq("Hash", hash))));
    }
}
