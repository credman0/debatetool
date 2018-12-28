package gui.locationtree;

import core.HashIdentifiedSpeechComponent;
import io.componentio.ComponentIOManager;
import io.structureio.StructureIOManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LocationTreeItem extends TreeItem<LocationTreeItemContent> {
    protected final StructureIOManager structureIOManager;
    protected final ComponentIOManager componentIOManager;
    protected final LocationTreeItem parent;
    protected ObservableList<TreeItem<LocationTreeItemContent>> children;

    /**
     * Create a tree branch that contains children that are potentially either leaves, or branches that contain leaves.
     * @param structureIOManager Manager to be used for lazily retrieving children when necessary.
     * @param parent
     * @param content
     */
    public LocationTreeItem(StructureIOManager structureIOManager, ComponentIOManager componentIOManager, LocationTreeItem parent, LocationTreeItemContent content){
        super();
        this.structureIOManager = structureIOManager;
        this.componentIOManager = componentIOManager;
        this.parent = parent;
        this.setValue(content);
    }

    @Override
    public ObservableList<TreeItem<LocationTreeItemContent>> getChildren(){
        if (children == null) {
            List<String> path = getPath();
            List<String> childrenDirs = structureIOManager.getChildren(path);
            List<byte[]> contentIDs = structureIOManager.getContent(path);
            children = FXCollections.observableArrayList();
            for (String name:childrenDirs){
                children.add(new LocationTreeItem(structureIOManager,componentIOManager, this, new LocationTreeItemContent(name)));
            }
            for (byte[] hash:contentIDs) {
                HashIdentifiedSpeechComponent content = null;
                try {
                    content = componentIOManager.retrieveSpeechComponent(hash);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                children.add(new LocationTreeItem(structureIOManager, componentIOManager, this, new LocationTreeItemContent(content)));
            }
        }
        return children;
    }


    @Override
    public boolean isLeaf(){
        return getValue().getSpeechComponent()!=null;
    }

    public List<String> getPath(){
        if (parent==null){
            return new ArrayList<>();
        }else{
            List<String> parentPath = parent.getPath();
            parentPath.add(getValue().toString());
            return parentPath;
        }
    }
}
