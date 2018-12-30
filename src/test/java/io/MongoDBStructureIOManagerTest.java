package io;

import com.mongodb.MongoClient;
import core.Card;
import core.Cite;
import io.structureio.StructureIOManager;
import io.structureio.mongodb.MongoDBStructureIOManager;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.ArrayList;

class MongoDBStructureIOManagerTest {
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
    public void directoriesAddGetTest (){
        MongoClient mongoClient = new MongoClient();
        try (StructureIOManager manager = new MongoDBStructureIOManager(mongoClient)) {
            ArrayList<String> emptyList = new ArrayList<>();
            manager.addChild(emptyList, "test_dir");

            ArrayList<String> testDirPath = new ArrayList<>();
            testDirPath.add("test_dir");
            manager.addContent(testDirPath, card.getHash());
            manager.addContent(testDirPath, card2.getHash());

            ArrayList<String> testChildDirPath = new ArrayList<>();
            testChildDirPath.add("test_dir");
            testChildDirPath.add("test_child_dir");
            manager.addChild(testDirPath, "test_child_dir");
            manager.addContent(testChildDirPath, card2.getHash());

            Assert.assertEquals(manager.getChildren(emptyList), testDirPath);
            Assert.assertEquals(manager.getContent(testDirPath).get(0), card.getHash());
            Assert.assertEquals(manager.getContent(testDirPath).get(1), card2.getHash());
            Assert.assertEquals(manager.getContent(testChildDirPath).get(0), card2.getHash());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}