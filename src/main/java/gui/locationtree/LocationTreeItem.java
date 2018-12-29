package gui.locationtree;

import core.HashIdentifiedSpeechComponent;
import io.componentio.ComponentIOManager;
import io.structureio.StructureIOManager;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LocationTreeItem extends TreeItem<LocationTreeItemContent> {
    private boolean childrenLoaded = false ;
    protected final StructureIOManager structureIOManager;
    protected final ComponentIOManager componentIOManager;

    /**
     * Create a tree branch that contains children that are potentially either leaves, or branches that contain leaves.
     * @param structureIOManager Manager to be used for lazily retrieving children when necessary.
     * @param content
     */
    public LocationTreeItem(StructureIOManager structureIOManager, ComponentIOManager componentIOManager, LocationTreeItemContent content){
        super();
        this.structureIOManager = structureIOManager;
        this.componentIOManager = componentIOManager;
        this.setValue(content);
    }

    @Override
    public ObservableList<TreeItem<LocationTreeItemContent>> getChildren(){
        if (childrenLoaded){
            return super.getChildren();
        }
        childrenLoaded = true;
        List<TreeItem<LocationTreeItemContent>> children = new ArrayList<>();
        List<String> path = getPath();
        List<String> childrenDirs = structureIOManager.getChildren(path);
        List<byte[]> contentIDs = structureIOManager.getContent(path);
        for (String name:childrenDirs){
            children.add(new LocationTreeItem(structureIOManager,componentIOManager, new LocationTreeItemContent(name)));
        }
        for (byte[] hash:contentIDs) {
            HashIdentifiedSpeechComponent content = null;
            try {
                content = componentIOManager.retrieveSpeechComponent(hash);
            } catch (IOException e) {
                e.printStackTrace();
            }
            children.add(new LocationTreeItem(structureIOManager, componentIOManager, new LocationTreeItemContent(content)));
        }
        super.getChildren().addAll(children);
        return super.getChildren();
    }


    @Override
    public boolean isLeaf(){
        return getValue().getSpeechComponent()!=null;
    }

    public List<String> getPath(){
        if (getParent().getParent()==null){
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
