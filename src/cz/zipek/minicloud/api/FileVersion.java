/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zipek.minicloud.api;

import java.util.Date;
import org.json.JSONObject;

/**
 *
 * @author Kamen
 */
public class FileVersion {
	private final int id;
	private final Date created;
	private final File file;
	
	public FileVersion(File file, JSONObject data) {
		this.file = file;
		
		id = data.optInt("version");
		created = new Date(data.optLong("created") * 1000);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the created
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}
}
