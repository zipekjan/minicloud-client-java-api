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
public class DownloadStoppedEvent extends DownloadEvent {
	private final File file;
	
	public DownloadStoppedEvent(File file) {
		this.file = file;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}
}
