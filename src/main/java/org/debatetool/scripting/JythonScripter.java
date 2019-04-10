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

package org.debatetool.scripting;

import org.debatetool.core.SpeechComponent;
import org.debatetool.gui.cardediting.MainGui;
import org.debatetool.io.iocontrollers.IOController;
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
        interp.set("GUI", MainGui.getActiveGUI());
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
