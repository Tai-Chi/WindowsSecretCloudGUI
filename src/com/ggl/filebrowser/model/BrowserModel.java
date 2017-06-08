package com.ggl.filebrowser.model;

import java.io.File;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public interface BrowserModel {
	void addGrandchildNodes(DefaultMutableTreeNode node);
	boolean isGoingToSync();
	Icon getFileIcon(File file);
	String getFileText(File file);
	DefaultTreeModel createTreeModel();
	void setJTree(JTree tree);
}
