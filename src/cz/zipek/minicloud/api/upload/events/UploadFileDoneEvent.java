/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zipek.minicloud.api.upload.events;

import cz.zipek.minicloud.api.upload.UploadEvent;
import cz.zipek.minicloud.api.upload.UploadItem;
import java.io.File;
import org.json.JSONObject;

/**
 *
 * @author Kamen
 */
public class UploadFileDoneEvent extends UploadEvent {
	private final JSONObject data;
	private final UploadItem item;
	
	public UploadFileDoneEvent(UploadItem item, JSONObject data) {
		this.item = item;
		this.data = data;
	}
	
	/**
	 * @return the file
	 */
	public File getFile() {
		return item.getFile();
	}
	
	/**
	 * @return the target
	 */
	public String getTarget() {
		return item.getTarget();
	}

	/**
	 * @return the data
	 */
	public JSONObject getData() {
		return data;
	}

	/**
	 * @return the remote
	 */
	public cz.zipek.minicloud.api.File getRemote() {
		return item.getExisting();
	}
}
