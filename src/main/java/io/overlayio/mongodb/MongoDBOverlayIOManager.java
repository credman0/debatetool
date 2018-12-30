package io.overlayio.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import core.CardOverlay;
import io.overlayio.OverlayIOManager;
import org.bson.Document;

import java.io.IOException;
import java.util.List;

public class MongoDBOverlayIOManager implements OverlayIOManager {
    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> collection;
    public MongoDBOverlayIOManager(MongoClient mongoClient){
        this.mongoClient = mongoClient;
        database = mongoClient.getDatabase("UDT");
        collection = database.getCollection("OverlayElements");
        collection.createIndex(Indexes.hashed("Hash"));
    }

    @Override
    public List<CardOverlay> getOverlays(byte[] cardHash) {
        return null;
    }

    @Override
    public void saveOverlays(byte[] cardHash, List<CardOverlay> overlays) {

    }

    @Override
    public void close() throws IOException {
        mongoClient.close();
    }
}
