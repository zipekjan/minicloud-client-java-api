/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.zipek.minicloud.api.upload.events;

import cz.zipek.minicloud.api.upload.UploadEvent;

/**
 *
 * @author Kamen
 */
public class UploadThreadProgressEvent extends UploadEvent {
	private final long total;
	private final long sent;

	public UploadThreadProgressEvent(long sent, long total) {
		this.sent = sent;
		this.total = total;
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
}
