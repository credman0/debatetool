/*
 *                               This program is free software: you can redistribute it and/or modify
 *                                it under the terms of the GNU General Public License as published by
 *                                the Free Software Foundation, version 3 of the License.
 *
 *                                This program is distributed in the hope that it will be useful,
 *                                but WITHOUT ANY WARRANTY; without even the implied warranty of
 *                                MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *                                GNU General Public License for more details.
 *
 *                                You should have received a copy of the GNU General Public License
 *                                along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *                                Copyright (c) 2019 Colin Redman
 */

package io.mongodb;

import org.debatetool.core.Card;
import org.debatetool.core.Cite;
import org.debatetool.io.componentio.ComponentIOManager;
import org.debatetool.io.iocontrollers.IOController;
import org.junit.AfterClass;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;

class MongoDBCardIOManagerTest {
    Card card;
    Card card2;

    @BeforeClass
    public void setUp(){
        IOController.getIoController().attemptInitialize("127.0.0.1", 27017, null,null);
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
        try {
            ComponentIOManager manager = IOController.getIoController().getComponentIOManager();
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
        try  {
            ComponentIOManager manager = IOController.getIoController().getComponentIOManager();
            manager.deleteSpeechComponent(card.getHash());
            manager.deleteSpeechComponent(card2.getHash());
            manager.storeSpeechComponent(card);
            manager.storeSpeechComponent(card2);
            Card recoveredCard = (Card) manager.retrieveSpeechComponent(card.getHash());
            Card recoveredCard2 = (Card) manager.retrieveSpeechComponent(card2.getHash());
            Assert.assertEquals(card, recoveredCard);
            Assert.assertEquals(card2, recoveredCard2);
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