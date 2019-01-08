package gui.blockediting;

import core.Block;
import javafx.fxml.FXML;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;

import java.io.IOException;

public class BlockEditor {
    @FXML protected
    BorderPane mainPane;
    @FXML protected
    TreeView blockTreeView;
    @FXML protected
    WebView viewerArea;
    Block block;

    public void open(Block block) {
        if (!block.isLoaded()){
            try {
                block.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        viewerArea.getEngine().executeScript("document.getElementById('textarea').innerHTML = \""+block.getDisplayContent()+"\";");
    }

    public Pane getPane() {
        return mainPane;
    }

    public void init() {

        viewerArea.getEngine().load(getClass().getClassLoader().getResource("BlockViewer.html").toExternalForm());
    }
}
