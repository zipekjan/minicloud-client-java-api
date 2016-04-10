/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zipek.minicloud.api.events;

import cz.zipek.minicloud.api.External;
import cz.zipek.minicloud.api.File;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Kamen
 */
public class FilesEvent extends SuccessEvent {

	protected File[] files;
	
	public FilesEvent(External sender, JSONObject data, String action_id) {
		super(sender, data, action_id);
		
		JSONArray list = data.optJSONArray("data");
		
		files = new File[list.length()];
		for(int i = 0; i < list.length(); i++) {
			files[i] = new File(sender, list.optJSONObject(i));
		}
	}
	
	public File[] getFiles() {
		return files;
	}
	
}
