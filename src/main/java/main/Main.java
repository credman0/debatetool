package main;

import core.Card;
import core.Cite;
import gui.cardediting.CardCreatorLauncher;
import javafx.application.Application;
import org.python.core.PyException;
import scripting.JythonScripter;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Application.launch(CardCreatorLauncher.class, args);
        try {
            JythonScripter.runScript("hello_world.py", new Card(new Cite("Colin", "2020", "Mad man"),"Great stuff"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PyException e){
            e.printStackTrace();
        }
    }
}
