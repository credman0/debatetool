package io.overlayio.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import core.CardOverlay;
import io.overlayio.OverlayIOManager;
import org.bson.Document;
import org.bson.types.Binary;

import java.io.IOException;
import java.util.ArrayList;
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
        return fromDocument(collection.find(Filters.eq("Hash", cardHash)).first());
    }

    @Override
    public void saveOverlays(byte[] cardHash, List<CardOverlay> overlays) {
        List<byte[]> overlayPositions = new ArrayList<>();
        List<byte[]> overlayTypes = new ArrayList<>();
        overlays.forEach(overlay -> {
            overlayPositions.add(overlay.getOverlayPositionBytes());
            overlayTypes.add(overlay.getOverlayTypeBytes());
        });
        collection.updateOne(Filters.eq("Hash", cardHash), Updates.addEachToSet("OverlayPositions", overlayPositions));
        collection.updateOne(Filters.eq("Hash", cardHash), Updates.addEachToSet("OverlayType", overlayTypes));
    }

    @Override
    public void close() throws IOException {
        mongoClient.close();
    }

    private List<CardOverlay> fromDocument (Document document){
        ArrayList<CardOverlay> overlays = new ArrayList<>();
        if (document != null) {
            List<Binary> binaryPositionList = (List<Binary>) document.get("OverlayPositions");
            List<Binary> binaryTypeList = (List<Binary>) document.get("OverlayType");
            for (int i = 0; i < binaryPositionList.size(); i++) {
                overlays.add(new CardOverlay(binaryPositionList.get(i).getData(), binaryTypeList.get(i).getData()));
            }
        }
        return overlays;
    }
}
