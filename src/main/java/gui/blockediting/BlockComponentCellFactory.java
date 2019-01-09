package gui.blockediting;

import core.BlockComponent;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.*;
import javafx.util.Callback;

public class BlockComponentCellFactory implements Callback<TreeView<BlockComponent>, TreeCell<BlockComponent>> {
    private static final DataFormat JAVA_FORMAT = new DataFormat("application/x-java-serialized-object");
    private static final String DROP_HINT_STYLE = "-fx-border-color: #eea82f; -fx-border-width: 0 0 2 0; -fx-padding: 3 3 1 3";
    private TreeCell<BlockComponent> dropZone;
    private TreeItem<BlockComponent> draggedItem;

    @Override
    public TreeCell<BlockComponent> call(TreeView<BlockComponent> treeView) {
        TreeCell<BlockComponent> cell = new TreeCell<BlockComponent>() {
            @Override
            protected void updateItem(BlockComponent item, boolean empty) {
                super.updateItem(item, empty);
                if (item!=null) {
                    setText(item.getLabel());
                }

            }
        };
        cell.setOnDragDetected((MouseEvent event) -> dragDetected(event, cell, treeView));
        cell.setOnDragOver((DragEvent event) -> dragOver(event, cell, treeView));
        cell.setOnDragDropped((DragEvent event) -> drop(event, cell, treeView));
        cell.setOnDragDone((DragEvent event) -> clearDropLocation());

        return cell;
    }

    private void dragDetected(MouseEvent event, TreeCell<BlockComponent> treeCell, TreeView<BlockComponent> treeView) {
        draggedItem = treeCell.getTreeItem();

        // root can't be dragged
        if (draggedItem.getParent() == null) return;
        Dragboard db = treeCell.startDragAndDrop(TransferMode.MOVE);

        ClipboardContent content = new ClipboardContent();
        content.putString("Empty");
        db.setContent(content);
        db.setDragView(treeCell.snapshot(null, null));
        event.consume();
    }

    private void dragOver(DragEvent event, TreeCell<BlockComponent> treeCell, TreeView<BlockComponent> treeView) {
        event.acceptTransferModes(TransferMode.MOVE);
        clearDropLocation();
        this.dropZone = treeCell;
        dropZone.setStyle(DROP_HINT_STYLE);
        System.out.println(dropZone);
        event.consume();
    }

    private void drop(DragEvent event, TreeCell<BlockComponent> treeCell, TreeView<BlockComponent> treeView) {
        if (draggedItem == null) return;

        TreeItem<BlockComponent> thisItem = treeCell.getTreeItem();
        TreeItem<BlockComponent> droppedItemParent = treeView.getRoot();

        // remove from previous location
        droppedItemParent.getChildren().remove(draggedItem);

        // add to new location
        TreeItem root = treeView.getRoot();
        int indexInParent = root.getChildren().indexOf(thisItem);
        treeView.getRoot().getChildren().add(indexInParent + 1, draggedItem);
        treeView.getSelectionModel().select(draggedItem);
        event.setDropCompleted(true);
    }

    private void clearDropLocation() {
        if (dropZone != null) dropZone.setStyle("");
    }
}
