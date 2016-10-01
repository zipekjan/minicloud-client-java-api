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
package cz.zipek.minicloud.api;

import cz.zipek.minicloud.api.encryption.Encryptor;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Contains informations about logged user.
 * 
 * @author Jan Zípek
 */
public class User {
	
	/**
	 * Origin of user data.
	 */
	protected External source;
	
	/**
	 * Unique identifier of user.
	 */
	protected int id;
	
	protected String name;
	protected String email;
	protected char[] password;
	
	/**
	 * Key in raw form.
	 */
	protected byte[] key;
	private String keyEncryption;
	
	protected boolean admin;
	
	public User(External api, JSONObject data, char[] password) throws JSONException {	
		this(api, data);
		
		this.password = password;
	}
	
	public User(External api, JSONObject data) throws JSONException {
		
		source = api;
		
		id = data.getInt("id");
		
		name = data.getString("name");
		email = data.optString("email", null);
		
		String keySource = data.optString("key", null);
		
		if (keySource != null && keySource.length() > 0) {
			try {
				key = Base64.decode(keySource);
			} catch (IOException ex) {
				key = null;
			}
		}
		
		keyEncryption = data.optString("key_encryption", null);
		
		admin = data.optBoolean("admin", false) || (data.optInt("admin", 0) == 1);
		
	}
	
	public User(External api, String name, String email, char[] password, boolean admin) {
		
		source = api;
		this.name = name;
		this.email = email;
		this.password = password;
		this.admin = admin;
		
	}

	/**
	 * API used to fetch this info.
	 * 
	 * @return the source
	 */
	public External getSource() {
		return source;
	}

	/**
	 * Unique ID representing this user.
	 * This id is used to identify user on server.
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets unhashed user password.
	 * This change is only local. You need to call save method to apply changes to server.
	 * Unhashed user password needs to be saved in order to decrypt user key.
	 * 
	 * @param password unhashed password 
	 * @param salt salt to be used for password
	 * @param decrypt 
	 * @throws java.security.NoSuchAlgorithmException 
	 * @throws javax.crypto.NoSuchPaddingException 
	 * @throws java.security.InvalidKeyException 
	 * @throws javax.crypto.IllegalBlockSizeException 
	 * @throws javax.crypto.BadPaddingException 
	 * @throws java.security.InvalidAlgorithmParameterException 
	 * @throws java.io.UnsupportedEncodingException 
	 * @throws java.security.NoSuchProviderException 
	 */
	public void setPassword(char[] password, char[] salt, boolean decrypt) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException {
		// Salt password
		this.password = new char[password.length + salt.length];
		System.arraycopy(salt, 0, this.password, 0, salt.length);
		System.arraycopy(password, 0, this.password, salt.length, password.length);
		
		// Decrypt user key using password
		if (key != null && decrypt) {
			key = getKeyEncryptor().decrypt(key);
		}
	}
	
	/**
	 * Returns unhashed user password.
	 * Unhashed user password is used to encrypt/decrypt user key.
	 * 
	 * @return unhashed password
	 */
	public char[] getPassword() {
		if (password == null)
			return password;
		return Arrays.copyOf(password, password.length);
	}
	
	/**
	 * Changes user name. This change is only local,
	 * setUser needs to be called to save changes to
	 * server.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Sets admin attribute. This change is only local,
	 * adminSetUser needs to be called to save changes to
	 * server.
	 * 
	 * @param admin
	 */
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	
	/**
	 * User name used for login.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets user email.
	 * This change is only local. You need to call save method to apply changes to server.
	 * 
	 * @param email 
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * Returns user email.
	 * Email is currently unused.
	 * 
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets user encryption key.
	 * Key must be unencrypted.
	 * 
	 * @param key 
	 */
	public void setKey(byte[] key) {
		this.key = key;
	}
	
	/**
	 * Returns unencrypted user key.
	 * 
	 * @return the key
	 */
	public byte[] getKey() {
		return key;
	}
	
	/**
	 * Returns user key.
	 * 
	 * @param encrypted
	 * @return the key
	 * @throws java.security.NoSuchAlgorithmException
	 * @throws javax.crypto.NoSuchPaddingException
	 * @throws java.security.InvalidKeyException
	 * @throws javax.crypto.IllegalBlockSizeException
	 * @throws javax.crypto.BadPaddingException
	 * @throws java.security.InvalidAlgorithmParameterException
	 * @throws java.io.UnsupportedEncodingException
	 * @throws java.security.NoSuchProviderException
	 */
	public byte[] getKey(boolean encrypted) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, NoSuchProviderException {
		
		// Skip null key
		if (key == null)
			return key;

		// Encrypt user key using password
		if (encrypted) {
			return getKeyEncryptor().encrypt(Arrays.copyOf(key, key.length));
		}
		
		return key;
	}

	/**
	 * Returns if user is admin.
	 * 
	 * @return is user admin
	 */
	public boolean isAdmin() {
		return admin;
	}
	
	/**
	 * Returns encryptor with user key applied.
	 * This method is shorcut for creating correct encryptor for user.
	 * 
	 * @param options Cipher options
	 * @return encryptor with user key applied
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException 
	 * @throws java.security.NoSuchProviderException 
	 */
	public Encryptor getEncryptor(String options) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
		if (options == null || options.length() == 0)
			return null;
		return new Encryptor(key, options);
	}
	
	/**
	 * Builds list of parameters used to update file.
	 * This list contains all changeable params of file.
	 * It's used when calling updateFile method of API.
	 * 
	 * @return updatable file params with current values
	 */
	public Map<String, String> getUpdate() {
		return getUpdate(false);
		
	}
	
	/**
	 * Builds list of parameters used to update file.
	 * This list contains all changeable params of file.
	 * It's used when calling updateFile method of API.
	 * 
	 * @param extended include name and role
	 * @return updatable file params with current values
	 */
	public Map<String, String> getUpdate(boolean extended) {
		Map<String, String> items = new HashMap<>();
		
		if (getPassword() != null) {
			try {
				items.put("password", Tools.sha256(getPassword()));
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
				Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		try {
			byte[] crypto = getKey(true);
			if (crypto != null) {
				items.put("key", Base64.encodeBytes(crypto));
				items.put("key_encryption", getKeyEncryption());
			}
		} catch (NoSuchProviderException | InvalidAlgorithmParameterException | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
			Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		items.put("email", getEmail());
		
		if (extended) {
			items.put("name", getName());
			items.put("admin", Boolean.toString(isAdmin()));
		}
		
		return items;
	}
	
	/**
	 * Applies local changes to server.
	 * This is asychronous call.
	 * You will need to listen on API for success event with same action id to confirm successfull save.
	 * 
	 * @return action id
	 */
	public String save() {
		try {
			return getSource().setUser(this);
		} catch (NoSuchProviderException ex) {
			Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	
	/**
	 * Creates encryptor that can be used to encrypt/decrypt user key.
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws UnsupportedEncodingException 
	 */
	private Encryptor getKeyEncryptor() throws NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, NoSuchProviderException {		
		return new Encryptor(getPasswordHash(), getKeyEncryption());
	}
	
	/**
	 * Builds password hash using PBKDF2
	 * 
	 * @return password hash
	 * @throws NoSuchAlgorithmException 
	 */
	private byte[] getPasswordHash() throws NoSuchAlgorithmException {
		int iterations = 1000;
        char[] chars = getPassword();
        byte[] salt = new byte[16];

        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 256);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		
		try {
			return skf.generateSecret(spec).getEncoded();
		} catch (InvalidKeySpecException ex) {
			Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		return null;
	}

	/**
	 * Encryption options used to ecnrypt key.
	 * User key is encrypted using SHA256 of user password.
	 * Encryption options should have following format:
	 *	ALGORITHM/MODE/PADDING
	 * 
	 * @return the keyEncryption
	 */
	public String getKeyEncryption() {
		return keyEncryption;
	}

	/**
	 * Sets encryption options used to encrypt key.
	 * User key is encrypted using SHA256 of user password.
	 * Encryption options should have following format:
	 *	ALGORITHM/MODE/PADDING
	 * 
	 * @param keyEncryption the keyEncryption to set
	 */
	public void setKeyEncryption(String keyEncryption) {
		this.keyEncryption = keyEncryption;
	}
	
}
