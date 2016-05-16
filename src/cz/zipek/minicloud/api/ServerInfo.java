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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Contains informations about minicloud server.
 * 
 * @author Jan Zípek
 */
public class ServerInfo {
	
	private final External source;
	
	private final String name;
	private final String description;
	private final String logo;
	
	private final boolean niceUrl;
	
	private final long time;
	private final long offset;
	
	/**
	 * Should only be instanced from API.
	 * 
	 * @param from origin 
	 * @param info raw data
	 * @throws JSONException thrown when data are malformed 
	 */
	public ServerInfo(External from, JSONObject info) throws JSONException {
		source = from;
		name = info.optString("name", "");
		description = info.optString("description", "");
		niceUrl = info.optBoolean("nice_url", false);
		logo = info.optString("logo", null);
		time = info.optLong("time", System.currentTimeMillis() / 1000L);
		offset = System.currentTimeMillis() / 1000L - time;
	}

	/**
	 * @return name of server
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return server description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return url to server logo
	 */
	public String getLogo() {
		return logo;
	}

	/**
	 * @return support for nice urls
	 */
	public boolean hasNiceUrl() {
		return niceUrl;
	}

	/**
	 * @return info origin
	 */
	public External getSource() {
		return source;
	}

	/**
	 * @return unix timestamp on server in time of request
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @return time offset from this machine in seconds
	 */
	public long getOffset() {
		return offset;
	}
	
}
