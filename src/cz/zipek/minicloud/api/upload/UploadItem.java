/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zipek.minicloud.api.upload;

import cz.zipek.minicloud.api.File;
import cz.zipek.minicloud.api.Tools;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kamen
 */
public class UploadItem {
	private final java.io.File file;
	private final File existing;
	private final InputStream stream;
	
	private String target;
	private boolean isPublic;
	private boolean createVersion;
	private String filename;
	private long size;
	
	public UploadItem(java.io.File file, boolean isPublic) {
		this.file = file;
		this.target = null;
		this.existing = null;
		this.stream = null;
		
		this.filename = file.getName();
		this.size = file.length();
		
		this.isPublic = isPublic;
	}
	
	public UploadItem(java.io.File file, String target, boolean isPublic) {
		this.file = file;
		this.target = target;
		this.existing = null;
		this.stream = null;
		
		this.filename = file.getName();
		this.size = file.length();
		
		this.isPublic = isPublic;
	}
	
	public UploadItem(java.io.File file, File existing) {
		this(file, existing, existing.isPublic());
	}
	
	public UploadItem(java.io.File file, File existing, boolean isPublic) {
		this(file, existing, isPublic, true);
	}
	
	public UploadItem(java.io.File file, File existing, boolean isPublic, boolean createVersion) {
		this.file = file;
		this.existing = existing;
		this.target = null;
		this.stream = null;
		
		this.filename = existing.getName();
		this.size = file.length();
		
		this.isPublic = isPublic;
		this.createVersion = createVersion;
	}
	
	public UploadItem(InputStream stream, File existing) {
		this(stream, existing.getName(), existing.getSize(), existing, existing.isPublic());
	}
	
	public UploadItem(InputStream stream, File existing, boolean isPublic) {
		this(stream, existing.getName(), existing.getSize(), existing, isPublic);
	}
	
	public UploadItem(InputStream stream, File existing, boolean isPublic, boolean createVersion) {
		this(stream, existing.getName(), existing.getSize(), existing, isPublic, createVersion);
	}
	
	public UploadItem(InputStream stream, String filename, long size, File existing, boolean isPublic) {
		this(stream, filename, size, existing, isPublic, true);
	}
	
	public UploadItem(InputStream stream, String filename, long size, File existing, boolean isPublic, boolean createVersion) {
		this.file = null;
		this.existing = existing;
		this.target = null;
		this.stream = stream;
		
		this.filename = filename;
		this.size = size;
		this.isPublic = isPublic;
		this.createVersion = createVersion;
	}

	/**
	 * @return the file
	 */
	public java.io.File getFile() {
		return file;
	}

	/**
	 * @return the existing
	 */
	public File getExisting() {
		return existing;
	}

	/**
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}
	
	public InputStream getStream() throws FileNotFoundException {
		if (file != null) {
			return new FileInputStream(file);
		}
		return stream;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public long getSize() {
		return size;
	}
	
	public String getChecksum() {
		try {
			return file != null ? Tools.md5Checksum(file) : existing.getChecksum();
		} catch (NoSuchAlgorithmException | IOException ex) {
			Logger.getLogger(UploadItem.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	
	public boolean isPublic() {
		return isPublic;
	}
	
	public boolean shouldCreateVersion() {
		return createVersion;
	}

	public void setTarget(String targetFolder) {
		target = targetFolder;
	}
	
}
