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
    private static Properties properties = new Properties();
    private static PreferencesFx preferencesFx;
    static{
        try {
            InputStream in = new FileInputStream("config.properties");
            properties.load(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String ipString = properties.getProperty("mongod_ip");
        if (ipString == null){
            // TODO standardize default properties somewhere
            mongoIP.setValue("127.0.0.1");
            properties.setProperty("mongod_ip", "127.0.0.1");
        }else{
            mongoIP.setValue(ipString);
        }

        preferencesFx =
                PreferencesFx.of(SettingsHandler.class,
                        Category.of("Category Title",
                                Group.of("Group Title",
                                        Setting.of("Setting Title", mongoIP)
                                )
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

    public static Object getSetting(String name){
        return properties.getProperty(name);
    }

    private static void saveChanges() throws IOException {
        boolean changed = false;
        String mongoIPProperty = properties.getProperty("mongod_ip");
        if (mongoIPProperty==null || !mongoIP.getValue().equals(mongoIPProperty)) {
            properties.put("mongod_ip", mongoIP.getValue());
            changed = true;
        }
        if (changed) {
            OutputStream out = new FileOutputStream("config.properties");
            properties.store(out, null);
        }
    }
}
