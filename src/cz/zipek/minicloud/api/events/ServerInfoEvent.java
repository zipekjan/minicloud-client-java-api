/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.zipek.minicloud.api.events;

import cz.zipek.minicloud.api.External;
import cz.zipek.minicloud.api.ServerInfo;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Kamen
 */
public class ServerInfoEvent extends SuccessEvent {	
	
	protected ServerInfo serverInfo;
	
	public ServerInfoEvent(External sender, JSONObject data, String action_id) {
		super(sender, data, action_id);
		
		try {
			serverInfo = new ServerInfo(sender, data.optJSONObject("data"));
		} catch(JSONException e) {
			Logger.getLogger(ServerInfoEvent.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * @return server info
	 */
	public ServerInfo getServerInfo() {
		return serverInfo;
	}
	
}
