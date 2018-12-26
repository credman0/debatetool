package io.cardio.mongodb;

import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import core.Card;
import core.Cite;
import io.cardio.CardIOManager;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;

public class MongoDBCardIOManager implements CardIOManager {
    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> collection;
    public MongoDBCardIOManager(){
        mongoClient = new MongoClient();
        database = mongoClient.getDatabase("UDT");
        collection = database.getCollection("Cards");
        collection.createIndex(Indexes.hashed("Hash"));
    }
    @Override
    public Card retrieveCard(byte[] hash) throws IOException {
        return fromDocument(collection.find(Filters.eq("Hash", hash)).first());
    }

    @Override
    public ArrayList<Card> retrieveCards(byte[][] hashes) throws IOException {
        // TODO: Make this more efficient at fetching several cards
        ArrayList<Card> cards = new ArrayList<>(hashes.length);
        for (int i = 0; i < hashes.length; i++){
            cards.add(retrieveCard(hashes[i]));
        }
        return cards;
    }

    @Override
    public void storeCard(Card card) throws IOException {
        // check if we already have the document
        if (collection.countDocuments(Filters.eq("Hash",card.getHash()))<1){
            collection.insertOne(new Document(toDocument(card)));
        };

    }

    @Override
    public void deleteCard(byte[] hash) throws IOException {

    }

    @Override
    public void close() {
        mongoClient.close();
    }

    public static final Document toDocument(Card card){
        Cite cite = card.getCite();
        return new Document("Hash", card.getHash())
                .append("Cite", new Document("Author", cite.getAuthor())
                        .append("Date",cite.getDate())
                        .append("Info",cite.getAdditionalInfo()))
                .append("Text", card.getText());
    }

    public static final Card fromDocument(Document document){
        Document citeDocument = (Document) document.get("Cite");
        String author = citeDocument.getString("Author");
        String date = citeDocument.getString("Date");
        String info = citeDocument.getString("Info");
        Cite cite = new Cite(author,date,info);
        Card card = new Card(cite,document.getString("Text"));
        return card;
    }
}
