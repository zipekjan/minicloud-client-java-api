/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zipek.minicloud.api.events;

import cz.zipek.minicloud.api.External;
import cz.zipek.minicloud.api.Path;
import cz.zipek.minicloud.api.User;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Kamen
 */
public class UsersEvent extends SuccessEvent {

	protected User[] users;
	
	public UsersEvent(External sender, JSONObject data, String action_id) {
		super(sender, data, action_id);
		
		try {
			JSONArray list = data.optJSONArray("data");

			users = new User[list.length()];
			for(int i = 0; i < list.length(); i++) {
				users[i] = new User(sender, list.optJSONObject(i));
			}
		} catch (JSONException ex) {
			Logger.getLogger(PathsEvent.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public User[] getUsers() {
		return users;
	}
	
}
