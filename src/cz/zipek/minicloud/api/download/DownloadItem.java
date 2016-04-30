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
 * Represents individual download item and its options.
 * 
 * @author Kamen
 */
public class DownloadItem {
	private final FileVersion version;
	private final String encryption;
	private String target;
	
	/**
	 * @param file version of file to be downloaded
	 * @param target local path where file will be downloaded
	 */
	public DownloadItem(FileVersion file, String target) {
		this.version = file;
		this.encryption = file.getFile().getEncryption();
		this.target = target;
	}
	/**
	 * @return downloaded file
	 */
	public File getFile() {
		return version.getFile();
	}
	
	/**
	 * @return downloaded version of file
	 */
	public FileVersion getVersion() {
		return version;
	}
	
	/**
	 * @return file encryption used
	 */
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
	
	/**
	 * Sets path, where file will be downloaded.
	 * @param target path where file will be downloaded
	 */
	public void setTarget(String target) {
		this.target = target;
	}
	
	/**
	 * @return path where file will be downloaded
	 */
	public String getTarget() {
		return target;
	}
	
}
