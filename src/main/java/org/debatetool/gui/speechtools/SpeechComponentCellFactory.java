/*
 *                               This program is free software: you can redistribute it and/or modify
 *                               it under the terms of the GNU General Public License as published by
 *                                the Free Software Foundation, either version 3 of the License, or
 *                                (at your option) any later version.
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

package org.debatetool.gui.speechtools;

import org.debatetool.core.Block;
import org.debatetool.core.SpeechComponent;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpeechComponentCellFactory implements Callback<TreeView<SpeechComponent>, TreeCell<SpeechComponent>> {
    public static DataFormat speechComponentFormat = new DataFormat("SpeechComponent");
    private List<Class<? extends SpeechComponent>> acceptedTypes = new ArrayList<>();
    private boolean useAlphabet = false;
    public SpeechComponentCellFactory(boolean useAlphabet){
        this.useAlphabet = useAlphabet;
    }

    public SpeechComponentCellFactory(boolean useAlphabet, Class<? extends SpeechComponent>... types){
        this.useAlphabet = useAlphabet;
        for (Class<? extends SpeechComponent> type : types){
            acceptedTypes.add(type);
        }
    }
    @Override
    public TreeCell<SpeechComponent> call(TreeView<SpeechComponent> treeView) {
        TreeCell<SpeechComponent> cell = new TreeCell<>() {
            @Override
            protected void updateItem(SpeechComponent item, boolean empty) {
                super.updateItem(item, empty);
                if (item!=null && !empty) {
                    TreeItem<SpeechComponent> parent = getTreeItem().getParent();
                    int index = parent.getChildren().indexOf(this.getTreeItem());;

                    String indexCharacter;
                    if (useAlphabet){
                        indexCharacter = Block.toAlphabet(index);
                    }else {
                        if (parent.getParent() == null){
                            // parent is root - use numbers
                            indexCharacter = "" + (index + 1);
                        }else{
                            // otherwise we are part of a block, use letters
                            indexCharacter = Block.toAlphabet(index);
                        }
                    }
                    setText(indexCharacter+") "+item.getLabel());
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
        cell.setOnDragDone((DragEvent event) -> dragDone(event,cell));

        return cell;
    }

    private void dragDone(DragEvent event, TreeCell<SpeechComponent> cell) {
        if (event.isAccepted()) {
            /*
            So for some reason the first time you call this something is null that shouldn't be and everything breaks,
            so I call it once first because I don't have the energy to find out what javafx is screwing up this time.
            */
            cell.getTreeItem().getParent();
            cell.getTreeItem().getParent().getChildren().remove(cell.getTreeItem());
        }
        event.consume();
    }

    private void dragDetected(MouseEvent event, TreeCell<SpeechComponent> treeCell, TreeView<SpeechComponent> treeView) {
        Dragboard db = treeCell.startDragAndDrop(TransferMode.MOVE);

        ClipboardContent content = new ClipboardContent();
        content.put(speechComponentFormat, treeCell.getTreeItem().getValue());
        db.setContent(content);

        event.consume();
    }

    private void dragOver(DragEvent event, TreeCell<SpeechComponent> treeCell, TreeView<SpeechComponent> treeView) {
        SpeechComponent draggedItem = (SpeechComponent) event.getDragboard().getContent(speechComponentFormat);
        if (!event.getDragboard().getContent(speechComponentFormat).equals(treeCell.getTreeItem()) && (acceptedTypes.isEmpty() || acceptedTypes.contains(draggedItem.getClass()))){
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    private void drop(DragEvent event, TreeCell<SpeechComponent> treeCell, TreeView<SpeechComponent> treeView) {
        TreeItem<SpeechComponent> thisItem = treeCell.getTreeItem();
        SpeechComponent droppedItem = (SpeechComponent) event.getDragboard().getContent(speechComponentFormat);
        boolean success = false;

        // add to new location
        TreeItem<SpeechComponent> parent;
        if (treeCell.getTreeItem() == null){
            parent = treeView.getRoot();
        }else{
            parent = treeCell.getTreeItem().getParent();
        }
        int indexInParent =
                parent.getChildren().indexOf(thisItem);
        if (!treeCell.isEmpty() && indexInParent>=0){
            success = true;
            parent.getChildren().add(indexInParent,createTreeItem(droppedItem));
            treeView.getSelectionModel().select(indexInParent);
        }else if (treeCell.isEmpty()){
            // dropped past the end, just put it at the end of parent
            success = true;
            treeView.getRoot().getChildren().add(createTreeItem(droppedItem));
            treeView.getSelectionModel().select(parent.getChildren().size()-1);
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void dragEntered (DragEvent event, TreeCell<SpeechComponent> cell){
        if (event.getGestureSource() != cell &&
                event.getDragboard().hasString()) {
            cell.setTextFill(Color.GREEN);
        }
        event.consume();
    }

    private void dragExited(DragEvent event, TreeCell<SpeechComponent> cell) {
        if (event.getGestureSource() != cell &&
                event.getDragboard().hasString()) {
            cell.setTextFill(Color.BLACK);
        }
        event.consume();
    }

    /**
     * Creates a TreeItem that represents the component, but for speeches it will create an item with children
     * @param component
     * @return
     */
    public static TreeItem<SpeechComponent> createTreeItem(SpeechComponent component){
        TreeItem item = new TreeItem<>(component);
        if (!component.isLoaded()){
            try {
                component.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (component.getClass().isAssignableFrom(Block.class)){
            Block block = (Block) component;
            for (int i = 0; i < block.size(); i++){
                item.getChildren().add(new TreeItem<>(block.getComponent(i)));
            }
        }
        return item;
    }
}
