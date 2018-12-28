package gui.locationtree;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public abstract class LocationTreeManager {
    abstract ObservableList<TreeItem<LocationTreeItemContent>> getChildren(LocationTreeItem item);
    public LocationTreeItem createItem(LocationTreeItemContent content){
        return new LocationTreeItem(this, content);
    }
}
