package core;

import gui.cardediting.CardCreatorLauncher;
import io.iocontrollers.IOController;
import io.iocontrollers.mongodb.MongoDBIOController;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        Application.launch(CardCreatorLauncher.class, args);
    }
}
