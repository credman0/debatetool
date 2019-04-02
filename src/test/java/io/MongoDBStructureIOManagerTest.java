package io;

import org.debatetool.core.Analytic;
import org.debatetool.core.Block;
import org.debatetool.core.Card;
import org.debatetool.core.Cite;
import org.debatetool.io.iocontrollers.IOController;
import org.debatetool.io.iocontrollers.mongodb.MongoDBIOController;
import org.debatetool.io.structureio.StructureIOManager;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.ArrayList;

class MongoDBStructureIOManagerTest {
    Card card;
    Card card2;
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
        card = new Card(new Cite("Smith", "2010", "Renowned writer of cards"), text);
        card2 = new Card(new Cite("Smith", "2010", "Renowned writer of cards"), text+"AAA");
    }

    @Test
    public void directoriesAddGetTest (){
        try (IOController controller = new MongoDBIOController()) {
            StructureIOManager manager = controller.getStructureIOManager();
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


            Block block = new Block(testChildDirPath, "Test Block");
            block.addComponent(card);
            block.addComponent(card2);
            block.addComponent(new Analytic(ANALYTIC_TEXT));
            manager.addContent(testChildDirPath, block.getHash());

            Assert.assertEquals(manager.getChildren(emptyList), testDirPath);
            Assert.assertEquals(manager.getContent(testDirPath).get(0), card.getHash());
            Assert.assertEquals(manager.getContent(testDirPath).get(1), card2.getHash());
            Assert.assertEquals(manager.getContent(testChildDirPath).get(0), card2.getHash());
            //Assert.assertEquals(manager.getContent(testChildDirPath).get(1), block.getHash());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}