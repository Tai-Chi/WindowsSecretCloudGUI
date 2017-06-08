package com.ggl.filebrowser.view.renderer;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
 
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
 
import com.ggl.filebrowser.model.NativeFileBrowserModel;
import com.ggl.filebrowser.model.FileBrowserModel2;
import com.ggl.filebrowser.model.FileNode;
 
public class FileTreeCellRenderer2 implements TreeCellRenderer {
     
    private NativeFileBrowserModel model;
     
    private JLabel label;
 
    public FileTreeCellRenderer2(NativeFileBrowserModel model) {
        this.model = model;
        this.label = new JLabel(" ");
        label.setOpaque(true);
    }
 
    @Override
    public Component getTreeCellRendererComponent(JTree tree, 
            Object value, boolean selected, boolean expanded, 
            boolean leaf, int row, boolean hasFocus) {
         
        DefaultMutableTreeNode node = 
                (DefaultMutableTreeNode) value;
        FileNode fileNode = (FileNode) node.getUserObject();
        if (fileNode != null) {
            File file = fileNode.getFile();
            label.setIcon(model.getFileIcon(file));
            label.setText(model.getFileText(file));
        } else {
            label.setText(value.toString());
        }
         
        if (selected) {
            label.setBackground(Color.BLUE);
            label.setForeground(Color.WHITE);
        } else {
            label.setBackground(Color.WHITE);
            label.setForeground(Color.BLACK);
        }
         
        return label;
    }
 
}