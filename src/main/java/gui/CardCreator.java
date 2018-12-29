package gui;

import core.Card;
import core.Cite;
import gui.locationtree.LocationTreeItem;
import gui.locationtree.LocationTreeItemContent;
import io.componentio.ComponentIOManager;
import io.componentio.mongodb.MongoDBComponentIOManager;
import io.structureio.StructureIOManager;
import io.structureio.mongodb.MongoDBStructureIOManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CardCreator{
    @FXML protected MenuItem menuNew;
    @FXML protected MenuItem menuSave;
    @FXML protected MenuItem menuQuit;
    @FXML protected Label currentPathLabel;
    @FXML protected TreeTableView directoryView;
    @FXML protected TextField authorField;
    @FXML protected TextField dateField;
    @FXML protected TextField additionalField;
    @FXML protected TextArea cardTextArea;

    private LocationTreeItem currentNode;
    private StringProperty currentPathString = new SimpleStringProperty("");

    ComponentIOManager componentIOManager;
    StructureIOManager structureIOManager;

    public void init(){
        componentIOManager = new MongoDBComponentIOManager();
        structureIOManager = new MongoDBStructureIOManager();
        populateDirectoryView();
        directoryView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldV, newV) -> {
            LocationTreeItem newItem = ((LocationTreeItem)newV);
            if (newItem==null){
                return;
            }
            if (newItem.isLeaf()){
                setCurrentNode((LocationTreeItem) newItem.getParent());
            }else{
                setCurrentNode(newItem);
            }
        });
        directoryView.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getClickCount() == 2) {
                LocationTreeItem node = (LocationTreeItem) directoryView.getSelectionModel().getSelectedItem();
                if (node != null && node.isLeaf()){
                    cardToFields((Card) node.getValue().getSpeechComponent());
                }
            }
        });
        currentPathLabel.textProperty().bind(currentPathString);
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
            root.getChildren().add(new LocationTreeItem(structureIOManager,componentIOManager, new LocationTreeItemContent(name)));
        }
        directoryView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        directoryView.setShowRoot(false);
        directoryView.setRoot(root);
    }

    @FXML
    public void saveCard() {
        if (currentNode == null){
            currentPathLabel.setText("Please select a location");
        }
        Card card = fieldsToCard();
        try {
            componentIOManager.storeSpeechComponent(card);
            structureIOManager.addContent(currentNode.getPath(),card.getHash());
            // TODO don't need to do a full reload here
            currentNode.reloadChildren();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void newCardAction(){
        authorField.clear();
        dateField.clear();
        additionalField.clear();
        cardTextArea.clear();
    }

    private Card fieldsToCard(){
        return new Card(new Cite(authorField.getText(), dateField.getText(), additionalField.getText()), cardTextArea.getText());
    }

    private void cardToFields(Card card){
        Cite cite = card.getCite();
        authorField.setText(cite.getAuthor());
        dateField.setText(cite.getDate());
        additionalField.setText(cite.getAdditionalInfo());
        cardTextArea.setText(card.getText());
    }

    public String getCurrentPathString() {
        return currentPathString.get();
    }

    public StringProperty currentPathStringProperty() {
        return currentPathString;
    }

    public LocationTreeItem getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(LocationTreeItem currentNode) {
        this.currentNode = currentNode;
        this.currentPathString.set(String.join("/",currentNode.getPath()));
    }
}
