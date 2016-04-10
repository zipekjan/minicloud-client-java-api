/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zipek.minicloud.api.upload;

import cz.zipek.minicloud.api.Eventor;
import cz.zipek.minicloud.api.encryption.Encryptor;
import cz.zipek.minicloud.api.upload.events.UploadThreadSentEvent;
import cz.zipek.minicloud.api.upload.events.UploadFailedEvent;
import cz.zipek.minicloud.api.upload.events.UploadThreadProgressEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
 
/**
 * This utility class provides an abstraction layer for sending multipart HTTP
 * POST requests to a web server. 
 * @author www.codejava.net
 *
 */
public class MultipartUtility extends Eventor<UploadEvent> {
    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private final HttpURLConnection httpConn;
    private final String charset;
    private final OutputStream outputStream;
    private final PrintWriter writer;
	
	private final Encryptor encryptor;
	
    /**
     * This constructor initializes a new HTTP POST request with content type
     * is set to multipart/form-data
     * @param requestURL
     * @param charset
	 * @param auth
	 * @param encrypt
     * @throws IOException
     */
    public MultipartUtility(String requestURL, String charset, String auth, Encryptor encrypt)
            throws IOException {
       
		this.charset = charset;
        this.encryptor = encrypt;
		
        // creates a unique boundary based on time stamp
        boundary = "===" + System.currentTimeMillis() + "===";
         
        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setChunkedStreamingMode(4096);
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		httpConn.setRequestProperty("X-Auth", auth);
		
		outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
    }
 
    /**
     * Adds a form field to the request
     * @param name field name
     * @param value field value
     */
    public void addFormField(String name, String value) {
		writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=" + charset).append(
                LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }
	
	
	public void addFormField(String name, int value) {
		addFormField(name, Integer.toString(value));
	}
 
	/**
     * Adds a upload file section to the request 
     * @param fieldName name attribute 
	 * @param fileName input stream filename
	 * @param stream input stream
	 * @param size input stream size
     * @throws IOException
	 * @throws java.security.InvalidKeyException
	 * @throws javax.crypto.IllegalBlockSizeException
	 * @throws javax.crypto.BadPaddingException
	 * @throws java.security.InvalidAlgorithmParameterException
     */
	public void addFilePart(String fieldName, String fileName, InputStream stream, long size) throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
                "Content-Disposition: form-data; name=\"" + fieldName
                        + "\"; filename=\"" + fileName + "\"")
                .append(LINE_FEED);
        writer.append(
                "Content-Type: "
                        + URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();
 
		byte[] buffer = new byte[4096];
		int sentNow;
		long sentTotal;

		System.out.println("Upload file " + fileName);
		
		CipherOutputStream cipherStream = null;
		if (encryptor != null) {
			cipherStream = encryptor.getOutputStream(new NotClosingOutputStream(outputStream), Cipher.ENCRYPT_MODE);
		
			System.out.println("(Upload) Encryption used: " + encryptor.getConfig());
		} else {
			System.out.println("(Upload) No encryption");
		}

		sentTotal = 0;
		while ((sentNow = stream.read(buffer)) != -1) {

			if (cipherStream != null) {
				cipherStream.write(buffer, 0, sentNow);
			} else {
				outputStream.write(buffer, 0, sentNow);
			}

			sentTotal += sentNow;
			fireEvent(new UploadThreadProgressEvent(sentTotal, size));

		}
		
		if (cipherStream != null) {
			cipherStream.flush();
			cipherStream.close();
		}

		outputStream.flush();
        writer.flush();   
    }
	
    /**
     * Adds a upload file section to the request 
     * @param fieldName name attribute
     * @param uploadFile a File to be uploaded 
     * @throws IOException
	 * @throws java.security.InvalidKeyException
	 * @throws javax.crypto.IllegalBlockSizeException
	 * @throws javax.crypto.BadPaddingException
	 * @throws java.security.InvalidAlgorithmParameterException
     */
    public void addFilePart(String fieldName, File uploadFile) throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		addFilePart(fieldName, uploadFile.getName(), new FileInputStream(uploadFile), uploadFile.length());
    }
 
    /**
     * Completes the request and receives response from the server.
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public String finish() throws IOException {
		StringBuilder data = new StringBuilder();
		
        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        // checks server's status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(
					httpConn.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (data.length() != 0)
						data.append(LINE_FEED);
					data.append(line);
				}
			}
			httpConn.disconnect();
        } else {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(
					httpConn.getErrorStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (data.length() != 0)
						data.append(LINE_FEED);
					data.append(line);
				}
			}
			
			fireEvent(new UploadFailedEvent(data.toString()));
        }
        
        fireEvent(new UploadThreadSentEvent(data.toString()));
 
        return data.toString();
    }
}
