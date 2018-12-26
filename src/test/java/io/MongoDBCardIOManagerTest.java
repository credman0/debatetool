package io;

import core.Card;
import core.Cite;
import io.componentio.ComponentIOManager;
import io.componentio.mongodb.MongoDBComponentIOManager;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;

class MongoDBCardIOManagerTest {
    Card card;
    Card card2;

    @BeforeClass
    public void setUp(){
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("testCardText.txt").getFile());
        String text = null;
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))){
            text = new String(in.readAllBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        card = new Card(new Cite("Smith", "2010", "Renowned writer of cards"), text);
        card2 = new Card(new Cite("Smith", "2010", "Renowned writer of cards"), text+"AAA");
    }

    @Test
    public void writeReadTest (){
        try (ComponentIOManager manager = new MongoDBComponentIOManager()) {
            manager.deleteSpeechComponent(card.getHash());
            manager.storeSpeechComponent(card);
            Card recoveredCard = (Card) manager.retrieveSpeechComponent(card.getHash());
            Assert.assertEquals(card.getText(), recoveredCard.getText());
            Assert.assertEquals(card.getCite().toString(),recoveredCard.getCite().toString());
            Assert.assertEquals(card.getTimeStamp(), recoveredCard.getTimeStamp());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void doubleWriteReadTest(){
        try (ComponentIOManager manager = new MongoDBComponentIOManager()) {
            manager.deleteSpeechComponent(card.getHash());
            manager.deleteSpeechComponent(card2.getHash());
            manager.storeSpeechComponent(card);
            manager.storeSpeechComponent(card2);
            Card recoveredCard = (Card) manager.retrieveSpeechComponent(card.getHash());
            Card recoveredCard2 = (Card) manager.retrieveSpeechComponent(card2.getHash());
            Assert.assertEquals(card.getText(), recoveredCard.getText());
            Assert.assertEquals(card2.getText(), recoveredCard2.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}