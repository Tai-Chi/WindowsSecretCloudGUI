package main;

import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import name.pachler.nio.file.Paths;

import com.ggl.filebrowser.FileBrowser;

public class Main {

	public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		// Watcher Main Routine
        FileWatcherOnWindows fw = new FileWatcherOnWindows(); //Paths.get("D:\\"));
    	fw.startWatching();
//    	fw.addWatchingList("D:\\орн▒\\MERGE_REBASE_SLIDE");
		// GUI
    	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(new FileBrowser(fw));
    }

}
