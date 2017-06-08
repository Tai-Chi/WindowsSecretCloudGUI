package com.ggl.filebrowser.model;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
 
public class NativeFileBrowserModel implements BrowserModel {
     
    private FileSystemView fileSystemView;
     
    public NativeFileBrowserModel() {
        this.fileSystemView = FileSystemView.getFileSystemView();
    }
     
    public DefaultTreeModel createTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
         
        for (File file : this.getRoots()) {
            root.add(new DefaultMutableTreeNode(new FileNode(file)));
        }
         
        addChildNodes(root);
        addGrandchildNodes(root);
         
        return new DefaultTreeModel(root);
    }
 
    public void addGrandchildNodes(DefaultMutableTreeNode root) {
        Enumeration<?> enumeration = root.children();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode node = 
                    (DefaultMutableTreeNode) enumeration.nextElement();
            addChildNodes(node);
        }
    }
 
    private void addChildNodes(DefaultMutableTreeNode root) {
        Enumeration<?> enumeration = root.children();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode node = 
                    (DefaultMutableTreeNode) enumeration.nextElement();
            FileNode fileNode = (FileNode) node.getUserObject();
            File file = fileNode.getFile();
            /***************************************************
             * Due to some permission issues, we want to change
             * file.isDirectory() to file.listFiles()!=null.
             **************************************************/
            if (file.listFiles()!=null) {
                for (File child : file.listFiles()) {
            		if(child.isDirectory()){                		
            			node.add(new DefaultMutableTreeNode(new FileNode(child)));
            		}
                }
            }
        }
    }
 
    public FileSystemView getFileSystemView() {
        return fileSystemView;
    }
     
    public Icon getFileIcon(File file) {
        return fileSystemView.getSystemIcon(file);
    }
     
    public String getFileText(File file) {
        return fileSystemView.getSystemDisplayName(file);
    }
    
    /**
     * Returns all root partitions on this system. On Windows, this
     * will be the A: through Z: drives.
     */
    public File[] getRoots() {
    	if( System.getProperty("os.name").startsWith("Windows") ) {
	        Vector<File> rootsVector = new Vector<File>();
	        // Solve OS bug:
	        // Create the A: drive whether it is mounted or not
//	        File floppy = new File("A" + ":" + "\\");
//	        rootsVector.addElement(floppy);
	        // Run through all possible mount points and check
	        // for their existence.
	        for (char c = 'C'; c <= 'Z'; c++) {
	            File deviceFile = new File(c + ":\\");
	            if (deviceFile != null && deviceFile.exists())
	                rootsVector.addElement(deviceFile);
	        }
	        File[] roots = new File[rootsVector.size()];
	        rootsVector.copyInto(roots);
	        return roots;
    	}
    	else
    		return fileSystemView.getRoots();
    }

	@Override
	public boolean isGoingToSync() {
		return true;
	}

	@Override
	public void setJTree(JTree tree) {
	}
}