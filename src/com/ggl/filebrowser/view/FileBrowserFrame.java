package com.ggl.filebrowser.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.TooManyListenersException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.DefaultMutableTreeNode;
 













import org.json.JSONException;
import org.json.JSONObject;

import main.FileWatcherOnWindows;

import com.ggl.filebrowser.model.NativeFileBrowserModel;
import com.ggl.filebrowser.model.FileBrowserModel2;
import com.ggl.filebrowser.model.FileNode;
import com.ggl.filebrowser.model.SyncListBrowserModel;
 
public class FileBrowserFrame extends JFrame {
	
	private static final long serialVersionUID = -1747665509439638751L;
	
	/* File Browser Model */
    private NativeFileBrowserModel model;
    private SyncListBrowserModel model2;
    
    /* Graphics User Interface */
    private JPanel mainPanel;
    private JPanel accountPanel;
    private TreeScrollPane treeScrollPane;
    private TreeScrollPane treeScrollPane2;
    private CenterButtonPanel centerButtonPanel;
    private JTextArea textArea;
    
    /* Backend watcher instance */
    private FileWatcherOnWindows fw;
    
    /* Account Info */
    private JTextArea username;
    private JTextArea password;
    private JButton   loginButton;
    private JButton   logoutButton;
    
    public FileBrowserFrame(FileWatcherOnWindows fw, NativeFileBrowserModel model, SyncListBrowserModel model2) {
    	// set file browser model instance
    	this.fw = fw;
        this.model = model;
        this.model2 = model2;
        // make file name font more closer to system-dependent font.
        this.setLookAndFeel();
        // set main frame
        this.setMainFrame();
    }
     
    private void setMainFrame() {
        this.setTitle("Secret Cloud");
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                exitProcedure();
            }
        });
        
        this.createMainPanel();
        
        this.add(mainPanel);
        this.pack();
        this.setLocationByPlatform(true);
        this.setVisible(true);
        this.setResizable(false);
    }
 
    private void createMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        centerButtonPanel = new CenterButtonPanel(fw, model2);
//        centerButtonPanel.setParentFrame(this);
        mainPanel.add(centerButtonPanel, BorderLayout.CENTER);
         
        treeScrollPane = new TreeScrollPane(model, centerButtonPanel);
        mainPanel.add(treeScrollPane.getScrollPane(), BorderLayout.WEST);
        treeScrollPane2 = new TreeScrollPane(model2, centerButtonPanel);
        mainPanel.add(treeScrollPane2.getScrollPane(), BorderLayout.EAST);
        
        textArea = new TextAreaFIFO(100); //JTextArea(10, 120);
        mainPanel.add(new JScrollPane(textArea), BorderLayout.SOUTH);
        PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
        System.setOut(printStream);
        System.setErr(printStream);
        
        username = new JTextArea(1,50);
        password = new JTextArea(1,50);
        accountPanel = new JPanel();
        accountPanel.add(username);
        accountPanel.add(password);
        loginButton = new JButton("Login");
        logoutButton = new JButton("Logout");
        accountPanel.add(loginButton);
        accountPanel.add(logoutButton);
        loginButton.addActionListener(new LoginListener());
        logoutButton.addActionListener(new LogoutListener());
        mainPanel.add(accountPanel, BorderLayout.NORTH);
    }
     
    public void exitProcedure() {
        this.dispose();
        System.exit(0);
    }
     
    public void updateFileDetail(FileNode fileNode) {
//        fileDetailPanel.setFileNode(fileNode, model);
    }
    
    public void updateUI(){
//    	model2 = new SyncListBrowserModel();
        mainPanel.updateUI();
    }
    
    private void setLookAndFeel() {
    	try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
    }
    
    private class CustomOutputStream extends OutputStream {
        private JTextArea textArea;

        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) throws IOException {
            // redirects data to the text area
            textArea.append(String.valueOf((char)b));
            // scrolls the text area to the end of data
            textArea.setCaretPosition(textArea.getDocument().getLength());
            // keeps the textArea up to date
            textArea.update(textArea.getGraphics());
        }
    }
    
    /*********
	 * Login *
	 *********/
    public class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
        	try {
	        	/********************** Variables *********************/
	        	JSONObject json;
	        	URL url;
	        	HttpURLConnection httpcon;
	        	BufferedWriter bw;
	        	BufferedReader reader;
	        	String line;
	        	/******************************************************/
	        	json = new JSONObject();
	    		json.put("username", username.getText());
	    		json.put("passwd", password.getText());
	            url = new URL("https://secret-cloud.herokuapp.com/accounts/authenticate");
	            httpcon = (HttpURLConnection)url.openConnection();
	            httpcon.setDoOutput(true);
	            httpcon.setRequestMethod("POST");
	            bw = new BufferedWriter(new OutputStreamWriter(httpcon.getOutputStream(), "UTF-8"));
	            bw.write(json.toString());
	            bw.flush();
	            bw.close();
	            // --------------------------------------------------------------
	            System.out.println(httpcon.getResponseCode() + " " + httpcon.getResponseMessage());
	            if( httpcon.getResponseCode()==200 ) {
	            	reader = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));
	            	// here the response contains only one line
	            	while((line = reader.readLine()) != null) {
	            		// Get auth_token
	            		json = new JSONObject(line);
	            		fw.setAuthToken(json.getString("auth_token"));
	            		centerButtonPanel.setAuthToken(json.getString("auth_token"));
	            		System.out.println("login: " + json.getString("auth_token"));
	            	}
	            } else {
	            	if (httpcon.getErrorStream()!=null) {
	    	        	reader = new BufferedReader(new InputStreamReader(httpcon.getErrorStream()));
	    	        	while((line = reader.readLine()) != null)
	    	        	    System.out.println(line);
	            	}
	            	return; // jump out of this function if the request is rejected.
	            }
        	} catch (JSONException | IOException e) {
        		e.printStackTrace();
        	}
        }
    }
    
    /*********
	 * Logout *
	 *********/
    public class LogoutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
        	fw.setAuthToken("");
        	centerButtonPanel.setAuthToken("");
        	System.out.println("logout");
        }
    }
 
}