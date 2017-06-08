package com.ggl.filebrowser.controller;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.ggl.filebrowser.model.BrowserModel;
import com.ggl.filebrowser.model.NativeFileBrowserModel;
import com.ggl.filebrowser.model.SyncListBrowserModel;
import com.ggl.filebrowser.runnable.AddNodes;
import com.ggl.filebrowser.view.CenterButtonPanel;
import com.ggl.filebrowser.view.FileBrowserFrame;
 
public class FileSelectionListener implements TreeSelectionListener {

    private BrowserModel model;
    private CenterButtonPanel centerPanel;
 
    public FileSelectionListener(BrowserModel model, CenterButtonPanel centerPanel) {
        this.model = model;
        this.centerPanel = centerPanel;
    }
 
    @Override
    public void valueChanged(TreeSelectionEvent event) {
    	centerPanel.setSyncPath(event.getPath());
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
        AddNodes addNodes = new AddNodes(model, node);
        new Thread(addNodes).start();
    }
}