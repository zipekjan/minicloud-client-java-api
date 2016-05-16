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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Representation of path (directory/folder) on minicloud server.
 * 
 * @author Jan Zípek
 */
public class Path {
	
	private final External source;
	
	private final int id;
	private final int parent;
	
	private final String name;
	private final String path;
	
	private final String checksum;

	private final Date mktime;
	private final Date mdtime;
	
	private final List<File> files = new ArrayList<>();
	private final List<Path> paths = new ArrayList<>();
	
	/**
	 * Creates new path representation. Should only be instanced
	 * from API.
	 * 
	 * @param api origin
	 * @param data raw path data
	 * @throws JSONException thrown when raw data are malformed
	 */
	public Path(External api, JSONObject data) throws JSONException {
		
		source = api;
		
		id = data.optInt("id", -1);
		parent = data.optInt("parent_id", -1);
		
		path = data.optString("path", null);
		if (path != null) {
			String[] split = path.split("/");
			if (split.length > 0) {
				name = split[split.length - 1];
			} else {
				name = null;
			}
		} else {
			name = null;
		}
		
		checksum = data.optString("checksum", null);
		
		if (data.optLong("mktime", -1) > 0) {
			mktime = new Date(data.getLong("mktime") * 1000);
		} else {
			mktime = null;
		}
		
		if (data.optLong("mdtime", -1) > 0) {
			mdtime = new Date(data.getLong("mdtime") * 1000);
		} else {
			mdtime = null;
		}
		
		JSONArray ch_files = data.getJSONArray("files");
		JSONArray ch_paths = data.getJSONArray("paths");
		
		for(int i = 0, l = ch_files.length(); i < l; i++) {
			File file = new File(this.source, ch_files.getJSONObject(i));
			file.setParent(this);
			files.add(file);
		}
		
		for(int i = 0, l = ch_paths.length(); i < l; i++) {
			paths.add(new Path(this.source, ch_paths.getJSONObject(i)));
		}
		
	}
	
	/**
	 * Loads list of all files including child folders.
	 * 
	 * @return 
	 */
	public List<File> getAllFiles() {
		List<File> items = new ArrayList<>();
		items.addAll(files);
		
		for(Path child : paths) {
			items.addAll(child.getAllFiles());
		}
		
		return items;
	}
	
	/**
	 * @return the source
	 */
	public External getSource() {
		return source;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the parent
	 */
	public int getParent() {
		return parent;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return the checksum
	 */
	public String getChecksum() {
		return checksum;
	}

	/**
	 * @return the mktime
	 */
	public Date getMktime() {
		return mktime;
	}

	/**
	 * @return the mdtime
	 */
	public Date getMdtime() {
		return mdtime;
	}

	/**
	 * @return the files
	 */
	public List<File> getFiles() {
		return files;
	}

	/**
	 * @return the paths
	 */
	public List<Path> getPaths() {
		return paths;
	}
	
	/**
	 * Returns path relative to specified path.
	 * 
	 * @param relative_to path result will be relative to
	 * @return path relative to argument
	 */
	public String getRelativePath(Path relative_to) {
		return getRelativePath(relative_to.getPath());
	}
	
	/**
	 * Returns path relative to specified path.
	 * 
	 * @param relative_to path result will be relative to
	 * @return path relative to argument
	 */
	public String getRelativePath(String relative_to) {
		
		// Sanitize path a little
		if (relative_to.length() > 0 && relative_to.charAt(0) == '/')
			relative_to = relative_to.substring(1);
		if (relative_to.length() > 0 && relative_to.charAt(relative_to.length() - 1) == '/')
			relative_to = relative_to.substring(0, relative_to.length() - 1);
		
		return path.substring(relative_to.length());
	}
	
}
