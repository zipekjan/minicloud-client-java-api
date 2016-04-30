/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zipek.minicloud.api.download;

import cz.zipek.minicloud.api.Eventor;
import cz.zipek.minicloud.api.External;
import cz.zipek.minicloud.api.File;
import cz.zipek.minicloud.api.FileVersion;
import cz.zipek.minicloud.api.Listener;
import cz.zipek.minicloud.api.User;
import cz.zipek.minicloud.api.download.events.DownloadAllDoneEvent;
import cz.zipek.minicloud.api.download.events.DownloadFileDoneEvent;
import cz.zipek.minicloud.api.download.events.DownloadFileStartedEvent;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Kamen
 */
public class Downloader extends Eventor<DownloadEvent> implements Listener {

	private final List<DownloadItem> items;
	private DownloadThread thread;
	private String targetFolder;
	private final External external;
	
	private final User user;
	
	/**
	 * @param aExternal API used for downloading
	 * @param aUser user that performs the downloading
	 */
	public Downloader(External aExternal, User aUser) {
		items = new LinkedList<>();
		external = aExternal;
		user = aUser;
	}
	
	/**
	 * Adds new file to download list.
	 * @param file
	 */
	public void add(FileVersion file) {
		add(new DownloadItem(file, null));
	}

	/**
	 * Adds new file to download list.
	 * @param file
	 * @param target
	 */
	public void add(FileVersion file, String target) {
		add(new DownloadItem(file, target));
	}
	
	/**
	 * Adds new file to download list.
	 * @param file
	 * @param target
	 */
	public void add(FileVersion file, OutputStream target) {
		add(new DownloadItemStream(file, target));
	}
	
	/**
	 * Adds new download item
	 * @param item
	 */
	public void add(DownloadItem item) {
		getItems().add(item);
	}
	
	/**
	 * Removes file from queue.
	 * @param file 
	 */
	public void remove(File file) {
		DownloadItem found = null;
		for(DownloadItem item : getItems()) {
			if (item.getFile() == file) {
				found = item;
				break;
			}
		}
		if (found != null) {
			getItems().remove(found);
		}
	}

	/**
	 * Starts downloading.
	 * @param target_folder default path that will be used, when no path is specified for item
	 */
	public void start(String target_folder) {
		if (getItems().size() > 0 && thread == null) {
			targetFolder = target_folder;
			nextFile();
		}
	}

	/**
	 * Starts downloading new file in queue.
	 */
	private synchronized void nextFile() {
		DownloadItem file = getItems().get(0);
		getItems().remove(0);

		String target = file.getTarget();
		if (target == null) {
			file.setTarget(targetFolder
				+ java.io.File.separator
				+ file.getFile().getName()
			);
		}
		
		fireEvent(new DownloadFileStartedEvent(file.getFile(), target));
		
		thread = new DownloadThread(file, external.getAuth(), user.getKey());
		thread.addListener(this);
		thread.start();
	}

	@Override
	public synchronized void handleEvent(Object event, Object sender) {
		if (event instanceof DownloadFileDoneEvent) {
			if (getItems().size() > 0) {
				nextFile();
			} else {
				fireEvent(new DownloadAllDoneEvent());
			}
		}
		fireEvent((DownloadEvent) event);
	}

	/**
	 * Stops downloading. All running downloads will be stopped immidiately.
	 */
	public void stop() {
		if (thread != null) {
			thread.setStopDownload(true);
			thread = null;
		}
	}

	/**
	 * @return download queue
	 */
	public List<DownloadItem> getItems() {
		return items;
	}
}
