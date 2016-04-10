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
public class BoolEvent extends SuccessEvent {	
	
	protected boolean bool;
	
	public BoolEvent(External sender, JSONObject data, String action_id) {
		super(sender, data, action_id);
		
		bool = data.optJSONObject("data").optBoolean("bool");
	}
	
	public boolean getBool() {
		return bool;
	}
}

