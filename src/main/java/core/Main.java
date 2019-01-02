package core;

import gui.cardediting.CardCreatorLauncher;
import io.iocontrollers.IOController;
import io.iocontrollers.mongodb.MongoDBIOController;
import javafx.application.Application;

public class Main {
    private static IOController ioController = new MongoDBIOController();
    public static void main(String[] args) {
        Application.launch(CardCreatorLauncher.class, args);
    }
    public static IOController getIoController(){
        return ioController;
    }
}
