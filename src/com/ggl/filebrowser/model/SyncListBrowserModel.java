package com.ggl.filebrowser.model;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
 
public class SyncListBrowserModel extends DefaultTreeModel implements BrowserModel {

	private static final long serialVersionUID = 3025731011490436923L;
	private FileSystemView fileSystemView;
	private JTree jtree;
	private int [] syncListCount;
     
    public SyncListBrowserModel() {
    	super(new DefaultMutableTreeNode());
        fileSystemView = FileSystemView.getFileSystemView();
        // assume only C: D: two drives
        syncListCount = new int[2];
    }
    
    public void addSyncFolder(TreePath path) {
    	if(path!=null) {
	    	DefaultMutableTreeNode parent = (DefaultMutableTreeNode) this.getRoot();
	    	String accPath = "";
	    	int j = 0; // check which drive
	    	boolean hasAdded = false;
	    	/**********************************************
	    	 *  Because the first element [0] has nothing,
	    	 *  we start the index from i=1.
	    	 *********************************************/
	    	for(int i=1; i<path.getPathCount(); i++) {
	    		Enumeration<?> children = parent.children();
	    		DefaultMutableTreeNode child = null;
	    		if(i<=1)
	    			accPath = path.getPathComponent(i).toString();
	    		else if(i<path.getPathCount()-1)
	    			accPath += path.getPathComponent(i).toString() + "\\";
	    		else
	    			accPath += path.getPathComponent(i).toString();
	            while (children.hasMoreElements()) {
	                DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
	                System.out.println("abspath: "+((FileNode)node.getUserObject()).getFile().getAbsolutePath());
	                System.out.println("accpath: "+accPath);
	                List<String> a = Arrays.asList(((FileNode)node.getUserObject()).getFile().getAbsolutePath().split("\\\\"));
	                a.removeAll(Arrays.asList(""));
	                List<String> b = Arrays.asList(accPath.split("\\\\"));
	                b.removeAll(Arrays.asList(""));
	                if(a.equals(b)) {
	                	child = node;
	                	System.out.println(accPath);
	                	break;
	                }
	                if(i==1)
	                	j++;
	            }
	            if( child == null ) {
	            	/**************************************************
	            	 * When we want to create a new branch, check the *
	            	 * parent first. If the parent has no children,   *
	            	 * it means that we are going to add a sub-folder *
	            	 * of a watched folder. This should not be done.  *
	            	 **************************************************/
	            	if(syncListCount[j]>0 && parent.getChildCount()<=0) {
	            		System.out.println("blocked");
	            		return;
	            	}
	            	/**************************************************/
	            	hasAdded = true;
	            	child = new DefaultMutableTreeNode(new FileNode(new File(accPath)));
	            	parent.add(child);
	            	this.reload(parent);
	            } else {
	            	
	            }
	            parent = child;
	//    		System.out.println(i + "," + path.getPathComponent(i));
	    	}
	    	
	    	/** Reorganize our tree. **/
	    	this.expandJTree(jtree);
	    	if(hasAdded) {
	    		syncListCount[j]++;
	    		System.out.println("syncListCount" + j + ": " + syncListCount[j]);
	    	}
    	}
    }
    
    public void removeSyncFolder(TreePath path) {
    	if(path!=null) {
	    	DefaultMutableTreeNode parent = (DefaultMutableTreeNode) this.getRoot();
	    	DefaultMutableTreeNode child = null;
	    	String accPath = "";
	    	int j = 0; // check which drive
	    	boolean hasRemoved = false;
	    	/**********************************************
	    	 *  Because the first element [0] has nothing,
	    	 *  we start the index from i=1.
	    	 *********************************************/
	    	for(int i=1; i<path.getPathCount(); i++) {
	    		Enumeration<?> children = parent.children();
	    		if(i<=1)
	    			accPath = path.getPathComponent(i).toString();
	    		else if(i<path.getPathCount()-1)
	    			accPath += path.getPathComponent(i).toString() + "\\";
	    		else
	    			accPath += path.getPathComponent(i).toString();
	            while (children.hasMoreElements()) {
	                DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
	                List<String> a = Arrays.asList(((FileNode)node.getUserObject()).getFile().getAbsolutePath().split("\\\\"));
	                a.removeAll(Arrays.asList(""));
	                List<String> b = Arrays.asList(accPath.split("\\\\"));
	                b.removeAll(Arrays.asList(""));
	                if(a.equals(b)) {
	                	child = node;
	                	break;
	                }
	                if(i==1)
	                	j++;
	            }
	            if( child == null ) {
	            	/* No such node */
	            	return;
	            }
	            parent = child;
	    	}
	    	
	    	/***********************************************************
	    	 * Although the tree view shows all layers of the path,    *
	    	 * the watched folder must be extended to the final layer. *
	    	 * If we choose non-final layer, it must not be a watched  *
	    	 * folder. Removing it takes no effect.                    *
	    	 ***********************************************************/
	    	if(child.getChildCount()>0)
	    		return;
	    	
	    	/*********************************************************
	    	 * Now we want to remove the target node. However if its
	    	 * parent node has no children after that, we should also
	    	 * remove that parent node. This process should be done
	    	 * bottom-up until the condition does not hold.
	    	 ********************************************************/
	    	parent = (DefaultMutableTreeNode) child.getParent();
	    	while(parent!=null) {
	    		hasRemoved = true;
		    	parent.remove(child); /** main objective **/
		    	if( parent.getChildCount()==0 ) {
		    		child = parent;
		    		parent = (DefaultMutableTreeNode) child.getParent();
		    	} else
		    		break;
	    	}
	    	
	    	/** Reorganize our tree. **/
	    	this.reload();
	    	this.expandJTree(jtree);
	    	if(hasRemoved) {
	    		syncListCount[j]--;
	    		System.out.println("syncListCount" + j + ": " + syncListCount[j]);
	    	}
    	}
    }
    
    /** Expand the whole sync list. **/
    public void expandJTree(JTree jtree) {
    	int j = jtree.getRowCount();
        int i = 0;
        while(i < j) {
            jtree.expandRow(i);
            i += 1;
            j = jtree.getRowCount();
        }
    }
    
    public void setJTree(JTree jtree) {
    	this.jtree = jtree;
    }
     
    public DefaultTreeModel createTreeModel() {
        return this;
    }
 
    public void addGrandchildNodes(DefaultMutableTreeNode root) {
//        Enumeration<?> enumeration = root.children();
//        while (enumeration.hasMoreElements()) {
//            DefaultMutableTreeNode node = 
//                    (DefaultMutableTreeNode) enumeration.nextElement();
//            addChildNodes(node);
//        }
    }
 
    private void addChildNodes(DefaultMutableTreeNode root) {
//        Enumeration<?> enumeration = root.children();
//        while (enumeration.hasMoreElements()) {
//            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
//            FileNode fileNode = (FileNode) node.getUserObject();
//            File file = fileNode.getFile();
//            if (file.listFiles() != null) {
//                for (File child : file.listFiles()) {
//                	if (child.isDirectory()){                		
//                		node.add(new DefaultMutableTreeNode(new FileNode(child)));
//                	}
//                }
//            }
//        }
    }

	@Override
	public Icon getFileIcon(File file) {
//		try {
//			System.out.println("GetIcon: " + file.getCanonicalPath());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return fileSystemView.getSystemIcon(file);
	}

	@Override
	public String getFileText(File file) {
		return fileSystemView.getSystemDisplayName(file);
	}

	@Override
	public boolean isGoingToSync() {
		return false;
	}
}