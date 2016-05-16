/* 
 * The MIT License
 *
 * Copyright 2016 Jan Zípek <jan at zipek.cz>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
 * @author Jan Zípek <jan at zipek.cz>
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
			
			// Add version param (only needed when forbidden)
			if (!item.shouldCreateVersion()) {
				sender.addFormField("version[file]", "0");
			}
			
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
