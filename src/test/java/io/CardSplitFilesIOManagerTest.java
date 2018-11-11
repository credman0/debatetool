package io;

import core.Card;
import core.Cite;
import io.cardio.CardSplitFiles.CardSplitFilesStreamer;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.*;

class CardSplitFilesIOManagerTest {
    File baseDirectory = new File("tests/testdb/");
    String baseName = "test";

    Card card;
    Card card2;

    @BeforeClass
    public void setUp(){
        if (baseDirectory.exists()){
            IOUtil.deleteDir(baseDirectory);
        }
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
        try (CardSplitFilesStreamer manager = new CardSplitFilesStreamer(0x100000,baseDirectory, baseName)) {
            manager.storeCard(card);
            Card recoveredCard = manager.retrieveCard(card.getHash());
            Assert.assertEquals(card.getText(), recoveredCard.getText());
            manager.storeCard(card);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void doubleWriteReadTest(){
        try (CardSplitFilesStreamer manager = new CardSplitFilesStreamer(0x100000,baseDirectory, baseName)) {
            manager.storeCard(card);
            manager.storeCard(card2);
            Card recoveredCard = manager.retrieveCard(card.getHash());
            Card recoveredCard2 = manager.retrieveCard(card2.getHash());
            Assert.assertEquals(card.getText(), recoveredCard.getText());
            Assert.assertEquals(card2.getText(), recoveredCard2.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}