package gui;

import core.Card;
import core.Cite;
import gui.locationtree.LocationTreeItem;
import gui.locationtree.LocationTreeItemContent;
import io.componentio.ComponentIOManager;
import io.componentio.mongodb.MongoDBComponentIOManager;
import io.structureio.StructureIOManager;
import io.structureio.mongodb.MongoDBStructureIOManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CardCreator{
    @FXML protected TreeTableView directoryView;
    @FXML protected TextField authorField;
    @FXML protected TextField dateField;
    @FXML protected TextField additionalField;
    @FXML protected TextArea cardTextArea;

    ComponentIOManager componentIOManager;
    StructureIOManager structureIOManager;

    public void init(){
        componentIOManager = new MongoDBComponentIOManager();
        structureIOManager = new MongoDBStructureIOManager();
        populateDirectoryView();
    }

    @FXML
    public void handleMenuKeyInput(KeyEvent keyEvent) {

    }

    private void populateDirectoryView(){
        List<String> rootFolders = structureIOManager.getRoot();
        TreeItem<LocationTreeItemContent> root = new TreeItem<>();
        TreeTableColumn<LocationTreeItemContent, String> labelColumn = new TreeTableColumn<>("Name");
        TreeTableColumn<LocationTreeItemContent, Date> timeColumn = new TreeTableColumn<>("Date");
        directoryView.getColumns().setAll(labelColumn,timeColumn);
        labelColumn.setCellValueFactory(new TreeItemPropertyValueFactory("display"));
        timeColumn.setCellFactory(column -> {
            TreeTableCell<LocationTreeItemContent, Date> cell = new TreeTableCell<>() {
                private SimpleDateFormat format = new SimpleDateFormat("MMM dd h:mm a ''yy");
                @Override
                protected void updateItem(Date item, boolean empty) {
                    super.updateItem(item, empty);
                    if(empty || item==null || item.getTime()<=0) {
                        setText(null);
                    } else {
                        setText(format.format(item));
                    }
                }
            };

            return cell;
        });
        timeColumn.setCellValueFactory(new TreeItemPropertyValueFactory("date"));

        for (String name:rootFolders){
            root.getChildren().add(new LocationTreeItem(structureIOManager,componentIOManager, null, new LocationTreeItemContent(name)));
        }
        directoryView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        directoryView.setShowRoot(false);
        directoryView.setRoot(root);
    }

    @FXML
    public void saveCard(ActionEvent actionEvent) {
        Card card = new Card(new Cite(authorField.getText(), dateField.getText(), additionalField.getText()), cardTextArea.getText());
        try {
            componentIOManager.storeSpeechComponent(card);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
