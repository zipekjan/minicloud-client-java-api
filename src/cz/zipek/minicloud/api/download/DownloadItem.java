/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template version, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zipek.minicloud.api.download;

import cz.zipek.minicloud.api.File;
import cz.zipek.minicloud.api.FileVersion;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 *
 * @author Kamen
 */
public class DownloadItem {
	private final FileVersion version;
	private final String encryption;
	private String target;
	
	public DownloadItem(FileVersion file, String target) {
		this.version = file;
		this.encryption = file.getFile().getEncryption();
		this.target = target;
	}
	/**
	 * @return the version
	 */
	public File getFile() {
		return version.getFile();
	}
	
	/**
	 * @return 
	 */
	public FileVersion getVersion() {
		return version;
	}
	
	public String getEncryption() {
		return this.encryption;
	}

	/**
	 * Prepares stream for download output.
	 * 
	 * @return download output stream
	 * @throws FileNotFoundException 
	 */
	public OutputStream getStream() throws FileNotFoundException {
		return new FileOutputStream(getTarget());
	}
	
	public void setTarget(String target) {
		this.target = target;
	}
	
	/**
	 * @return path to target version
	 */
	public String getTarget() {
		return target;
	}
	
}
