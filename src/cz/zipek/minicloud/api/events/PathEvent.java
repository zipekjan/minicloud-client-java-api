/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zipek.minicloud.api.events;

import cz.zipek.minicloud.api.External;
import cz.zipek.minicloud.api.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Kamen
 */
public class PathEvent extends SuccessEvent {

	protected Path path;
	
	public PathEvent(External sender, JSONObject data, String action_id) {
		super(sender, data, action_id);
		
		try {
			path = new Path(sender, data.optJSONObject("data"));
		} catch (JSONException ex) {
			Logger.getLogger(PathEvent.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * @return the path
	 */
	public Path getPath() {
		return path;
	}
	
}
