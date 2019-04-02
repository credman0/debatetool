package org.debatetool.io.overlayio.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.debatetool.core.CardOverlay;
import org.debatetool.io.overlayio.OverlayIOManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MongoDBOverlayIOManager implements OverlayIOManager {
    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> collection;
    public static final UpdateOptions upsertOption = new UpdateOptions().upsert(true);
    public MongoDBOverlayIOManager(MongoClient mongoClient){
        this.mongoClient = mongoClient;
        database = mongoClient.getDatabase("UDT");
        collection = database.getCollection("OverlayElements");
        collection.createIndex(Indexes.hashed("Hash"));
    }

    @Override
    public HashMap<String,List<CardOverlay>> getOverlays(byte[] cardHash) {
        FindIterable <Document> documents =  collection.find(Filters.eq("Hash", cardHash));
        HashMap<String,List<CardOverlay>> overlays = new HashMap<>();
        for (Document document:documents){
            overlays.put(document.getString("Type"),fromDocument(document));
        }
        return overlays;
    }

    @Override
    public void saveOverlays(byte[] cardHash, List<CardOverlay> overlays, String type) {
        List<byte[]> overlayPositions = new ArrayList<>();
        List<byte[]> overlayTypes = new ArrayList<>();
        List<String> names = new ArrayList<>();
        overlays.forEach(overlay -> {
            overlayPositions.add(overlay.getOverlayPositionBytes());
            overlayTypes.add(overlay.getOverlayTypeBytes());
            names.add(overlay.getName());
        });
        collection.updateOne(Filters.and(Filters.eq("Hash", cardHash),Filters.eq("Type", type)), Updates.set("Names", names),upsertOption);
        collection.updateOne(Filters.and(Filters.eq("Hash", cardHash),Filters.eq("Type", type)), Updates.set("OverlayPositions", overlayPositions),upsertOption);
        collection.updateOne(Filters.and(Filters.eq("Hash", cardHash),Filters.eq("Type", type)), Updates.set("OverlayTypes", overlayTypes),upsertOption);
    }

    @Override
    public HashMap<Binary, HashMap<String, List<CardOverlay>>> getAllOverlays(List<byte[]> cardHashes) {
        List<Bson> fetchFilterList = new ArrayList<>();
        for (byte[] hash:cardHashes){
            fetchFilterList.add(Filters.eq("Hash", hash));
        }
        if (fetchFilterList.isEmpty()){
            return new HashMap<>();
        }
        Bson fetchFilter = Filters.or(fetchFilterList);
        FindIterable<Document> foundDocuments = collection.find(fetchFilter);
        HashMap<Binary, HashMap<String, List<CardOverlay>>> allOverlays = new HashMap<>();
        for (Document document:foundDocuments){
            Binary binaryHash = (Binary) document.get("Hash");
            if (!allOverlays.containsKey(binaryHash)){
                allOverlays.put(binaryHash, new HashMap<>());
            }
            allOverlays.get(binaryHash).put(document.getString("Type"),fromDocument(document));
        }
        return allOverlays;
    }

    @Override
    public void close() throws IOException {
        mongoClient.close();
    }

    private List<CardOverlay> fromDocument (Document document){
        ArrayList<CardOverlay> overlays = new ArrayList<>();
        if (document != null) {
            List<Binary> binaryPositionList = (List<Binary>) document.get("OverlayPositions");
            List<Binary> binaryTypeList = (List<Binary>) document.get("OverlayTypes");
            List<String> nameList = (List<String>) document.get("Names");
            for (int i = 0; i < binaryPositionList.size(); i++) {
                overlays.add(new CardOverlay(nameList.get(i), binaryPositionList.get(i).getData(), binaryTypeList.get(i).getData()));
            }
        }
        return overlays;
    }
}
