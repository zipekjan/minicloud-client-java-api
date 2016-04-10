/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zipek.minicloud.api.download;

import cz.zipek.minicloud.api.File;
import cz.zipek.minicloud.api.FileVersion;
import cz.zipek.minicloud.api.Listener;
import cz.zipek.minicloud.api.download.events.DownloadFailedEvent;
import cz.zipek.minicloud.api.download.events.DownloadStoppedEvent;
import cz.zipek.minicloud.api.download.events.DownloadFileDoneEvent;
import cz.zipek.minicloud.api.download.events.DownloadProgressEvent;
import cz.zipek.minicloud.api.encryption.Encryptor;
import cz.zipek.minicloud.api.upload.NotClosingOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

class DownloadThread extends Thread
{
	private final DownloadItem item;
	private final String auth;
	private final byte[] key;
	
	private boolean stopDownload;

	public DownloadThread(DownloadItem item, String auth, byte[] key) {
		super("File download");
		this.item = item;
		this.auth = auth;
		this.key = key;
	}
	
	protected final List<Listener> listeners = new ArrayList<>();
	
	public synchronized void addListener(Listener listener) {
		listeners.add(listener);
	}
	
	public synchronized void removeListener(Listener listener) {
		listeners.remove(listener);
	}
	
	protected synchronized void fireEvent(DownloadEvent event) {
		for(Listener listener : listeners) {
			listener.handleEvent(event, this);
		}
	}

	@Override
	public void run() {
		File source = item.getFile();
		FileVersion version = item.getVersion();
		String target = item.getTarget();
		String encryption = item.getEncryption();
		
		try {
			URL url = new URL(this.getSource().getDownloadLink(version));
			
			System.out.println("Downloading " + url);
			
			Encryptor encryptor = null;
			if (encryption != null && encryption.length() > 0) {
				encryptor = new Encryptor(key, encryption);
			}
			
			HttpURLConnection httpConn;
			
			try {
				httpConn = (HttpURLConnection) url.openConnection();
				httpConn.setDoOutput(true);
				httpConn.setDoInput(true);
				httpConn.setChunkedStreamingMode(4096);
				httpConn.setRequestProperty("X-Auth", auth);
				
				int status = httpConn.getResponseCode();
				if (status == HttpURLConnection.HTTP_OK) {
					OutputStream outputStream;
					long total;
					long downloaded;
					int bytesRead;
					byte[] buffer;
					try (InputStream inputStream = httpConn.getInputStream()) {
						outputStream = item.getStream();
						
						CipherOutputStream cipherStream = null;
						if (encryptor != null) {
							cipherStream = encryptor.getOutputStream(new NotClosingOutputStream(outputStream), Cipher.DECRYPT_MODE);
						
							System.out.println("(Download) Encryption used: " + encryptor.getConfig());
						} else {
							System.out.println("(Download) No encryption");
						}

						buffer = new byte[4096];
						total = httpConn.getContentLengthLong();
						downloaded = 0;

						while ((bytesRead = inputStream.read(buffer)) != -1 && !stopDownload) {
							
							//@TODO: Block size should be same
							if (cipherStream != null) {
								cipherStream.write(buffer, 0, bytesRead);
							} else {
								outputStream.write(buffer, 0, bytesRead);
							}

							downloaded += bytesRead;
							fireEvent(new DownloadProgressEvent(source, target, downloaded, total));
						}

						if (cipherStream != null) {
							cipherStream.flush();
							cipherStream.close();
						}
						
						outputStream.flush();
						outputStream.close();
						inputStream.close();

						if (stopDownload) {
							fireEvent(new DownloadStoppedEvent(source));
						} else {
							fireEvent(new DownloadFileDoneEvent(source, target));
						}
						
					} catch (IOException | InvalidKeyException | InvalidAlgorithmParameterException ex) {
						fireEvent(new DownloadFailedEvent(source, ex));
						Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
					}
				} else {
					fireEvent(new DownloadFailedEvent(source, null));
				}
			} catch (IOException ex) {
				fireEvent(new DownloadFailedEvent(source, ex));
				Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
			}
		} catch (NoSuchProviderException | NoSuchAlgorithmException | NoSuchPaddingException | MalformedURLException ex) {
			fireEvent(new DownloadFailedEvent(source, ex));
			Logger.getLogger(DownloadThread.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * @return the source
	 */
	public File getSource() {
		return item.getFile();
	}

	/**
	 * @return the target
	 */
	public String getTarget() {
		return item.getTarget();
	}

	/**
	 * @param stopDownload the stopDownload to set
	 */
	public void setStopDownload(boolean stopDownload) {
		this.stopDownload = stopDownload;
	}
}
