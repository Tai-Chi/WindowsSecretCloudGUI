package com.ggl.filebrowser.runnable;

import javax.swing.tree.DefaultMutableTreeNode;

import com.ggl.filebrowser.model.BrowserModel;
import com.ggl.filebrowser.model.NativeFileBrowserModel;
import com.ggl.filebrowser.model.FileNode;
 
public class AddNodes implements Runnable {
     
    private DefaultMutableTreeNode node;
     
    private BrowserModel model;
 
    public AddNodes(BrowserModel model, DefaultMutableTreeNode node) {
        this.model = model;
        this.node = node;
    }
 
    @Override
    public void run() {
        FileNode fileNode = (FileNode) node.getUserObject();
        if (fileNode.isGenerateGrandchildren()) {
            model.addGrandchildNodes(node);
            fileNode.setGenerateGrandchildren(false);
        }
    }
     
}