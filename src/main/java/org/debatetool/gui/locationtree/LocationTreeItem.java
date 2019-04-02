package org.debatetool.gui.locationtree;

import org.debatetool.core.HashIdentifiedSpeechComponent;
import org.debatetool.gui.cardediting.MainGui;
import org.debatetool.io.iocontrollers.IOController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LocationTreeItem extends TreeItem<LocationTreeItemContent> {
    private boolean childrenLoaded = false ;
    public final static Image DIRECTORY_CLOSED = new Image(LocationTreeItem.class.getResource("/icons/Places-folder-icon.png").toExternalForm());
    public final static Image DIRECTORY_OPEN = new Image(LocationTreeItem.class.getResource("/icons/Places-folder-empty-icon.png").toExternalForm());
    public final static Image LETTER_B = new Image(LocationTreeItem.class.getResource("/icons/Letter-B-blue-icon.png").toExternalForm());
    public final static Image LETTER_C = new Image(LocationTreeItem.class.getResource("/icons/Letter-C-pink-icon.png").toExternalForm());
    public final static Image LETTER_S = new Image(LocationTreeItem.class.getResource("/icons/Letter-S-lg-icon.png").toExternalForm());
    private SimpleBooleanProperty loadingProperty = new SimpleBooleanProperty(false);

    public void addLoadingListener(ChangeListener<Boolean> listener){
        loadingProperty.addListener(listener);
    }

    public void removeLoadingListener(ChangeListener<Boolean> listener){
        loadingProperty.removeListener(listener);
    }

    public boolean isChildrenLoaded() {
        return childrenLoaded;
    }

    /**
     * Create a tree branch that contains children that are potentially either leaves, or branches that contain leaves.
     * @param content
     */
    public LocationTreeItem(LocationTreeItemContent content){
        super();
        this.setValue(content);

    }

    @Override
    public ObservableList<TreeItem<LocationTreeItemContent>> getChildren(){
        if (childrenLoaded){
            return super.getChildren();
        }
        childrenLoaded = true;
        // cannot be accessed from within trhead if not declared here
        ObservableList<TreeItem<LocationTreeItemContent>> superChildren = super.getChildren();
        Task task = new Task<>() {
            @Override
            protected Object call() throws Exception {
                MainGui.getActiveGUI().getScene().getRoot().setCursor(Cursor.WAIT);
                List<TreeItem<LocationTreeItemContent>> children = new ArrayList<>();
                List<String> path = getPath();
                List<String> childrenDirs = IOController.getIoController().getStructureIOManager().getChildren(path);
                List<HashIdentifiedSpeechComponent> contents = null;
                try {
                    contents = IOController.getIoController().getStructureIOManager().getContent(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (String name:childrenDirs){
                    children.add(new LocationTreeItem(new LocationTreeItemContent(name)));
                }
                for (HashIdentifiedSpeechComponent component :contents){
                    children.add(new LocationTreeItem(new LocationTreeItemContent(component)));
                }
                MainGui.getActiveGUI().getScene().getRoot().setCursor(Cursor.DEFAULT);
                superChildren.addAll(children);
                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        return super.getChildren();
    }

    public void reloadChildren(){
        getChildren().clear();
        childrenLoaded = false;
        if (isExpanded()){
            // need to reload right away
            getChildren();
        }
    }

    public void reloadChildrenRecursive(){
        if (!childrenLoaded || isLeaf()){
            return;
        }
        for (TreeItem<LocationTreeItemContent> child:getChildren()){
            ((LocationTreeItem)child).reloadChildrenRecursive();
        }
        reloadChildren();
    }

    // TODO this method is inefficient on several levels - will matter with a large tree
    public boolean contains(List<String> path){
        if (isLeaf()){
            return getPath().equals(path);
        }
        if (isChildrenLoaded()) {
            for (TreeItem<LocationTreeItemContent> child : getChildren()) {
                if (((LocationTreeItem) child).getPath().equals(path) || ((LocationTreeItem) child).contains(path)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public boolean isLeaf(){
        return getValue().getSpeechComponent()!=null;
    }

    public List<String> getPath(){
        if (getParent() == null){
            return new ArrayList<>();
        }else if (getParent().getParent()==null){
            List<String> path = new ArrayList<>();
            path.add(getValue().toString());
            return path;
        }else{
            List<String> parentPath = ((LocationTreeItem) getParent()).getPath();
            parentPath.add(getValue().toString());
            return parentPath;
        }
    }
}
