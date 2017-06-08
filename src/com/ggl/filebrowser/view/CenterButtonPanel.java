package com.ggl.filebrowser.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel; 
import javax.swing.tree.TreePath;

import org.json.JSONException;
import org.json.JSONObject;

import main.FileWatcherOnWindows;
import main.GoogleDriveAPI;

import com.ggl.filebrowser.model.FileNode;
import com.ggl.filebrowser.model.SyncListBrowserModel;
import com.ggl.security.AESUtils;
 
public class CenterButtonPanel extends JPanel {

	private static final long serialVersionUID = 5102393272161150267L;
    private SyncListBrowserModel syncModel;
    private TreePath syncPath;
    private TreePath unsyncPath;
    private FileWatcherOnWindows fw;
    private String authToken;
     
    public CenterButtonPanel(FileWatcherOnWindows fw, SyncListBrowserModel syncModel) {
        setMainPanel();
        this.fw = fw;
        this.syncModel = syncModel;
        this.authToken = "";
    }
     
    private void setMainPanel() {
        JButton addSyncButton = new JButton(">>>>>>> Add to sync list >>>>>>>");
        JButton removeSyncButton = new JButton("   Remove from sync list <<<<<<<");
        JButton downloadButton = new JButton("Start to download the files from Drive");
        JButton pauseButton = new JButton("Pause Sync");
        JButton resumeButton = new JButton("Resume Sync");
        addSyncButton.addActionListener(new AddSyncListener());
        removeSyncButton.addActionListener(new RemoveSyncListener());
        downloadButton.addActionListener(new DownloadListener());
        pauseButton.addActionListener(new PauseSyncListener());
        resumeButton.addActionListener(new ResumeSyncListener());
        this.setLayout(new BorderLayout());
        this.add(addSyncButton, BorderLayout.NORTH);
        this.add(removeSyncButton, BorderLayout.SOUTH);
        this.add(downloadButton, BorderLayout.CENTER);
//        this.add(pauseButton, BorderLayout.WEST);
//        this.add(resumeButton, BorderLayout.EAST);
    }
     
   
    static String key;
    
    static {
        try {
//		    key = AESUtils.getSecretKey();
		    key = "eVwv3SnHf5NfXP8nmM43jA==";
        	System.out.println(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
//    public void setFileNode(FileNode fileNode) {
//        this.fileNode = fileNode;
//    }
    
    public void setSyncPath(TreePath syncPath) {
        this.syncPath = syncPath;
    }
    
    public void unsetSyncPath(TreePath unsyncPath) {
        this.unsyncPath = unsyncPath;
    }
    
    public void setAuthToken(String token) {
    	this.authToken = token;
    }
    
//    public void setDestination(String path){
//    	this.destination = path;
//    }
    
//    public void setParentFrame(FileBrowserFrame frame){
//    	this.frame = frame;
//    }
    
    private String convertTreePathToString(TreePath _path) {
    	String path = "";
    	for(int i=1; i<_path.getPathCount(); i++) {
	    	if(i<=1)
				path = _path.getPathComponent(i).toString();
			else if(i<_path.getPathCount()-1)
				path += _path.getPathComponent(i).toString() + "\\";
			else
				path += _path.getPathComponent(i).toString();
    	}
    	return path;
    }
    
    /************************
	 * Pause sync procedure *
	 ************************/
    public class PauseSyncListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
        	fw.pauseWatching();
        }
    }
    
    /************************
	 * Resume sync procedure *
	 ************************/
    public class ResumeSyncListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
        	fw.resumeWatching();
        }
    }
 
    /***************************
	 * Add folder to sync list *
	 ***************************/
    public class AddSyncListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
        	if(syncPath!=null) {
	        	// for GUI
	        	syncModel.addSyncFolder(syncPath);
	        	// for real watching routine
	        	fw.addWatchingList(convertTreePathToString(syncPath));
        	}
        }
    }
 
    /********************************
	 * Remove folder from sync list *
	 ********************************/
    public class RemoveSyncListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
        	if(unsyncPath!=null) {
	        	// for GUI
	        	syncModel.removeSyncFolder(unsyncPath);
	        	// for real watching routine
	        	fw.removeWatchingList(convertTreePathToString(unsyncPath));
        	}
        }
    }

    /*********************************************
     * Download selected files from Google Drive *
     *********************************************/
    public class DownloadListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
        	/**********************************************************
        	 * Setup UI for users to select the download destination. *
        	 **********************************************************/
        	JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            chooser.setDialogTitle("選取下載位置");
            chooser.showOpenDialog(CenterButtonPanel.this);
            if( chooser.getSelectedFile()!=null ) {
	            System.out.println("save to: " + chooser.getSelectedFile().toString());
	            /**********************************************************/
	        	try {
	        		/******************************************
	            	 * Connect to server to execute download. *
	            	 ******************************************/
	        		int failTimes = 0;
		        	URL url;
		        	HttpURLConnection httpcon;
		        	BufferedWriter bw;
		        	BufferedReader reader;
		        	String line;
		        	String gaccount = null;
		        	String file_gid = null;
		        	String dst_path = null;
		        	/******************************************/
		        	while(failTimes<10) {
//			        	json = new JSONObject();
//			    		json.put("username", "alan");
			            url = new URL("https://secret-cloud.herokuapp.com/download/execute");
			            httpcon = (HttpURLConnection)url.openConnection();
			            httpcon.setDoOutput(true);
			            httpcon.setRequestMethod("POST");
			            httpcon.setRequestProperty("AUTHORIZATION", "Bearer " + authToken);
			            bw = new BufferedWriter(new OutputStreamWriter(httpcon.getOutputStream(), "UTF-8"));
//			            bw.write(json.toString());
			            bw.flush();
			            bw.close();
			            // --------------------------------------------------------------
			            System.out.println(httpcon.getResponseCode() + " " + httpcon.getResponseMessage());
			            if( httpcon.getResponseCode()==200 ) {
			            	reader = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));
			            	// here the response contains only one line
			            	while((line = reader.readLine()) != null) {
			            		// Get gaccount, file gid, and destination path
			                	gaccount = line.split(" ")[0];
			                	file_gid = line.split(" ")[1];
			                	dst_path = line.split(" ")[2];
								GoogleDriveAPI.download(gaccount, file_gid, chooser.getSelectedFile().toString() + "\\" + dst_path);
								System.out.println("download: " + chooser.getSelectedFile().toString() + "\\" + dst_path);
			            	}
			            	failTimes = 0;
			            } else {
			            	failTimes++;
			            	if (httpcon.getErrorStream()!=null) {
			    	        	reader = new BufferedReader(new InputStreamReader(httpcon.getErrorStream()));
			    	        	while((line = reader.readLine()) != null)
			    	        	    System.out.println(line);
			            	}
			            }
		        	}
	        	} catch (IOException e){
	        		e.printStackTrace();
	        	}
            }
        }
    }  
}