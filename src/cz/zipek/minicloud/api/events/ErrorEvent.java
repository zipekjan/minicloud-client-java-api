/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zipek.minicloud.api.events;

import cz.zipek.minicloud.api.External;
import org.json.JSONObject;

/**
 *
 * @author Kamen
 */
public class ErrorEvent extends cz.zipek.minicloud.api.Event {	
	
	protected String message;
	
	public ErrorEvent(External sender, JSONObject data, String action_id) {
		super(sender, data, action_id);
		
		if (data != null)
			message = data.optString("data");
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	
}
