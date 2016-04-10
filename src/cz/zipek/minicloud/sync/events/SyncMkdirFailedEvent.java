/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zipek.minicloud.sync.events;

import cz.zipek.minicloud.sync.SyncEvent;

/**
 *
 * @author Kamen
 */
public class SyncMkdirFailedEvent extends SyncEvent {
	private final String folder;

	public SyncMkdirFailedEvent(String folder) {
		this.folder = folder;
	}

	/**
	 * @return the folder
	 */
	public String getFolder() {
		return folder;
	}
	
	
}
