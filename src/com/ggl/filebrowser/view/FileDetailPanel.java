package com.ggl.filebrowser.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
 
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
 
import com.ggl.filebrowser.model.NativeFileBrowserModel;
import com.ggl.filebrowser.model.FileNode;
 
public class FileDetailPanel {
     
    private static final Insets finalInsets = 
            new Insets(6, 6, 6, 6);
 
    private FileNode fileNode;
     
    private JLabel fileNameLabel;
    private JLabel fileNameTextLabel;
    private JLabel filePathTextLabel;
    private JLabel lastModifiedLabel;
    private JLabel lastModifiedTextLabel;
    private JLabel fileSizeLabel;
    private JLabel fileSizeTextLabel;
     
    private JTextField filePathField;
     
    private JPanel panel;
     
    public FileDetailPanel() {
        createPartControl();
    }
     
    private void createPartControl() {
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
         
        int gridy = 0;
         
        fileNameTextLabel = new JLabel(" ");
        addComponent(panel, fileNameTextLabel, 0, gridy, 1, 1,
                finalInsets, GridBagConstraints.LINE_START,
                GridBagConstraints.NONE);
         
        fileNameLabel = new JLabel(" ");
        addComponent(panel, fileNameLabel, 1, gridy++, 1, 1,
                finalInsets, GridBagConstraints.LINE_START,
                GridBagConstraints.NONE);
         
        filePathTextLabel = new JLabel("Path:");
        addComponent(panel, filePathTextLabel, 0, gridy, 1, 1,
                finalInsets, GridBagConstraints.LINE_START,
                GridBagConstraints.NONE);
         
        filePathField = new JTextField(70);
        filePathField.setEditable(false);
        addComponent(panel, filePathField, 1, gridy++, 1, 1,
                finalInsets, GridBagConstraints.LINE_START,
                GridBagConstraints.NONE);
         
        lastModifiedTextLabel = new JLabel("Last Modified:");
        addComponent(panel, lastModifiedTextLabel, 0, gridy, 1, 1,
                finalInsets, GridBagConstraints.LINE_START,
                GridBagConstraints.NONE);
         
        lastModifiedLabel = new JLabel(" ");
        addComponent(panel, lastModifiedLabel, 1, gridy++, 1, 1,
                finalInsets, GridBagConstraints.LINE_START,
                GridBagConstraints.NONE);
     
        fileSizeTextLabel = new JLabel("File Size:");
        addComponent(panel, fileSizeTextLabel, 0, gridy, 1, 1,
                finalInsets, GridBagConstraints.LINE_START,
                GridBagConstraints.NONE);
         
        fileSizeLabel = new JLabel(" ");
        addComponent(panel, fileSizeLabel, 1, gridy++, 1, 1,
                finalInsets, GridBagConstraints.LINE_START,
                GridBagConstraints.NONE);
         
    }
     
    private void addComponent(Container container, Component component,
            int gridx, int gridy, int gridwidth, int gridheight, 
            Insets insets, int anchor, int fill) {
        GridBagConstraints gbc = new GridBagConstraints(gridx, gridy,
                gridwidth, gridheight, 1.0D, 1.0D, anchor, fill, 
                insets, 0, 0);
        container.add(component, gbc);
    }
     
    private void updatePartControl(NativeFileBrowserModel model) {
        File file = fileNode.getFile();
         
        if (file.isDirectory()) {
            fileNameTextLabel.setText("Directory:");
        } else {
            fileNameTextLabel.setText("File:");
        }
         
        fileNameLabel.setText(file.getName());
        fileNameLabel.setIcon(model.getFileIcon(file));
        filePathField.setText(file.getAbsolutePath());
        filePathField.setCaretPosition(0);
        lastModifiedLabel.setText(generateLastModified(file));
        fileSizeLabel.setText(generateFileSize(file));
    }
     
    private String generateLastModified(File file) {
        long timestamp = file.lastModified();
        Date date = new Date(timestamp);
        String pattern = "EEE, d MMM yyyy zzzz  h:mm:ss aa";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }
     
    private String generateFileSize(File file) {
        String[] label = {"bytes", "KB", "GB", "TB"};
        long bytes = file.length();
        double dbytes = bytes;
         
        int count = 0;
        while (dbytes > 1000D) {
            dbytes /= 1024D;
            count++;
        }
         
        return String.format("%.3f ", dbytes) + label[count];
    }
 
    public void setFileNode(FileNode fileNode, NativeFileBrowserModel model) {
        this.fileNode = fileNode;
        updatePartControl(model);
    }
 
    public FileNode getFileNode() {
        return fileNode;
    }
 
    public JPanel getPanel() {
        return panel;
    }
     
}