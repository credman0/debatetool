package io;

import com.mongodb.MongoClient;
import core.Analytic;
import core.Block;
import core.Card;
import core.Cite;
import io.componentio.ComponentIOManager;
import io.componentio.mongodb.MongoDBComponentIOManager;
import io.iocontrollers.IOController;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.ArrayList;

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


        ArrayList<String> testChildDirPath = new ArrayList<>();
        testChildDirPath.add("test_dir");
        testChildDirPath.add("test_child_dir");
        Card card1 = new Card(new Cite("Smith", "2010", "Renowned writer of cards"), text);
        Card card2 = new Card(new Cite("Smith", "2010", "Renowned writer of cards"), text+"AAA");
        block = new Block(testChildDirPath, "Test Block");
        block.addComponent(card1);
        block.addComponent(card2);
        block.addComponent(new Analytic(ANALYTIC_TEXT));
        IOController.getIoController().getStructureIOManager().addContent(testChildDirPath, block.getHash());
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