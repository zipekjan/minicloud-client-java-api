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
package cz.zipek.minicloud.api.upload.events;

import cz.zipek.minicloud.api.upload.UploadEvent;
import cz.zipek.minicloud.api.upload.UploadItem;
import java.io.File;
import org.json.JSONObject;

/**
 *
 * @author Jan Zípek
 */
public class UploadFileDoneEvent extends UploadEvent {
	private final JSONObject data;
	private final UploadItem item;
	
	public UploadFileDoneEvent(UploadItem item, JSONObject data) {
		this.item = item;
		this.data = data;
	}
	
	/**
	 * @return the file
	 */
	public File getFile() {
		return item.getFile();
	}
	
	/**
	 * @return the target
	 */
	public String getTarget() {
		return item.getTarget();
	}

	/**
	 * @return the data
	 */
	public JSONObject getData() {
		return data;
	}

	/**
	 * @return the remote
	 */
	public cz.zipek.minicloud.api.File getRemote() {
		return item.getExisting();
	}
}
