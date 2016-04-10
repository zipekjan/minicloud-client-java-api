/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.zipek.minicloud.api.download.events;

import cz.zipek.minicloud.api.File;
import cz.zipek.minicloud.api.download.DownloadEvent;

/**
 *
 * @author Kamen
 */
public class DownloadProgressEvent extends DownloadEvent {
	private final File file;
	private final long downloaded;
	private final long total;
	private final String target;
	
	public DownloadProgressEvent(File file, String target, long downloaded, long total) {
		this.file = file;
		this.target = target;
		this.downloaded = downloaded;
		this.total = total;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @return the downloaded
	 */
	public long getDownloaded() {
		return downloaded;
	}

	/**
	 * @return the total
	 */
	public long getTotal() {
		return total;
	}

	/**
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}
}
