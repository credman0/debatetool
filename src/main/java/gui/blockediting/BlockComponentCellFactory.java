package gui.blockediting;

import core.Block;
import core.BlockComponent;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class BlockComponentCellFactory implements Callback<TreeView<BlockComponent>, TreeCell<BlockComponent>> {
    public static DataFormat blockComponentFormat = new DataFormat("BlockComponent");
    @Override
    public TreeCell<BlockComponent> call(TreeView<BlockComponent> treeView) {
        TreeCell<BlockComponent> cell = new TreeCell<>() {
            @Override
            protected void updateItem(BlockComponent item, boolean empty) {
                super.updateItem(item, empty);
                if (item!=null && !empty) {
                    int index = treeView.getRoot().getChildren().indexOf(this.getTreeItem());
                    setText(Block.toAlphabet(index)+") "+item.getLabel());
                } else{
                    setText(null);
                }
            }
        };
        cell.setOnDragDetected((MouseEvent event) -> dragDetected(event, cell, treeView));
        cell.setOnDragOver((DragEvent event) -> dragOver(event, cell, treeView));
        cell.setOnDragDropped((DragEvent event) -> drop(event, cell, treeView));
        cell.setOnDragEntered((DragEvent event) -> dragEntered(event,cell));
        cell.setOnDragExited((DragEvent event) -> dragExited(event,cell));
        cell.setOnDragDone((DragEvent event) -> dragDone(event,cell,treeView));

        return cell;
    }

    private void dragDone(DragEvent event, TreeCell<BlockComponent> cell, TreeView<BlockComponent> treeView) {
        if (event.isAccepted()) {
            treeView.getRoot().getChildren().remove(cell.getTreeItem());
        }
        event.consume();
    }

    private void dragDetected(MouseEvent event, TreeCell<BlockComponent> treeCell, TreeView<BlockComponent> treeView) {
        Dragboard db = treeCell.startDragAndDrop(TransferMode.MOVE);

        ClipboardContent content = new ClipboardContent();
        content.put(blockComponentFormat, treeCell.getTreeItem().getValue());
        db.setContent(content);

        event.consume();
    }

    private void dragOver(DragEvent event, TreeCell<BlockComponent> treeCell, TreeView<BlockComponent> treeView) {
        if (!event.getDragboard().getContent(blockComponentFormat).equals(treeCell.getTreeItem())){
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    private void drop(DragEvent event, TreeCell<BlockComponent> treeCell, TreeView<BlockComponent> treeView) {
        TreeItem<BlockComponent> thisItem = treeCell.getTreeItem();
        BlockComponent droppedItem = (BlockComponent) event.getDragboard().getContent(blockComponentFormat);
        boolean success = false;

        // add to new location
        TreeItem root = treeView.getRoot();
        int indexInParent = root.getChildren().indexOf(thisItem);
        if (!treeCell.isEmpty() && indexInParent>=0){
            success = true;
            treeView.getRoot().getChildren().add(indexInParent,new TreeItem<>(droppedItem));
            treeView.getSelectionModel().select(indexInParent);
        }else if (treeCell.isEmpty()){
            // dropped past the end, just put it at the end of parent
            success = true;
            treeView.getRoot().getChildren().add(new TreeItem<>(droppedItem));
            treeView.getSelectionModel().select(root.getChildren().size()-1);
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void dragEntered (DragEvent event, TreeCell<BlockComponent> cell){
        if (event.getGestureSource() != cell &&
                event.getDragboard().hasString()) {
            cell.setTextFill(Color.GREEN);
        }
        event.consume();
    }

    private void dragExited(DragEvent event, TreeCell<BlockComponent> cell) {
        if (event.getGestureSource() != cell &&
                event.getDragboard().hasString()) {
            cell.setTextFill(Color.BLACK);
        }
        event.consume();
    }
}
