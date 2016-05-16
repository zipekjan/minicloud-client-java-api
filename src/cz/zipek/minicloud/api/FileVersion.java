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

import java.util.Date;
import org.json.JSONObject;

/**
 *
 * @author Jan Zípek
 */
public class FileVersion {
	private final int id;
	private final Date created;
	private final File file;
	
	/**
	 * Creates new version of file. This class should only be
	 * instanced by File class.
	 * 
	 * @param file origin file
	 * @param data raw version data
	 */
	public FileVersion(File file, JSONObject data) {
		this.file = file;
		
		id = data.optInt("version");
		created = new Date(data.optLong("created") * 1000);
	}

	/**
	 * @return ID of file version
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return date of version creation
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * @return origin file
	 */
	public File getFile() {
		return file;
	}
}
