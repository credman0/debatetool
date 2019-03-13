package scripting;

import core.SpeechComponent;
import gui.cardediting.CardCreator;
import io.iocontrollers.IOController;
import org.python.core.PySyntaxError;
import org.python.util.PythonInterpreter;

import java.io.*;

public class JythonScripter {
    public static final String SCRIPT_PATH = "scripts";
    public static void runScript(String name, SpeechComponent current) throws IOException {
        PythonInterpreter interp = new PythonInterpreter();
        // TODO this preloading should happen out of a dictionary
        interp.set("component", current);
        interp.set("structureManager", IOController.getIoController().getStructureIOManager());
        interp.set("componentManager", IOController.getIoController().getComponentIOManager());
        interp.set("overlayManager", IOController.getIoController().getOverlayIOManager());
        interp.set("GUI", CardCreator.getActiveGUI());
        try(BufferedReader reader = new BufferedReader(new FileReader(SCRIPT_PATH+"/"+name))) {
            String line = reader.readLine();
            while (line != null) {
                interp.exec(line);
                line = reader.readLine();
            }
        }catch(PySyntaxError e){
            e.printStackTrace();
        }
    }

    public static String[] getScripts() {
        return new File(SCRIPT_PATH).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String extension = "";

                int i = name.lastIndexOf('.');
                if (i > 0) {
                    extension = name.substring(i+1);
                }
                return extension.equals("py");
            }
        });
    }
}
