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
package cz.zipek.minicloud.api.encryption;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Jan Zípek <jan at zipek.cz>
 */
public class Encryptor {
	
	private String config;
	
	private Cipher cipher;
	
	private SecretKeySpec key;

	private IvParameterSpec iv;
	
	/**
	 * Create encryptor using default options.
	 * 
	 * @param key key for encryption
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws NoSuchProviderException 
	 */
	public Encryptor(byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
		this(key, "AES/CBC/PKCS5Padding");
	}
	
	/**
	 * @param rawKey key for encryption
	 * @param aConfig encryption config, JCA compatible
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws NoSuchProviderException 
	 */
	public Encryptor(byte[] rawKey, String aConfig) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
		
		config = aConfig;
		cipher = Cipher.getInstance(config);
		
		// Load cipher name, needed for key config
		String cipherName = config;
		if (cipherName.contains("/")) {
			cipherName = cipherName.split("/")[0];
		}
		
		// Create IV, assume IV needs to be same size as block
		byte[] ivParam = new byte[cipher.getBlockSize()];
		Arrays.fill( ivParam, (byte) 0 );
		
		// Create encryption params
		key = new SecretKeySpec(rawKey, cipherName);
		iv = new IvParameterSpec(ivParam);
		
	}
	
	/**
	 * Creates encrypted/decrypted output stream.
	 * 
	 * @param stream original stream
	 * @param optmode javax.crypto.ENCRYPT_MODE or javax.crypto.DECRYPT_MODE
	 * @return cipher stream
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException 
	 */
	public CipherOutputStream getOutputStream(OutputStream stream, int optmode) throws InvalidKeyException, InvalidAlgorithmParameterException {
		
		cipher.init(optmode, key, iv);
		return new CipherOutputStream(stream, cipher);
		
	}
	
	/**
	 * Creates encrypted/decrypted input stream.
	 * 
	 * @param stream original stream
	 * @param optmode javax.crypto.Cipher.ENCRYPT_MODE or javax.crypto.Cipher.DECRYPT_MODE
	 * @return cipher stream
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 */
	public CipherInputStream getInputStream(InputStream stream, int optmode) throws InvalidKeyException, InvalidAlgorithmParameterException {
		
		cipher.init(optmode, key, iv);
		return new CipherInputStream(stream, cipher);
		
	}
	
	/**
	 * Encrypt bytes.
	 * 
	 * @param input
	 * @return encrypted bytes
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidAlgorithmParameterException 
	 */
	public byte[] encrypt(byte[] input) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		return cipher.doFinal(input);

	}
	
	/**
	 * Decrypt bytes.
	 * 
	 * @param input
	 * @return decrypted bytes
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidAlgorithmParameterException
	 */
	public byte[] decrypt(byte[] input) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		
		cipher.init(Cipher.DECRYPT_MODE, key, iv);
		return cipher.doFinal(input);
		
	}
	
	/**
	 * @return encryptor config (JCE compatible)
	 */
	public String getConfig() {
		return config;
	}
	
}
