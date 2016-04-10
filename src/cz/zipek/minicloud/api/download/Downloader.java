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
import java.nio.file.FileSystems;
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
	
	public Downloader(External aExternal, User aUser) {
		items = new LinkedList<>();
		external = aExternal;
		user = aUser;
	}
	
	public void add(FileVersion file) {
		add(new DownloadItem(file, null));
	}

	public void add(FileVersion file, String target) {
		add(new DownloadItem(file, target));
	}
	
	public void add(FileVersion file, OutputStream target) {
		add(new DownloadItemStream(file, target));
	}
	
	public void add(DownloadItem item) {
		getItems().add(item);
	}
	
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

	public void start(String target_folder) {
		if (getItems().size() > 0 && thread == null) {
			targetFolder = target_folder;
			nextFile();
		}
	}

	private synchronized void nextFile() {
		DownloadItem file = getItems().get(0);
		getItems().remove(0);

		String target = file.getTarget();
		if (target == null) {
			file.setTarget(targetFolder
				+ FileSystems.getDefault().getSeparator()
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

	public void stop() {
		if (thread != null) {
			thread.setStopDownload(true);
			thread = null;
		}
	}

	/**
	 * @return the items
	 */
	public List<DownloadItem> getItems() {
		return items;
	}
}
