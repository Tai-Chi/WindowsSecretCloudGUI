package com.ggl.filebrowser.model;

import java.io.File;

public class FileNode {
     
    private boolean generateGrandchildren;
     
    private File file;
 
    public FileNode(File file) {
        this.file = file;
        this.generateGrandchildren = true;
    }
     
    public File getFile() {
        return file;
    }
 
    public boolean isGenerateGrandchildren() {
        return generateGrandchildren;
    }
 
    public void setGenerateGrandchildren(boolean generateGrandchildren) {
        this.generateGrandchildren = generateGrandchildren;
    }
 
    @Override
    public String toString() {
        String name = file.getName();
        if (name.equals("")) {
            return file.getAbsolutePath();
        } else {
            return name;
        }
    }
}