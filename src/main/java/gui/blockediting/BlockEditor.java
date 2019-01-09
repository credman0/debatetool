package gui.blockediting;

import core.Block;
import core.BlockComponent;
import core.Card;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import java.io.IOException;

public class BlockEditor {
    @FXML protected
    ScrollPane scrollpane;
    @FXML protected
    BorderPane mainPane;
    @FXML protected
    TreeView blockTreeView;
    @FXML protected
    GridPane viewerArea;
    Block block;
    final static String WEBVIEW_HTML = BlockEditor.class.getClassLoader().getResource("BlockViewer.html").toExternalForm();

    public void open(Block block) {
        if (!block.isLoaded()){
            try {
                block.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.block = block;
        populateTree();
        generateContents();
    }

    private void generateContents(){
        for (int i = 0; i < block.size(); i++){
            VBox componentBox = new VBox();
            WebView blockContentsView = new WebView();
            blockContentsView.getEngine().load(WEBVIEW_HTML);
            final int index = i;
            blockContentsView.getEngine().getLoadWorker().stateProperty().addListener(new ContentLoader(block,i,blockContentsView));
            blockContentsView.setDisable(true);
            if (block.getComponent(i).getClass().isAssignableFrom(Card.class)){
                ComboBox<String> tagsBox = new ComboBox<>();
                tagsBox.setItems(FXCollections.observableList(((Card) block.getComponent(i)).getTags()));
                componentBox.getChildren().add(tagsBox);
            }
            componentBox.getChildren().add(blockContentsView);
            viewerArea.add(componentBox,0,i);
        }
    }

    private class ContentLoader implements ChangeListener<Worker.State>{
        private final Block block;
        private final int index;
        private final WebView contentView;

        private ContentLoader(Block block, int index, WebView contentView) {
            this.block = block;
            this.index = index;
            this.contentView = contentView;
        }

        @Override
        public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState) {
            if (newState == Worker.State.SUCCEEDED) {
                contentView.getEngine().executeScript("document.getElementById('textarea').innerHTML = \""+block.getComponent(index).getDisplayContent()+"\";");
            }
        }
    }

    public Pane getPane() {
        return mainPane;
    }

    public void init() {
        blockTreeView.setCellFactory(new BlockComponentCellFactory());
    }

    private void populateTree(){
        TreeItem<BlockComponent> root = new TreeItem<>();
        for (int i = 0; i < block.size(); i++){
            root.getChildren().add(new TreeItem<>(block.getComponent(i)));
        }
        blockTreeView.setRoot(root);
    }
}
