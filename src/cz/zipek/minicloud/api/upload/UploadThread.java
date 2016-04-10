/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zipek.minicloud.api.upload;

import cz.zipek.minicloud.api.Listener;
import cz.zipek.minicloud.api.encryption.Encryptor;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

/**
 *
 * @author Kamen
 */
public class UploadThread extends Thread implements Listener {
	protected final List<Listener> listeners = new ArrayList<>();
	
	protected final Uploader uploader;
	protected final Encryptor encryptor;
	protected final UploadItem item;
	
	public UploadThread(Uploader uploader, UploadItem item, Encryptor encryptor) {
		super("Upload thread");
		
		this.uploader = uploader;
		this.encryptor = encryptor;
		this.item = item;
	}
	
	public synchronized void addListener(Listener listener) {
		listeners.add(listener);
	}
	
	public synchronized void removeListener(Listener listener) {
		listeners.remove(listener);
	}
	
	protected synchronized void fireEvent(Object event) {
		for(Listener listener : listeners) {
			listener.handleEvent(event, this);
		}
	}
	
	private String parseFolder(String folder) {
		return folder.replaceAll("\\/{2,}","/");
	}
	
	@Override
	public void run() {
		try {
			// Helper for sending big requests
			MultipartUtility sender = new MultipartUtility(
				uploader.getSource().getApiUrl(), "UTF-8",
				uploader.getSource().getAuth(), encryptor
			);
			
			// Listen to sender events
			sender.addListener(this);
			
			// Proper aciton
			sender.addFormField("action", "upload_file");

			// Path to upload files to
			if (item.getTarget() != null) {
				sender.addFormField("path", parseFolder(item.getTarget()));
			}
			
			// Add encryption info
			if (encryptor != null) {
				sender.addFormField("encryption[file]", encryptor.getConfig());
			} else {
				sender.addFormField("encryption[file]", "");
			}
			
			// Override existing file
			if (item.getExisting() != null) {
				sender.addFormField("replace[file]", item.getExisting().getId());
				
				// Save params of remote file
				if (encryptor != null)
					item.getExisting().setEncryption(encryptor.getConfig());
				else
					item.getExisting().setEncryption("");
			}
			
			// Add file checksum (unencrypted)
			sender.addFormField("checksum[file]", item.getChecksum());
			
			// Add public param
			sender.addFormField("public[file]", Boolean.toString(item.isPublic()));
			
			// Add file
			sender.addFilePart("file", item.getFilename(), item.getStream(), item.getSize());
			
			// Start sending
			sender.finish();
		} catch (IOException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException ex) {
			Logger.getLogger(UploadThread.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void handleEvent(Object event, Object sender) {
		fireEvent(event);
	}
}
