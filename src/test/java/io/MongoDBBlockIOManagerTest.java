package io;

import org.debatetool.core.Analytic;
import org.debatetool.core.Block;
import org.debatetool.core.Card;
import org.debatetool.core.Cite;
import org.debatetool.io.componentio.ComponentIOManager;
import org.debatetool.io.iocontrollers.IOController;
import org.debatetool.io.iocontrollers.mongodb.MongoDBIOController;
import org.junit.AfterClass;
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
        IOController.getIoController().attemptAuthenticate("127.0.0.1", 27017, null,null);
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
        block = new Block("Test Block");
        block.addComponent(card1);
        block.addComponent(card2);
        block.addComponent(new Analytic(ANALYTIC_TEXT));
        try {
            IOController.getIoController().getComponentIOManager().storeSpeechComponent(card1);
            IOController.getIoController().getComponentIOManager().storeSpeechComponent(card2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void writeReadTest () {
        try {
            ComponentIOManager manager = IOController.getIoController().getComponentIOManager();
            manager.storeSpeechComponent(block);
            Block recovered = (Block) manager.retrieveSpeechComponent(block.getHash());
            recovered.load();
            Assert.assertEquals(recovered,block);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public void tearDown(){
        try {
            IOController.getIoController().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}