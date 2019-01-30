package gui.locationtree;

import core.HashIdentifiedSpeechComponent;
import io.iocontrollers.IOController;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LocationTreeItem extends TreeItem<LocationTreeItemContent> {
    private boolean childrenLoaded = false ;

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
        List<TreeItem<LocationTreeItemContent>> children = new ArrayList<>();
        List<String> path = getPath();
        List<String> childrenDirs = IOController.getIoController().getStructureIOManager().getChildren(path);
        List<byte[]> contentIDs = IOController.getIoController().getStructureIOManager().getContent(path);
        for (String name:childrenDirs){
            children.add(new LocationTreeItem(new LocationTreeItemContent(name)));
        }
        // TODO use group fetch function of io manager
        for (byte[] hash:contentIDs) {
            HashIdentifiedSpeechComponent content = null;
            try {
                content = IOController.getIoController().getComponentIOManager().retrieveSpeechComponent(hash);
            } catch (IOException e) {
                e.printStackTrace();
            }
            children.add(new LocationTreeItem(new LocationTreeItemContent(content)));
        }
        super.getChildren().addAll(children);
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
