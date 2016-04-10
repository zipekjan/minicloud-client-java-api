/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.zipek.minicloud.api.upload.events;

import cz.zipek.minicloud.api.upload.UploadEvent;
import org.json.JSONObject;

/**
 *
 * @author Kamen
 */
public class UploadFailedEvent extends UploadEvent {
	
	private String response;
	
	public UploadFailedEvent(String response) {
		this.response = response;
	}
	
	/**
	 * @return the response
	 */
	public String getResponse() {
		return response;
	}
}
