package main;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import name.pachler.nio.file.*;
import name.pachler.nio.file.ext.ExtendedWatchEventKind;
import static name.pachler.nio.file.StandardWatchEventKind.*;
import static name.pachler.nio.file.ext.ExtendedWatchEventKind.*;
import static name.pachler.nio.file.ext.ExtendedWatchEventModifier.*;

public class FileWatcherOnWindows {
 
    private WatchService watcher;
    private Map<WatchKey, Path> keys;
    private Thread thread;
    private List<String> watchingList;
    private boolean running;
    private boolean pausing;
    private boolean [] watchedDrives;
    private String authToken = "";
 
    /**
     * Creates a WatchService and registers the given directory
     */
    FileWatcherOnWindows () throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();
        this.thread = new Thread( new Runnable() {
        	public void run () {
    			try {
					processLoop();
				} catch (UnsupportedOperationException | JSONException
						| IOException e) {
					e.printStackTrace();
				}
        	}
        } );
        this.running = true;
        this.pausing = false;
        this.watchingList = new ArrayList<String>();
        this.watchedDrives = new boolean [2]; // assume only 2 drives
//        registerDrive(dir);
    }
    
    public void setAuthToken(String token) {
    	authToken = token;
    }
 
    /**
     * Although this function can register all the given directories,
     * we only use this function to register drives for some file
     * resource lock reasons.
     */
    private void registerDrive(Path dir) 
    {	
		try {
			WatchEvent.Kind<?> eventList[] = { ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, ENTRY_RENAME_FROM, ENTRY_RENAME_TO };
	        WatchKey key = dir.register(watcher, eventList, FILE_TREE);
			keys.put(key, dir);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    /************************************************************
     *  When someone creates a file, we:
	 *  (1) tell our server the file's path and size, then
	 *      the server returns file primary key and account.
	 *  (2) This software uploads the file to this account,
	 *      then the API returns file gid and remaining space.
	 *  (3) eventually updates the file gid to our server.
	 *  (4) eventually updates the remaining space to our server.
	 ************************************************************/
    private void performCreateInstance(String fileName) throws JSONException, IOException {
    	if( new File(fileName).isDirectory() )
    		performCreateDirectory(fileName);
    	else
    		performCreateFile(fileName);
    }
    private void performCreateDirectory(final String _directory) throws IOException, JSONException {
    	File [] directory = new File(_directory).listFiles();
    	if(directory!=null) {
	    	for( File file : directory ) {
	    		if( file.isDirectory() )
	    			performCreateDirectory(file.toString());
	    		else
	    			performCreateFile(file.toString());
	    	}
    	}
    }
    private void performCreateFile(String fileName) throws JSONException, IOException {
    	/********************** Variables *********************/
    	JSONObject json;
    	URL url;
    	HttpURLConnection httpcon;
    	BufferedWriter bw;
    	BufferedReader reader;
    	String line;
    	String file_id = null;
    	String gaccount = null;
    	/******************************************************/
    	
    	/********************** Part (1) **********************/
    	json = new JSONObject();
//		json.put("username", "alan");
		json.put("path", fileName);
        json.put("portion", 1);
        json.put("size", new File(fileName).length());
        json.put("time", new SimpleDateFormat("yyyy MM dd HH:mm:ss").format(new Date(new File(fileName).lastModified())));
        url = new URL("https://secret-cloud.herokuapp.com/create/file");
        httpcon = (HttpURLConnection)url.openConnection();
        httpcon.setDoOutput(true);
        httpcon.setRequestMethod("POST");
        httpcon.setRequestProperty("AUTHORIZATION", "Bearer " + authToken);
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
        		// Get file record id and allocated gaccount
            	file_id = line.split(" ")[0];
            	gaccount = line.split(" ")[1];
        	}
        } else {
        	if (httpcon.getErrorStream()!=null) {
	        	reader = new BufferedReader(new InputStreamReader(httpcon.getErrorStream()));
	        	while((line = reader.readLine()) != null) {
	        	    System.out.println(line);
	        	    if(line.equals("OUTDATED")) {
	        	    	performDeleteInstance(fileName);
	        	    	performCreateFile(fileName);
	        	    }
	        	}
        	}
        	return; // jump out of this function if the request is rejected.
        }
        /******************************************************/
        
        /********************** Part (2) **********************/
        // Call Java API to upload files to Drive
    	String gfile_id = GoogleDriveAPI.upload(gaccount, fileName);
    	long space = GoogleDriveAPI.getSpace(gaccount);
    	/******************************************************/
    	
    	/********************** Part (3) **********************/
    	// Update file gid metadata to our server
    	json = new JSONObject();
//		json.put("username", "alan");
		json.put("file_id", file_id);
		json.put("gfile_id", gfile_id);
		System.out.println(file_id + ", " + gfile_id);
        url = new URL("https://secret-cloud.herokuapp.com/update/gfid");
        httpcon = (HttpURLConnection)url.openConnection();
        httpcon.setDoOutput(true);
        httpcon.setRequestMethod("POST");
        httpcon.setRequestProperty("AUTHORIZATION", "Bearer " + authToken);
        bw = new BufferedWriter(new OutputStreamWriter(httpcon.getOutputStream(), "UTF-8"));
        bw.write(json.toString());
        bw.flush();
        bw.close();
        httpcon.connect();
        // --------------------------------------------------------------
        System.out.println(httpcon.getResponseCode() + " " + httpcon.getResponseMessage());
        if( httpcon.getResponseCode()==200 && httpcon.getInputStream() != null )
    		reader = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));
    	else if( httpcon.getErrorStream() != null )
    		reader = new BufferedReader(new InputStreamReader(httpcon.getErrorStream()));
    	else
    		reader = null;
        if( reader != null ) {
        	while((line = reader.readLine()) != null)
        		System.out.println(line);
        }
    	if( httpcon.getResponseCode() != 200 )
    		return; // jump out of this function if the request is rejected.
    	/******************************************************/
    	
    	/********************** Part (4) **********************/
    	// Update available drive space metadata to our server
    	json = new JSONObject();
//		json.put("username", "alan");
		json.put("gaccount", gaccount);
		json.put("space", space);
        url = new URL("https://secret-cloud.herokuapp.com/update/space");
        httpcon = (HttpURLConnection)url.openConnection();
        httpcon.setDoOutput(true);
        httpcon.setRequestMethod("POST");
        httpcon.setRequestProperty("AUTHORIZATION", "Bearer " + authToken);
        bw = new BufferedWriter(new OutputStreamWriter(httpcon.getOutputStream(), "UTF-8"));
        bw.write(json.toString());
        bw.flush();
        bw.close();
        httpcon.connect();
        // --------------------------------------------------------------
        System.out.println(httpcon.getResponseCode() + " " + httpcon.getResponseMessage());
        if( httpcon.getResponseCode()==200 && httpcon.getInputStream() != null )
    		reader = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));
    	else if( httpcon.getErrorStream() != null )
    		reader = new BufferedReader(new InputStreamReader(httpcon.getErrorStream()));
    	else
    		reader = null;
        if( reader != null ) {
        	while((line = reader.readLine()) != null)
        		System.out.println(line);
        }
    	if( httpcon.getResponseCode() != 200 )
    		return; // jump out of this function if the request is rejected.
    	/******************************************************/
    }    
    
    /***************************************************
     *  When someone deletes a file, we:
	 *  (1) tell our server the file's path, then the
	 *      server returns the gaccount and file gid.
	 *  (2) This software uses the above two info to
	 *      delete files on Drive with API.
	 *  (3) Finally update available Drive space to
	 *      our server.
	 **************************************************/
    private void performDeleteInstance(String fileName) throws JSONException, IOException {
    	System.out.println("delete instance: " + fileName);
    	/********************** Variables *********************/
    	JSONObject json;
    	URL url;
    	HttpURLConnection httpcon;
    	BufferedWriter bw;
    	BufferedReader reader;
    	String line;
    	String file_gid = null;
    	String gaccount = null;
    	boolean mode = true; // switch from folder to file
    	/******************************************************/
    	
    	/********************** Part (1) **********************/
    	/*********************************************************** 
    	 *  We try /delete/folder route first to check whether
    	 *  the deleted instance is a folder or a file. Of course
    	 *  we try folder first because it is larger than a file.
    	 *  If /delete/folder returns 403, then we try /delete/file.
    	 *  If /delete/file still returns 403, then we know that the
    	 *  instance does not exist.
    	 **********************************************************/
    	do {
	    	json = new JSONObject();
//			json.put("username", "alan");
			json.put("path", fileName);
	        url = new URL("https://secret-cloud.herokuapp.com/delete/" + (mode ? "folder" : "file"));
	        httpcon = (HttpURLConnection)url.openConnection();
	        httpcon.setDoOutput(true);
	        httpcon.setRequestMethod("POST");
	        httpcon.setRequestProperty("AUTHORIZATION", "Bearer " + authToken);
	        bw = new BufferedWriter(new OutputStreamWriter(httpcon.getOutputStream(), "UTF-8"));
	        bw.write(json.toString());
	        bw.flush();
	        bw.close();
	        mode = !mode;
	        System.out.println(httpcon.getResponseCode() + " " + httpcon.getResponseMessage());
    	} while (!mode && httpcon.getResponseCode()==403);
        if( httpcon.getResponseCode() == 200 )
        {
        	reader = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));
        	while((line = reader.readLine()) != null) {
        	    // Get gaccount and file google id
            	gaccount = line.split(" ")[0];
            	file_gid = line.split(" ")[1];
            	
            	System.out.println("delete instance: ready to call delete API");
            	/********************** Part (2) **********************/
            	// Call Java API to delete files on Drive for each response
            	GoogleDriveAPI.delete(gaccount, file_gid);
            	/******************************************************/
            	System.out.println("delete instance: finish calling delete API");
        	}
        } else {
        	if( httpcon.getErrorStream() != null ) {
	        	reader = new BufferedReader(new InputStreamReader(httpcon.getErrorStream()));
	        	while((line = reader.readLine()) != null)
	        	    System.out.println(line);
        	}
        	return; // jump out of this function if the request is rejected.
        }
        /******************************************************/
        
        /********************** Part (3) **********************/
    	// Update available drive space metadata to our server
    	json = new JSONObject();
//		json.put("username", "alan");
		json.put("gaccount", gaccount);
		json.put("space", GoogleDriveAPI.getSpace(gaccount));
        url = new URL("https://secret-cloud.herokuapp.com/update/space");
        httpcon = (HttpURLConnection)url.openConnection();
        httpcon.setDoOutput(true);
        httpcon.setRequestMethod("POST");
        httpcon.setRequestProperty("AUTHORIZATION", "Bearer " + authToken);
        bw = new BufferedWriter(new OutputStreamWriter(httpcon.getOutputStream(), "UTF-8"));
        bw.write(json.toString());
        bw.flush();
        bw.close();
        httpcon.connect();
        // --------------------------------------------------------------
        System.out.println(httpcon.getResponseCode() + " " + httpcon.getResponseMessage());
        if( httpcon.getResponseCode()==200 && httpcon.getInputStream() != null )
    		reader = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));
    	else if( httpcon.getErrorStream() != null )
    		reader = new BufferedReader(new InputStreamReader(httpcon.getErrorStream()));
    	else
    		reader = null;
        if( reader != null ) {
        	while((line = reader.readLine()) != null)
        		System.out.println(line);
        }
    	if( httpcon.getResponseCode() != 200 )
    		return; // jump out of this function if the request is rejected.
        /******************************************************/
    }
    
    /**************************************************
     *  When someone renames a file, we tell our
	 *  server the file's old path and its new name.
	 *  It can be done with one request. Very easy.
	 *************************************************/
    private void performRenameInstance(String old_path, String new_path) throws JSONException, IOException {
    	/********************** Variables *********************/
    	JSONObject json;
    	URL url;
    	HttpURLConnection httpcon;
    	BufferedWriter bw;
    	BufferedReader reader;
    	String line;
    	/******************************************************/
    	
    	/************************ Body ************************/
    	json = new JSONObject();
//		json.put("username", "alan");
		json.put("old_path", old_path);
		json.put("new_name", new_path.replaceAll("\\\\","/").substring(new_path.replaceAll("\\\\","/").lastIndexOf('/') + 1));
        url = new URL("https://secret-cloud.herokuapp.com/rename/" + (new File(new_path).isDirectory() ? "folder" : "file"));
        httpcon = (HttpURLConnection)url.openConnection();
        httpcon.setDoOutput(true);
        httpcon.setRequestMethod("POST");
        httpcon.setRequestProperty("AUTHORIZATION", "Bearer " + authToken);
        bw = new BufferedWriter(new OutputStreamWriter(httpcon.getOutputStream(), "UTF-8"));
        bw.write(json.toString());
        bw.flush();
        bw.close();
        // --------------------------------------------------------------
        System.out.println(httpcon.getResponseCode() + " " + httpcon.getResponseMessage());
        if( httpcon.getResponseCode()==200 && httpcon.getInputStream() != null )
    		reader = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));
    	else if( httpcon.getErrorStream() != null )
    		reader = new BufferedReader(new InputStreamReader(httpcon.getErrorStream()));
    	else
    		reader = null;
        if( reader != null ) {
        	while((line = reader.readLine()) != null)
        		System.out.println(line);
        }
    	if( httpcon.getResponseCode() != 200 )
    		return; // jump out of this function if the request is rejected.
        /******************************************************/
    }
    
    /**
     * Process all events for keys queued to the watcher
     * @throws IOException
     * @throws JSONException
     * @throws Exception
     */
    private void processLoop () throws UnsupportedOperationException, JSONException, IOException {
    	/** Tiny data for procedure control **/
    	String beforeRename = null;
    	String lastFileName = null;
    	long lastModifiedTime = 0;
    	/*************************************/
    	while(running){
    		if(!pausing) {
	    	    WatchKey signalledKey;
	    	    try {
	    	    	// take() will block until receiving a signal.
	    	        signalledKey = watcher.take();
	    	    } catch (InterruptedException ix){
	    	        // we'll ignore being interrupted
	    	        continue;
	    	    } catch (ClosedWatchServiceException cwse){
	    	        // other thread closed watch service
	    	        System.out.println("watcher closed, terminating.");
	    	        break;
	    	    }
	    	    Path dir = keys.get(signalledKey);
	
	    	    // get list of events from key
	    	    List<WatchEvent<?>> list = signalledKey.pollEvents();
	
	    	    // VERY IMPORTANT! call reset() AFTER pollEvents() to allow the
	    	    // key to be reported again by the watch service
	    	    signalledKey.reset();
	
	    	    // we'll simply print what has happened; real applications
	    	    // will do something more sensible here
	    	    for(WatchEvent<?> event : list) {
	    	    	@SuppressWarnings("unchecked")
	                Path child = dir.resolve(((WatchEvent<Path>)event).context());
	    	    	String fileName = child.toString();
	    	    	// Watching list is a subset of the whole watched dist.
	    	    	// Go through the whole list to check whether there
	    	    	// exists a watched file/folder related to this event.
	                for(String str: watchingList) {
	                	if ( fileName.startsWith(str) || str.startsWith(fileName) )
	                	{
	                		// print out event
	            			System.out.format("%s: %s\n", event.kind().name(), child);
	            			
	                		if( event.kind() == StandardWatchEventKind.ENTRY_CREATE ) {
	                			performCreateInstance(fileName);
	                		}
	                		else if( event.kind() == StandardWatchEventKind.ENTRY_DELETE ) {
	                			performDeleteInstance(fileName);
	                		}
	                		else if( event.kind() == StandardWatchEventKind.ENTRY_MODIFY ) {
	                			File file = new File(fileName);
	                			// eliminate duplicated events
	                			if( !fileName.equals(lastFileName) ||
	                					(fileName.equals(lastFileName) && file.lastModified()>lastModifiedTime) ) {
		                			// modify folder = modify files in that folder
		                			// hence we only process files for ENTRY_MODIFY
		                			if( !file.isDirectory() ) {
			                			performDeleteInstance(fileName);
			                			performCreateFile(fileName);
		                			}
	                			}
	                			lastFileName = fileName;
	                			lastModifiedTime = file.lastModified();
	                		}
	                		else if( event.kind() == ExtendedWatchEventKind.ENTRY_RENAME_FROM ) {
	                			beforeRename = fileName;
	                		}
	                		else if( event.kind() == ExtendedWatchEventKind.ENTRY_RENAME_TO ) {
	                			performRenameInstance(beforeRename, fileName);
	                		}
	                		else {
	                			throw new UnsupportedOperationException();
	                		}
	                		
	                		// If a watched file/folder matches the coming event, then
	                		// it is not necessary to check other files/folders.
	            			break;
	                	}
	                }
	    	    }
    		}
    	}
    }
    
    public void startWatching() {
    	this.thread.start();
    }
    
    public void stopWatching() {
    	this.running = false;
    }
    
    public void pauseWatching() {
    	this.pausing = true;
    }
    
    public void resumeWatching() {
    	this.pausing = false;
    }
    
    private void recursivelyAddFile(String path) {
    	File folder = new File(path);
    	if( folder.listFiles()!=null ) {
	    	for(File file : folder.listFiles()) {
	    		if(file.isDirectory())
	    			recursivelyAddFile(file.getAbsolutePath());
				else
					try {
						performCreateFile(file.getAbsolutePath());
					} catch (JSONException | IOException e) {
						e.printStackTrace();
					}
	    	}
    	}
    }
    
    public void addWatchingList(String path) {
    	for(String str : watchingList) {
    		/****************************************************
    		 * If we want to add a child folder of an existing  *
    		 * folder, then we have nothing to do.              *
    		 *                                                  *
    		 * If we want to add a parent folder of an existing *
    		 * folder, then we should remove that existing      *
    		 * folder first manually.    						*
    		 ****************************************************/
    		if(path.startsWith(str)||str.startsWith(path))
    			return;
    	}
    	watchingList.add(path);
    	
    	recursivelyAddFile(path);
    	
    	if(!watchedDrives[path.charAt(0)-'C']) {
    		registerDrive(Paths.get(path.charAt(0) + ":\\"));
    		watchedDrives[path.charAt(0)-'C'] = true;
    	}
    	System.out.println("addwatchinglist: " + path);
    }
    
    public void removeWatchingList(String path) {
    	// Because each element is unique, so theoretically
    	// we can only remove the first occurrence.
    	watchingList.remove(path);
    }
 
}