package org.debatetool.io.componentio.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import org.debatetool.core.*;
import org.debatetool.io.componentio.ComponentIOManager;
import org.debatetool.io.iocontrollers.IOController;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    public HashMap<Binary, HashIdentifiedSpeechComponent> retrieveSpeechComponents(List<byte[]> hashes) throws IOException {
        List<Bson> fetchFilterList = new ArrayList<>();
        for (byte[] hash:hashes){
            fetchFilterList.add(Filters.eq("Hash", hash));
        }
        if (fetchFilterList.isEmpty()){
            return new HashMap<>();
        }
        Bson fetchFilter = Filters.or(fetchFilterList);
        FindIterable<Document> foundDocuments = collection.find(fetchFilter);

        HashMap<Binary,HashIdentifiedSpeechComponent> speechComponents = new HashMap<>(hashes.size()*2);
        for (Document doc:foundDocuments){
            Binary binaryHash = (Binary) doc.get("Hash");
            speechComponents.put(binaryHash, fromDocument(doc, binaryHash.getData()));
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
    public void loadAll(HashIdentifiedSpeechComponent component) throws IOException {
        if (Block.class.isInstance(component)) {
            Block block = (Block) component;
            ArrayList<byte[]> cardHashes = new ArrayList<>(block.size());
            ArrayList<Card> cards = new ArrayList<>(block.size());
            appendBlockCards(block,cardHashes,cards);
            loadCards(cardHashes,cards);
        }else if (Speech.class.isInstance(component)) {
            Speech speech = (Speech) component;
            ArrayList<byte[]> cardHashes = new ArrayList<>(speech.size());
            ArrayList<Card> cards = new ArrayList<>(speech.size());
            for (int i = 0; i < speech.size(); i++){
                SpeechComponent speechComponent = speech.getComponent(i);
                if (Card.class.isInstance(speechComponent)){
                    cardHashes.add(((HashIdentifiedSpeechComponent)speechComponent).getHash());
                    cards.add((Card) speechComponent);
                }else if (Block.class.isInstance(speechComponent)){
                    appendBlockCards(((Block) speechComponent), cardHashes,cards);
                }else{
                    throw new IllegalStateException("Illegal component in speech: "+speechComponent.getClass());
                }
            }
            loadCards(cardHashes,cards);
        } else {
            throw new IllegalArgumentException("Unrecognized type: " + component.getClass());
        }
    }

    private void appendBlockCards(Block block, List<byte[]> cardHashes, List<Card> cards){
        for (int i = 0; i < block.size(); i++){
            SpeechComponent blockComponent = block.getComponent(i);
            if (Card.class.isInstance(blockComponent)){
                cardHashes.add(((HashIdentifiedSpeechComponent)blockComponent).getHash());
                cards.add((Card) blockComponent);
            }else if (Analytic.class.isInstance(blockComponent)){
                // nothing to load
            }else{
                throw new IllegalStateException("Illegal component in block: "+blockComponent.getClass());
            }
        }
    }

    private void loadCards(List<byte[]> cardHashes, List<Card> cards) throws IOException {
        // TODO combine this into one request
        HashMap<Binary, HashIdentifiedSpeechComponent> loadedComponents = retrieveSpeechComponents(cardHashes);
        HashMap<Binary, HashMap<String, List<CardOverlay>>> allOverlays = IOController.getIoController().getOverlayIOManager().getAllOverlays(cardHashes);
        for (Card cardToLoad:cards){
            Binary cardBinaryHash = new Binary(cardToLoad.getHash());
            cardToLoad.setTo((Card) loadedComponents.get(cardBinaryHash));
            HashMap<String, List<CardOverlay>> cardOverlays = allOverlays.get(cardBinaryHash);
            if (cardOverlays!=null) {
                cardToLoad.assignOverlaysFromMap(cardOverlays);
            }
        }
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
