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

/**
 *
 * @author Jan Zípek
 */
public class UploadProgressEvent extends UploadEvent {
	private final long total;
	private final long sent;
	private final UploadItem item;

	public UploadProgressEvent(UploadItem item, long sent, long total) {
		this.sent = sent;
		this.total = total;
		this.item = item;
	}

	/**
	 * @return the total
	 */
	public long getTotal() {
		return total;
	}

	/**
	 * @return the sent
	 */
	public long getSent() {
		return sent;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return item.getFile();
	}

	/**
	 * @return the remote
	 */
	public cz.zipek.minicloud.api.File getRemote() {
		return item.getExisting();
	}
	
	public String getTarget() {
		return item.getTarget();
	}
}
