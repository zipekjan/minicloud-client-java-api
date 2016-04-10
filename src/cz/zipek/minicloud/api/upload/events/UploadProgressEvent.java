/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.zipek.minicloud.api.upload.events;

import cz.zipek.minicloud.api.upload.UploadEvent;
import cz.zipek.minicloud.api.upload.UploadItem;
import java.io.File;

/**
 *
 * @author Kamen
 */
public class UploadProgressEvent extends UploadEvent {
	private final long total;
	private final long sent;
	private final UploadItem item;

	public UploadProgressEvent(UploadItem item, long sent, long total) {
		this.sent = sent;
		this.total = total;
		this.item = item;
	}

	/**
	 * @return the total
	 */
	public long getTotal() {
		return total;
	}

	/**
	 * @return the sent
	 */
	public long getSent() {
		return sent;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return item.getFile();
	}

	/**
	 * @return the remote
	 */
	public cz.zipek.minicloud.api.File getRemote() {
		return item.getExisting();
	}
	
	public String getTarget() {
		return item.getTarget();
	}
}
