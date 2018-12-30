package io;

import com.mongodb.MongoClient;
import core.Analytic;
import core.Block;
import core.Card;
import core.Cite;
import io.componentio.ComponentIOManager;
import io.componentio.mongodb.MongoDBComponentIOManager;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;

class MongoDBBlockIOManagerTest {
    Block block;
    public final String ANALYTIC_TEXT = "This is an analytic";

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
        Card card1 = new Card(new Cite("Smith", "2010", "Renowned writer of cards"), text);
        Card card2 = new Card(new Cite("Smith", "2010", "Renowned writer of cards"), text+"AAA");
        block = new Block();
        block.addComponent(card1);
        block.addComponent(card2);
        block.addComponent(new Analytic(ANALYTIC_TEXT));
    }

    @Test
    public void writeReadTest (){
        MongoClient mongoClient = new MongoClient();
        try (ComponentIOManager manager = new MongoDBComponentIOManager(mongoClient)) {
            manager.storeSpeechComponent(block);
            Block recovered = (Block) manager.retrieveSpeechComponent(block.getHash());
            recovered.load();
            Assert.assertEquals(recovered.getLabel(), block.getLabel());
            Assert.assertEquals(recovered.size(), block.size());
            for (int i = 0; i < block.size(); i++){
                Assert.assertEquals(block.getComponent(i).getDisplayContent(), recovered.getComponent(i).getDisplayContent());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}