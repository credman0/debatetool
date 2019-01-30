package io.componentio.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import core.HashIdentifiedSpeechComponent;
import io.componentio.ComponentIOManager;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MongoDBComponentIOManager implements ComponentIOManager {
    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> collection;
    public MongoDBComponentIOManager(MongoClient mongoClient){
        this.mongoClient = mongoClient;
        database = mongoClient.getDatabase("UDT");
        collection = database.getCollection("SpeechComponents");
        collection.createIndex(Indexes.hashed("Hash"));
    }
    @Override
    public HashIdentifiedSpeechComponent retrieveSpeechComponent(byte[] hash) throws IOException {
        return fromDocument(collection.find(Filters.eq("Hash", hash)).first(), hash);
    }

    @Override
    public ArrayList<HashIdentifiedSpeechComponent> retrieveSpeechComponents(byte[][] hashes) throws IOException {
        List<Bson> fetchFilterList = new ArrayList<>();
        for (byte[] hash:hashes){
            fetchFilterList.add(Filters.eq("Hash", hash));
        }
        Bson fetchFilter = Filters.or(fetchFilterList);
        FindIterable<Document> foundDocuments = collection.find(fetchFilter);

        ArrayList<HashIdentifiedSpeechComponent> speechComponents = new ArrayList<>(hashes.length);
        for (Document doc:foundDocuments){
            speechComponents.add(fromDocument(doc, ((Binary) doc.get("Hash")).getData()));
        }
        return speechComponents;
    }

    @Override
    public void storeSpeechComponent(HashIdentifiedSpeechComponent speechComponent) throws IOException {
        // check if we already have the document
        if (collection.countDocuments(Filters.eq("Hash",speechComponent.getHash()))<1){
            collection.insertOne(toDocument(speechComponent));
        }else{
            if (speechComponent.isModified()) {
                ArrayList<String>[] labelledArray = speechComponent.toLabelledLists();
                Bson update = Updates.combine(Updates.set("Labels", labelledArray[0]), Updates.set("Values", labelledArray[1]));
                collection.updateOne(Filters.eq("Hash", speechComponent.getHash()), update);
            }
        }
    }


    @Override
    public void deleteSpeechComponent(byte[] hash) throws IOException {
        collection.findOneAndDelete(Filters.eq("Hash", hash));
    }

    @Override
    public void close() {
        mongoClient.close();
    }

    public static final Document toDocument(HashIdentifiedSpeechComponent speechComponent){
        ArrayList<String>[] labelledArray = speechComponent.toLabelledLists();
        return new Document("Type",speechComponent.getClass().getName())
                .append("Hash", speechComponent.getHash())
                .append("Labels",labelledArray[0])
                .append("Values",labelledArray[1]);
    }

    public static final HashIdentifiedSpeechComponent fromDocument(Document document, byte[] hash){
        String type = document.getString("Type");
        ArrayList<String> labels = (ArrayList<String>) document.get("Labels");
        ArrayList<String> values = (ArrayList<String>) document.get("Values");
        return HashIdentifiedSpeechComponent.createFromLabelledLists(type,labels,values, hash);
    }
}
