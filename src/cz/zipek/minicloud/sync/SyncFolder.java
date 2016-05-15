/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zipek.minicloud.sync;

import com.sun.scenario.Settings;
import cz.zipek.minicloud.api.Event;
import cz.zipek.minicloud.api.Eventor;
import cz.zipek.minicloud.api.External;
import cz.zipek.minicloud.api.Listener;
import cz.zipek.minicloud.api.Path;
import cz.zipek.minicloud.api.Tools;
import cz.zipek.minicloud.api.User;
import cz.zipek.minicloud.api.download.DownloadEvent;
import cz.zipek.minicloud.api.download.Downloader;
import cz.zipek.minicloud.api.download.events.DownloadAllDoneEvent;
import cz.zipek.minicloud.api.events.PathEvent;
import cz.zipek.minicloud.api.upload.UploadEvent;
import cz.zipek.minicloud.api.upload.Uploader;
import cz.zipek.minicloud.api.upload.events.UploadAllDoneEvent;
import cz.zipek.minicloud.sync.events.SyncChecksumFailedEvent;
import cz.zipek.minicloud.sync.events.SyncDone;
import cz.zipek.minicloud.sync.events.SyncDownloadEvent;
import cz.zipek.minicloud.sync.events.SyncEncryptionFailedEvent;
import cz.zipek.minicloud.sync.events.SyncExternalEvent;
import cz.zipek.minicloud.sync.events.SyncMkdirFailedEvent;
import cz.zipek.minicloud.sync.events.SyncUploadEvent;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.crypto.NoSuchPaddingException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Kamen
 */
public class SyncFolder extends Eventor<SyncEvent> implements Listener {
	private File local;
	private String remote;
	private Date lastSync;
	private boolean syncing = false;
	private String actionId;
	private External external;
	private Pattern regexp;
	private long maxSize;
	private User user;
	
	private Downloader downloader;
	private Uploader uploader;
	
	private int timeOffset;
	private String encryption;

	public SyncFolder(JSONObject folder) throws JSONException {
		local = new File(folder.getString("local"));
		remote = folder.getString("remote");
		lastSync = folder.getLong("last") != 0 ? new Date(folder.getLong("last")) : null;
		maxSize = folder.has("max-size") ? folder.getLong("max-size") : 0;
		regexp = null;
		
		try {
			regexp = folder.has("regexp") && !folder.getString("regexp").isEmpty() ? Pattern.compile(folder.getString("regexp")) : null;
		} catch (PatternSyntaxException ex) {
			Logger.getLogger(SyncFolder.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public SyncFolder(File local, String remote, long maxSize, Pattern regexp, Date lastSync) {
		this.local = local;
		this.remote = remote;
		this.maxSize = maxSize;
		this.regexp = regexp;
		this.lastSync = lastSync;
	}

	/**
	 * @return the local
	 */
	public File getLocal() {
		return local;
	}

	/**
	 * @param local the local to set
	 */
	public void setLocal(File local) {
		this.local = local;
	}

	/**
	 * @return the remote
	 */
	public String getRemote() {
		return remote;
	}

	/**
	 * @param remote the remote to set
	 */
	public void setRemote(String remote) {
		this.remote = remote;
	}

	/**
	 * @return the lastSync
	 */
	public Date getLastSync() {
		return lastSync;
	}
	
	/**
	 * Set API used for synchronization.
	 * @param external
	 */
	public void setExternal(External external) {
		
		if (this.external != null)
			this.external.removeListener(this);
		
		this.external = external;
		this.external.addListener(this);
		
	}
	
	public void setTimeOffset(int offset) {
		timeOffset = offset;
	}
	
	/**
	 * Sets user used for synchronization.
	 * @param user 
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * Sets ecnryption options used for synchronization.
	 * @param encryption
	 */
	public void setEncryption(String encryption) {
		this.encryption = encryption;
	}
	
	/**
	 * @param lastSync the lastSync to set
	 */
	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}
	
	/**
	 * Starts synchronization on background.
	 * @return thread doing the synchronization
	 */
	public Thread syncAsync() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				sync();
			}
		};
		thread.start();
		return thread;
	}
	
	/**
	 * Starts synchronization.
	 */
	public void sync() {
		if (syncing)
			return;
		
		syncing = true;
		
		if (!local.exists()) {
			if (!local.mkdirs()) {
				fireEvent(new SyncMkdirFailedEvent(local.getAbsolutePath()));
				return;
			}
		}
		
		if (external == null) {
			external = new External();
			external.addListener(this);
		}
		
		// Request file list
		actionId = external.getPath(remote, true);
	}

	/**
	 * Stops synchronization. All pending downloads and uploads
	 * will be cancelled.
	 */
	public void stop() {
		if (downloader != null) {
			downloader.stop();
			downloader = null;
		}
		if (uploader != null) {
			uploader.stop();
			uploader = null;
		}
	}
	
	@Override
	public void handleEvent(Object event, Object sender) {
		if (event instanceof Event) {
			handleExternal((Event)event);
		} else if (event instanceof DownloadEvent) {
			handleDownloader((DownloadEvent)event);
		} else if (event instanceof UploadEvent) {
			handleUploader((UploadEvent)event);
		}
	}
	
	private synchronized void handleUploader(UploadEvent event) {
		fireEvent(new SyncUploadEvent(event));
		if (event instanceof UploadAllDoneEvent) {
			uploader = null;
			checkIfComplete();
		}
	}
	
	private synchronized void handleDownloader(DownloadEvent event) {
		fireEvent(new SyncDownloadEvent(event));
		if (event instanceof DownloadAllDoneEvent) {
			downloader = null;
			checkIfComplete();
		}
	}
	
	private synchronized void checkIfComplete() {
		if ((uploader == null || uploader.getItems().isEmpty()) &&
			(downloader == null || downloader.getItems().isEmpty())) {
			setLastSync(new Date());
			fireEvent(new SyncDone());
			syncing = false;
		}
	}
	
	private File remoteFileToLocal(String root, cz.zipek.minicloud.api.File remote, Path remote_root) {
		if (remote_root != null)
			return new File(
				root + File.separator + remote.getRelativePath(remote_root).replace("/", File.separator)
			);
		else
			return new File(
				root + File.separator + remote.getRelativePath(this.remote).replace("/", File.separator)
			);
	}
	
	private void handleExternal(Event event) {
		fireEvent(new SyncExternalEvent(event));
		
		if (event.getActionId() != null && event.getActionId().equals(actionId)) {
			if (event instanceof PathEvent) {
				try {
					PathEvent pathEvent = (PathEvent)event;
					Path folder = pathEvent.getPath();
					
					downloader = new Downloader(external, user);
					uploader = new Uploader(external, user.getEncryptor(encryption));
					
					downloader.addListener(this);
					uploader.addListener(this);
					
					//All files in remote folder
					List<cz.zipek.minicloud.api.File> files = new ArrayList<>();
					
					if (folder != null) {
						files = folder.getAllFiles();
						
						//Download new files, sync changed
						for(cz.zipek.minicloud.api.File file : files) {
							boolean invalid = false;
							
							//Check if there isn't copy
							if (file.getParent() != null) {
								for(cz.zipek.minicloud.api.File brother : file.getParent().getFiles()) {
									if (brother.getName().equals(file.getName()) &&
											brother.getMdtime().before(file.getMdtime())) {
										invalid = true;
										break;
									}
								}
							}
							
							if (!invalid) {
								if (!syncFile(remoteFileToLocal(local.getAbsolutePath(), file, folder), file)) {
									return;
								}
							}
						}
					}
					
					//Find new local files
					List<File> loc = getAllFiles(local);
					for(File file : loc) {
						boolean exists = false;
						
						if (maxSize != 0 && file.length() > maxSize) {
							continue;
						}
						
						for(cz.zipek.minicloud.api.File external_file : files) {
							File ondisk = remoteFileToLocal(local.getAbsolutePath(), external_file, folder);
							if (ondisk.getAbsolutePath().equals(file.getAbsolutePath())) {
								exists = true;
								break;
							}
						}
						
						
						if (!exists) {
							String relative = file.getParentFile().getAbsolutePath().substring(local.getAbsolutePath().length()).replace(File.separator, "/");
							
							if (regexp == null || !regexp.matcher(relative).matches()) {
								String path = remote + "/" + relative;
								
								//Because trim with char param is too OP for java
								while (path.length() > 0 && path.charAt(path.length() - 1) == '/')
									path = path.substring(0, path.length() - 1);
								while (path.length() > 0 && path.charAt(0) == '/')
									path = path.substring(1);

								uploader.add(file, path, false);
							}
						}
					}
					
					if (downloader.getItems().isEmpty() && uploader.getItems().isEmpty()) {
						checkIfComplete();
					} else {
						downloader.start(local.getAbsolutePath());
						uploader.start(remote);
					}
				} catch (NoSuchProviderException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
					fireEvent(new SyncEncryptionFailedEvent());
					Logger.getLogger(SyncFolder.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}
	
	private List<File> getAllFiles(File folder) {
		List<File> result = new ArrayList<>();
		for(File file : folder.listFiles()) {
			if (file.isDirectory()) {
				result.addAll(getAllFiles(file));
			} else {
				result.add(file);
			}
		}
		return result;
	}
	
	private boolean syncFile(File local, cz.zipek.minicloud.api.File remote) {
		if (maxSize != 0 && remote.getSize() > maxSize) {
			return true;
		}
		
		if (regexp != null && regexp.matcher(remote.getRelativePath(this.remote)).matches()) {
			return true;
		}
		
		//Check if file exists
		if (!local.exists()) {
			if (!local.getParentFile().exists() && !local.getParentFile().mkdirs()) {
				fireEvent(new SyncMkdirFailedEvent(local.getParentFile().getAbsolutePath()));
				return false;
			}
			
			downloader.add(remote.getVersion(), local.getAbsolutePath());
		} else {
			//Check if file has changed
			String md5;
			try {
				md5 = Tools.md5Checksum(local);
			} catch (NoSuchAlgorithmException | IOException ex) {
				Logger.getLogger(SyncFolder.class.getName()).log(Level.SEVERE, null, ex);
				
				fireEvent(new SyncChecksumFailedEvent(local.getAbsolutePath()));
				return false;
			}

			if (!md5.equals(remote.getChecksum())) {
				if (local.lastModified() > remote.getMdtime().getTime() + timeOffset) {
					uploader.add(local, remote);
				} else {
					downloader.add(remote.getVersion(), local.getAbsolutePath());
				}
			}
		}
		
		return true;
	}

	/**
	 * @return the syncing
	 */
	public boolean isSyncing() {
		return syncing;
	}

	/**
	 * @return the regexp
	 */
	public Pattern getRegexp() {
		return regexp;
	}

	/**
	 * @param regexp the regexp to set
	 */
	public void setRegexp(Pattern regexp) {
		this.regexp = regexp;
	}

	/**
	 * @return the maxSize
	 */
	public long getMaxSize() {
		return maxSize;
	}

	/**
	 * @param maxSize the maxSize to set
	 */
	public void setMaxSize(long maxSize) {
		this.maxSize = maxSize;
	}
}
