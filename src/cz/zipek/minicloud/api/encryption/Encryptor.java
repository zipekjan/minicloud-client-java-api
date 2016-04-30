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
 * @author Kamen
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
