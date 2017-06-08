package com.ggl.filebrowser.view;

import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTree;

import com.ggl.filebrowser.controller.FileDeletionListener;
import com.ggl.filebrowser.controller.FileSelectionListener;
import com.ggl.filebrowser.controller.TreeExpandListener;
import com.ggl.filebrowser.model.BrowserModel;
import com.ggl.filebrowser.model.NativeFileBrowserModel;
import com.ggl.filebrowser.model.SyncListBrowserModel;
import com.ggl.filebrowser.view.renderer.FileTreeCellRenderer;
 
public class TreeScrollPane {

    private JScrollPane scrollPane;
    private BrowserModel model;
    private CenterButtonPanel centerPanel;
    private JTree tree;
 
    public TreeScrollPane(BrowserModel model, CenterButtonPanel centerPanel) {
        this.model = model;
        this.centerPanel = centerPanel;
        setMainScrollPane();
    }
     
    private void setMainScrollPane() {
        tree = new JTree(model.createTreeModel());
        if (model.isGoingToSync())
        	tree.addTreeSelectionListener(new FileSelectionListener(model, centerPanel));
        else
        	tree.addTreeSelectionListener(new FileDeletionListener(model, centerPanel));
        tree.addTreeWillExpandListener(new TreeExpandListener(model));
        tree.expandRow(1);
        tree.setRootVisible(false);
        tree.setCellRenderer(new FileTreeCellRenderer(model));
        tree.setShowsRootHandles(true);
        
        // We always want to expand the whole sync list. 
        model.setJTree(tree);

        scrollPane = new JScrollPane(tree);
        Dimension preferredSize = scrollPane.getPreferredSize();
        Dimension widePreferred = new Dimension(300, (int) preferredSize.getHeight());
        scrollPane.setPreferredSize(widePreferred);
    }
 
    public JTree getTree() {
        return tree;
    }
 
    public JScrollPane getScrollPane() {
        return scrollPane;
    }
     
}