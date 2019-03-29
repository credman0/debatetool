package gui;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.PreferencesFxEvent;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;

import java.io.*;
import java.util.Properties;

public class SettingsHandler {
    private static StringProperty mongoIP = new SimpleStringProperty("");
    private static final String DEFAULT_MONGO_IP = "127.0.0.1";
    private static StringProperty mongoPort = new SimpleStringProperty("");
    private static final String DEFAULT_MONGO_PORT = "27017";
    private static StringProperty username = new SimpleStringProperty("");
    private static StringProperty password = new SimpleStringProperty("");
    private static Properties properties = new Properties();
    private static PreferencesFx preferencesFx;
    static{
        if (new File("config.properties").exists()) {
            try {
                InputStream in = new FileInputStream("config.properties");
                properties.load(in);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            properties = new Properties();
        }

        String portString = properties.getProperty("mongod_port");
        if (portString == null){
            // TODO standardize default properties somewhere
            mongoPort.setValue(DEFAULT_MONGO_PORT);
            properties.setProperty("mongod_port", DEFAULT_MONGO_PORT);
        }else{
            mongoPort.setValue(portString);
        }

        String ipString = properties.getProperty("mongod_ip");
        if (ipString == null){
            // TODO standardize default properties somewhere
            mongoIP.setValue(DEFAULT_MONGO_IP);
            properties.setProperty("mongod_ip", DEFAULT_MONGO_IP);
        }else{
            mongoIP.setValue(ipString);
        }

        username.set(properties.getProperty("username"));
        password.set(properties.getProperty("password"));

        preferencesFx =
                PreferencesFx.of(SettingsHandler.class,
                        Category.of("Mongo Database Settings",
                                Group.of("Server",
                                        Setting.of("Mongodb IP", mongoIP),
                                        Setting.of("Mongodb Port", mongoPort)
                                ),
                                Group.of("Account",
                                        Setting.of("Username",username),
                                        Setting.of("Password",password))
                        )
                ).addEventHandler(PreferencesFxEvent.EVENT_PREFERENCES_SAVED, new EventHandler<PreferencesFxEvent>() {
                    @Override
                    public void handle(PreferencesFxEvent preferencesFxEvent) {
                        try {
                            saveChanges();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
    public static void showDialog(){
        preferencesFx.show();
    }

    public static String getSetting(String name){
        return properties.getProperty(name);
    }

    private static void saveChanges() throws IOException {
        boolean changed = false;
        String mongoIPProperty = properties.getProperty("mongod_ip");
        if (mongoIPProperty==null || !mongoIP.getValue().equals(mongoIPProperty)) {
            properties.put("mongod_ip", mongoIP.getValue());
            changed = true;
        }
        String mongoPortProperty = properties.getProperty("mongod_port");
        if (mongoPortProperty==null || !mongoPort.getValue().equals(mongoIPProperty)) {
            properties.put("mongod_port", mongoPort.getValue());
            changed = true;
        }
        String usernameProperty = properties.getProperty("username");
        if (usernameProperty==null || !username.getValue().equals(usernameProperty)) {
            properties.put("username", username.getValue());
            changed = true;
        }
        // TODO do not store this in plaintext
        String passwordProperty = properties.getProperty("password");
        if (passwordProperty==null || !password.getValue().equals(passwordProperty)) {
            properties.put("password", password.getValue());
            changed = true;
        }
        if (changed) {
            OutputStream out = new FileOutputStream("config.properties");
            properties.store(out, null);
        }
    }
}
