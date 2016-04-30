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
	
	/**
	 * Creates new version of file. This class should only be
	 * instanced by File class.
	 * 
	 * @param file origin file
	 * @param data raw version data
	 */
	public FileVersion(File file, JSONObject data) {
		this.file = file;
		
		id = data.optInt("version");
		created = new Date(data.optLong("created") * 1000);
	}

	/**
	 * @return ID of file version
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return date of version creation
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * @return origin file
	 */
	public File getFile() {
		return file;
	}
}
