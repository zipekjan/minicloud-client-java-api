/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.zipek.minicloud.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Helper class with possibly usefull and widely used methods.
 * 
 * @author Kamen
 */
public class Tools {
	
	public enum Hash {
		MD5("MD5"),
		SHA256("SHA-256"),
		SHA512("SHA-512");
		
		private final String value;
		
		private Hash(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return value;
		}
	}
	
	/**
	 * Returns human readable file size.
	 * 
	 * @param size size to stringify
	 * @param decimal_points round precision
	 * @return human readable file size
	 */
    public static String humanFileSize(long size, int decimal_points)
    {
        String[] levels = new String[]{ "", "k", "M", "G" };
        int level = 0;
        float s = size;
        while(s > 1024)
        {
            s /= 1024;
            level++;
        }
        return Math.round(s * Math.pow(10, decimal_points))/Math.pow(10, decimal_points) + " " + levels[level] + "iB";
    }
	
	/**
	 * Calculates md5 checksum of file.
	 * 
	 * @param file
	 * @return md5 checksum
	 * @throws NoSuchAlgorithmException
	 * @throws FileNotFoundException
	 * @throws IOException 
	 */
	public static String md5Checksum(File file) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		return getDigest(new FileInputStream(file.getAbsolutePath()), md, 2048);
	}

	/**
	 * Generates hashed string.
	 * 
	 * @param is input string
	 * @param md hash method
	 * @param byteArraySize
	 * @return hashed string
	 * @throws NoSuchAlgorithmException
	 * @throws IOException 
	 */
	private static String getDigest(InputStream is, MessageDigest md, int byteArraySize)
			throws NoSuchAlgorithmException, IOException {
		md.reset();
		byte[] bytes = new byte[byteArraySize];
		int numBytes;
		while ((numBytes = is.read(bytes)) != -1) {
			md.update(bytes, 0, numBytes);
		}
		byte[] hash = md.digest();
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < hash.length; i++) {
			if ((0xff & hash[i]) < 0x10) {
				result.append("0").append(Integer.toHexString((0xFF & hash[i])));
			} else {
				result.append(Integer.toHexString(0xFF & hash[i]));
			}
		}
		return result.toString();
	}
	
	public static String md5(String what) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		return md5(what.getBytes("UTF-8"));
	}
	
	public static String md5(char[] what) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		return md5(toBytes(what));
	}
		
	public static String md5(byte[] what) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		return getHexString(MessageDigest.getInstance("MD5").digest(what));
	}
	
	public static String sha256(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		return sha256(input.getBytes("UTF-8"));
	}
	
	public static String sha256(char[] input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		return sha256(toBytes(input));
	}
	
	public static String sha256(byte[] input) throws NoSuchAlgorithmException {
		return getHexString(MessageDigest.getInstance("SHA-256").digest(input));
	}
	
	public static byte[] sha256Bytes(char[] input) throws NoSuchAlgorithmException {
		return sha256Bytes(toBytes(input));
	}
	
	public static byte[] sha256Bytes(byte[] input) throws NoSuchAlgorithmException {
		return MessageDigest.getInstance("SHA-256").digest(input);
	}
	
	public static String hash(String input, Hash encoder) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		return hash(input.getBytes("UTF-8"), encoder);
	}
	
	public static String hash(char[] input, Hash encoder) throws NoSuchAlgorithmException {
		return hash(toBytes(input), encoder);
	}
	
	public static String hash(byte[] input, Hash encoder) throws NoSuchAlgorithmException {
		return getHexString(MessageDigest.getInstance(encoder.toString()).digest(input));
	}
	
	public static byte[] toBytes(char[] chars) {
		CharBuffer charBuffer = CharBuffer.wrap(chars);
		ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
		byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
				byteBuffer.position(), byteBuffer.limit());
		Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
		Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
		return bytes;
	}
	
	public static String getHexString(byte[] bytes) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			if ((0xff & bytes[i]) < 0x10) {
				result.append("0").append(Integer.toHexString((0xFF & bytes[i])));
			} else {
				result.append(Integer.toHexString(0xFF & bytes[i]));
			}
		}
		return result.toString();
	}
}

