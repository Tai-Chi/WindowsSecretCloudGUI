package com.ggl.filebrowser;

import main.FileWatcherOnWindows;

import com.ggl.filebrowser.model.NativeFileBrowserModel;
import com.ggl.filebrowser.model.SyncListBrowserModel;
import com.ggl.filebrowser.view.FileBrowserFrame;
 
public class FileBrowser implements Runnable {
	
	FileWatcherOnWindows fw;
	
	public FileBrowser(FileWatcherOnWindows fw) {
		this.fw = fw;
	}
 
    @Override
    public void run() {
        new FileBrowserFrame(fw, new NativeFileBrowserModel(), new SyncListBrowserModel());
    }
}