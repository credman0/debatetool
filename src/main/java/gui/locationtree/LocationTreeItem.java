package gui.locationtree;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;


public class LocationTreeItem extends TreeItem<LocationTreeItemContent> {
    protected final LocationTreeManager manager;
    protected ObservableList<TreeItem<LocationTreeItemContent>> children;

    /**
     * Create a tree branch that contains children that are potentially either leaves, or branches that contain leaves.
     * @param manager Manager to be used for retrieving children when necessary.
     */
    protected LocationTreeItem(LocationTreeManager manager, LocationTreeItemContent content){
        super();
        this.manager = manager;
        this.setValue(content);
    }

    @Override
    public ObservableList<TreeItem<LocationTreeItemContent>> getChildren(){
        if (children == null) {
            children = manager.getChildren(this);
        }
        return children;
    }


    @Override
    public boolean isLeaf(){
        return getValue().card!=null;
    }
}
