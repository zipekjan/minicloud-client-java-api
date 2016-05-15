package cz.zipek.minicloud.api;

import cz.zipek.minicloud.api.events.BoolEvent;
import cz.zipek.minicloud.api.events.ErrorEvent;
import cz.zipek.minicloud.api.events.FileEvent;
import cz.zipek.minicloud.api.events.FilesEvent;
import cz.zipek.minicloud.api.events.PathEvent;
import cz.zipek.minicloud.api.events.PathsEvent;
import cz.zipek.minicloud.api.events.ServerInfoEvent;
import cz.zipek.minicloud.api.events.SuccessEvent;
import cz.zipek.minicloud.api.events.UserEvent;
import cz.zipek.minicloud.api.events.UsersEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class manages interaction with minicloud server API
 * 
 * @author Kamen
 */
public class External extends Eventor<Event> {

	/**
	 * Codes used to determine response type.
	 */
	class codes {
		public static final String PATH = "path";
		public static final String PATHS = "paths";
		public static final String FILE = "file";
		public static final String FILES = "files";
		public static final String USER = "user";
		public static final String USERS = "users";
		public static final String SERVER_INFO = "server";
		public static final String ERROR = "error";
		public static final String BOOL = "bool";
	}
	
	///@var events maps response type to class that parses said response
	private final Map<String, Class> events = new HashMap<>();

	///@var server path to server
	private String server = "http://minicloud.zipek.cz";

	///@var auth authorization string used to autorize against API
	private String auth;
	
	///@var long counter used to automatically create action_id
	private long actionCounter;
	
	/**
	 * Used for parametrized thread.
	 */
	class ParamThread extends Thread {

		public final String params;
		public final String auth;

		ParamThread(String params, String auth) {
			this.params = params;
			this.auth = auth;
		}
	}

	/**
	 * Initializes basic event mapping.
	 */
	public External() {
		this.actionCounter = 0;
		
		events.put("", SuccessEvent.class);
		events.put(codes.FILE, FileEvent.class);
		events.put(codes.FILES, FilesEvent.class);
		events.put(codes.ERROR, ErrorEvent.class);
		events.put(codes.PATH, PathEvent.class);
		events.put(codes.PATHS, PathsEvent.class);
		events.put(codes.USER, UserEvent.class);
		events.put(codes.USERS, UsersEvent.class);
		events.put(codes.SERVER_INFO, ServerInfoEvent.class);
		events.put(codes.BOOL, BoolEvent.class);
		
	}

	/**
	 * Initializes basic event mapping and sets server url.
	 * 
	 * @param server serve to use
	 */
	public External(String server) {
		this();
		
		this.setServer(server);
	}

	/**
	 * Returns url to minicloud server, without api suffix.
	 * 
	 * @return server url without api suffix.
	 */
	public String getServer() {
		return server;
	}

	/**
	 * Sets url to minicloud server, without api suffix.
	 * 
	 * @param aServer the url to server without api suffix
	 */
	public final void setServer(String aServer) {
		server = aServer;
	}
	
	/**
	 * Url to server with api suffix.
	 * 
	 * @return url to server with api suffix
	 */
	public String getApiUrl() {
		return getServer() + "/api.php";
	}

	private Thread request(String params) {
		return request(params, getAuth());
	}
	
	private Thread request(String params, String auth) {		
		Thread request = new ParamThread(params, auth) {
			@Override
			public void run() {
				try {
					JSONObject res = loadResponse(this.params, this.auth);
					dispatchResponse(res);
				} catch (JSONException | IOException excalibur) {
					dispatchResponse(null);
					Logger.getLogger(External.class.getName()).log(Level.SEVERE, null, excalibur);
				}
			}
		};
		request.start();
		return request;
	}

	private void dispatchResponse(JSONObject response) {
		
		if (response == null) {
			fireEvent(new ErrorEvent(this, null, null));
			return;
		}
		
		String type = response.optString("type", "");
		String action_id = response.optString("action_id", null);
		JSONObject data = response; //.optJSONObject("data");
		
		Class handler = events.get(type);
		
		if (handler != null) {
					
			//System.out.println("Event type " + handler.getName());
			
			try {
				fireEvent((Event)(
					handler
						.getDeclaredConstructor(External.class, JSONObject.class, String.class)
						.newInstance(this, data, action_id)
				));
			} catch (NoSuchMethodException |
					SecurityException |
					InstantiationException |
					IllegalAccessException |
					IllegalArgumentException |
					InvocationTargetException ex) {
				Logger.getLogger(External.class.getName()).log(Level.SEVERE, null, ex);
				
				//We need to fire something
				fireEvent(new Event(this, data, action_id));
			}
		} else {
			fireEvent(new Event(this, data, action_id));
		}
	}

	private JSONObject loadResponse(String params, String auth) throws IOException, JSONException {
		URL urlsort = new URL(getApiUrl());
		HttpURLConnection conn = (HttpURLConnection) urlsort.openConnection();
		conn.setReadTimeout(10000);
		conn.setConnectTimeout(15000);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("X-Auth", auth);
		//conn.setRequestProperty("Content-Length", "" + Integer.toString(request.getBytes().length));

		//System.out.println(params);
		
		try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
			out.writeBytes(params);
			out.flush();
		} catch (Exception e) {
			Logger.getLogger(External.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}

		String response;
		try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
			response = "";
			String line;
			while ((line = in.readLine()) != null) {
				response += line + "\n";
			}
		} catch (Exception e) {
			try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
				response = "";
				String line;
				while ((line = in.readLine()) != null) {
					response += line + "\n";
				}
			} catch (Exception e2) {
				Logger.getLogger(External.class.getName()).log(Level.SEVERE, null, e2);
				return null;
			}
		}
		
		//System.out.print(response);
		
		return new JSONObject(response);
	}
	
	private String createUrl(String action, Map<String, String> params) {
		try {
			StringBuilder result = new StringBuilder();
			result.append("action=");
			result.append(URLEncoder.encode(action, "UTF-8"));
			
			for(Map.Entry<String, String> item : params.entrySet()) {
				result.append("&");
				result.append(item.getKey());
				result.append("=");
				if (item.getValue() != null) {
					result.append(URLEncoder.encode(item.getValue(), "UTF-8"));
				}
			}
			
			return result.toString();
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(External.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		return "";
	}
	
	/**
	 * Requests server info.
	 * 
	 * @return action id
	 */
	public String getServerInfo() {
		return getServerInfo(false);
	}
	
	/**
	 * Requests server info.
	 * 
	 * @param wait wait for request to finish
	 * @return action id
	 */
	public String getServerInfo(boolean wait) {
		return getServerInfo(wait, Long.toString(this.actionCounter++));
	}
	
	/**
	 * Requests server info.
	 * 
	 * @param wait wait for request to finish
	 * @param action_id user specified action id
	 * @return action id
	 */
	public String getServerInfo(boolean wait, String action_id) {
		Map<String, String> params = new HashMap<>();
		params.put("action_id", action_id);
		
		Thread request = request(createUrl("get_server_info", params));

		if (wait) {
			try {
				request.join();
			} catch (InterruptedException ex) {
				Logger.getLogger(External.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		return action_id;
	}
	
	/**
	 * Requests current user info.
	 * 
	 * @return action id
	 */
	public String getUser() {
		return getUser(false);
	}
	
	/**
	 * Requests current user info.
	 * 
	 * @param wait wait for request to finish
	 * @return action id
	 */
	public String getUser(boolean wait) {
		return getUser(wait, Long.toString(this.actionCounter++));
	}
	
	/**
	 * Requests current user info.
	 * 
	 * @param wait wait for request to finish
	 * @param action_id user specified action id
	 * @return action id
	 */
	public String getUser(boolean wait, String action_id) {
		Map<String, String> params = new HashMap<>();
		params.put("action_id", action_id);
		
		Thread request = request(createUrl("get_user", params));

		if (wait) {
			try {
				request.join();
			} catch (InterruptedException ex) {
				Logger.getLogger(External.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		return action_id;
	}
	
	/**
	 * Returns list of all users on server. This method
	 * is only usable by admins.
	 * 
	 * @return action id if request
	 */
	public String getUsers() {
		return getUsers(Long.toString(this.actionCounter++));
	}
	
	/**
	 * Returns list of all users on server. This method
	 * is only usable by admins.
	 * 
	 * @param action_id ID identify response to this request.
	 * @return action id if request
	 */
	public String getUsers(String action_id) {
		Map<String, String> params = new HashMap<>();
		params.put("action_id", action_id);
		
		request(createUrl("admin_get_users", params));
		
		return action_id;
	}
	
	/**
	 * Updates user values on server. This method can only
	 * be used on logged user.
	 * 
	 * @param user user with updated values
	 * @return action id to identify response
	 * @throws NoSuchProviderException thown when user is unable to hash password
	 */
	public String setUser(User user) throws NoSuchProviderException {
		return setUser(user, Long.toString(this.actionCounter++));
	}
	
	/**
	 * Updates user values on server. This method can only
	 * be used on logged user.
	 * 
	 * @param user user with updated values
	 * @param action_id ID to identify response to this request
	 * @return action id to identify response
	 * @throws NoSuchProviderException thown when user is unable to hash password
	 */
	public String setUser(User user, String action_id) throws NoSuchProviderException {
		Map<String, String> params = new HashMap<>();
		
		params.putAll(user.getUpdate());
		
		params.put("action_id", action_id);
		params.put("id", Integer.toString(user.getId()));
		
		request(createUrl("set_user", params));
		
		return action_id;
	}
	
	/**
	 * Creates new user on server. This method is only
	 * available to admin.
	 * 
	 * @param user data of user to be created
	 * @return action id to identify response
	 */
	public String createUser(User user) {
		return createUser(user, Long.toString(this.actionCounter++));
	}
	
	/**
	 * Creates new user on server. This method is only
	 * available to admin.
	 * 
	 * @param user data of user to be created
	 * @param action_id ID to identify response to this request
	 * @return action id to identify response
	 */
	public String createUser(User user, String action_id) {
		Map<String, String> params = new HashMap<>();
		params.put("action_id", action_id);
		
		params.putAll(user.getUpdate(true));
		
		request(createUrl("admin_create_user", params));

		return action_id;
	}
	
	/**
	 * Updated user data on server. This method allows updating
	 * any user, but is only available for admins.
	 * 
	 * @param user updated user data
	 * @return action id to identify response
	 */
	public String adminSetUser(User user) {
		return adminSetUser(user, Long.toString(this.actionCounter++));
	}
	
	/**
	 * Updated user data on server. This method allows updating
	 * any user, but is only available for admins.
	 * 
	 * @param user updated user data
	 * @param action_id ID to identify response to this request
	 * @return action id to identify response
	 */
	public String adminSetUser(User user, String action_id) {
		Map<String, String> params = new HashMap<>();
		params.put("action_id", action_id);
		
		params.putAll(user.getUpdate(true));
		
		request(createUrl("admin_set_user", params));

		return action_id;
	}
	
	/**
	 * Deletes user from server. This method is only avilable
	 * to admin.
	 * 
	 * @param user user to be deleted
	 * @return action id to identify response
	 */
	public String deleteUser(User user) {
		return deleteUser(user, Long.toString(this.actionCounter++));
	}
	
	/**
	 * Deletes user from server. This method is only avilable
	 * to admin.
	 * 
	 * @param user user to be deleted
	 * @param action_id ID to identify response to this request
	 * @return action id to identify response
	 */
	public String deleteUser(User user, String action_id) {
		Map<String, String> params = new HashMap<>();
		params.put("action_id", action_id);
		params.put("id", Integer.toString(user.getId()));
		
		request(createUrl("admin_delete_user", params));
		
		return action_id;
	}
	
	/**
	 * Requests root path info.
	 * 
	 * @return action id
	 */
	public String getPath() {
		return getPath("");
	}
	
	/**
	 * Requests path info.
	 * 
	 * @param path queried path
	 * @return action id
	 */
	public String getPath(String path) {
		return getPath(path, false);
	}
	
	/**
	 * Requests path info.
	 * 
	 * @param path queried path
	 * @param recursive
	 * @return action id
	 */
	public String getPath(String path, boolean recursive) {
		return getPath(path, recursive, Long.toString(this.actionCounter++));
	}
	
	/**
	 * Requests path info.
	 * 
	 * @param path queried path
	 * @param recursive
	 * @param action_id user specified action id
	 * @return action id
	 */
	public String getPath(String path, boolean recursive, String action_id) {
		Map<String, String> params = new HashMap<>();
		params.put("action_id", action_id);
		
		if (path != null) {
			params.put("path", path);
		}
		
		if (recursive) {
			params.put("recursive", "1");
		}
		
		request(createUrl("get_path", params));

		return action_id;
	}
	
	/**
	 * Requests path info.
	 * 
	 * @param id queried path id
	 * @return action id
	 */
	public String getPath(int id) {
		return getPath(id, false);
	}
	
	/**
	 * Requests path info.
	 * 
	 * @param id queried path id
	 * @param wait wait for request to finish
	 * @return action id
	 */
	public String getPath(int id, boolean wait) {
		return getPath(id, wait, Long.toString(this.actionCounter++));
	}
	
	/**
	 * Requests path info.
	 * 
	 * @param id queried path id
	 * @param wait wait for request to finish
	 * @param action_id user specified action id
	 * @return action id
	 */
	public String getPath(int id, boolean wait, String action_id) {
		Map<String, String> params = new HashMap<>();
		params.put("action_id", action_id);
		params.put("id", Integer.toString(id));

		Thread request = request(createUrl("get_path", params));

		if (wait) {
			try {
				request.join();
			} catch (InterruptedException ex) {
				Logger.getLogger(External.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		return action_id;
	}
	
	/**
	 * Requests list of all paths.
	 * 
	 * @return action id
	 */
	public String getPaths() {
		return getPaths(false);
	}
	/**
	 * Requests list of all paths.
	 * 
	 * @param wait wait for request to finish
	 * @return action id
	 */
	public String getPaths(boolean wait) {
		return getPaths(wait, Long.toString(this.actionCounter++));
	}
	
	/**
	 * Requests list of all paths.
	 * 
	 * @param wait wait for request to finish
	 * @param action_id user specified action id
	 * @return action id
	 */
	public String getPaths(boolean wait, String action_id) {
		Map<String, String> params = new HashMap<>();
		params.put("action_id", action_id);
		
		Thread request = request(createUrl("get_paths", params));

		if (wait) {
			try {
				request.join();
			} catch (InterruptedException ex) {
				Logger.getLogger(External.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		return action_id;
	}
	
	/**
	 * Requests deletion of specified files.
	 * 
	 * @param files files to deleteFiles
	 * @return action id
	 */
	public String deleteFiles(List<File> files) {
		return deleteFiles(files, Long.toString(this.actionCounter++));
	}
	
	/**
	 * Requests deletion of specified files.
	 * 
	 * @param files files to deleteFiles
	 * @param action_id user specified action id
	 * @return action id
	 */
	public String deleteFiles(List<File> files, String action_id) {
		try {
			StringBuilder params = new StringBuilder();
			params.append(String.format(
					"action=delete_files&action_id=%s",
					URLEncoder.encode(action_id, "UTF-8")
			));
			for (File file : files) {
				params.append("&files[]=");
				params.append(file.getId());
			}
			request(params.toString(), getAuth());
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(External.class.getName()).log(Level.SEVERE, null, ex);
		}
		return action_id;
	}
	
	/**
	 * Requests moving of specified file to specified path.
	 * 
	 * @param file file to be moved
	 * @param path target path
	 * @return action id
	 */
	public String moveFile(File file, String path) {
		return moveFile(file, path, Long.toString(actionCounter++));
	}
	
	/**
	 * Requests moving of specified file to specified path.
	 * 
	 * @param file file to be moved
	 * @param path target path
	 * @param action_id user specified action id
	 * @return action id
	 */
	public String moveFile(File file, String path, String action_id) {
		try {
			request(
					String.format(
							"action=set_file&action_id=%s&id=%s&path=%s",
							URLEncoder.encode(action_id, "UTF-8"),
							Integer.toString(file.getId()),
							URLEncoder.encode(path, "UTF-8")
					)
			);
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(External.class.getName()).log(Level.SEVERE, null, ex);
		}
		return action_id;
	}

	/**
	 * Requests applying of local changes to server.
	 * 
	 * @param file changed file
	 * @return action id
	 */
	public String updateFile(File file) {
		return updateFile(file, Long.toString(actionCounter++));
	}
	
	/**
	 * Requests applying of local changes to server.
	 * 
	 * @param file changed file
	 * @param action_id user specified action id
	 * @return action id
	 */
	public String updateFile(File file, String action_id) {
		Map<String, String> params = new HashMap<>();
		params.putAll(file.getUpdate());
		
		params.put("action_id", action_id);
		params.put("id", Integer.toString(file.getId()));

		request(createUrl("set_file", params));
		
		return action_id;
	}
	
		
	/**
	 * Requests moving of specified file to specified path.
	 * 
	 * 
	 * @param path
	 * @param result
	 * @return action id
	 */
	public String movePath(Path path, String result) {
		return movePath(path, result, Long.toString(actionCounter++));
	}
	
	/**
	 * Requests moving of specified file to specified path.
	 * 
	 * @param path
	 * @param result
	 * @param action_id user specified action id
	 * @return action id
	 */
	public String movePath(Path path, String result, String action_id) {
		Map<String, String> params = new HashMap<>();
		params.put("action_id", action_id);
		params.put("id", Integer.toString(path.getId()));
		params.put("path", result + "/" + path.getName());
		params.put("parent", result);
		
		request(createUrl("set_path", params));
		
		return action_id;
	}
	
		
	/**
	 * Requests moving of specified file to specified path.
	 * 
	 * 
	 * @param path
	 * @return action id
	 */
	public String deletePath(Path path) {
		return deletePath(path, Long.toString(actionCounter++));
	}
	
	/**
	 * Requests moving of specified file to specified path.
	 * 
	 * @param path
	 * @param action_id user specified action id
	 * @return action id
	 */
	public String deletePath(Path path, String action_id) {
		Map<String, String> params = new HashMap<>();
		params.put("action_id", action_id);
		params.put("id", Integer.toString(path.getId()));
		
		request(createUrl("delete_path", params));
		
		return action_id;
	}
	
	/**
	 * Requests deletion of specified paths.
	 * 
	 * @param paths paths to delete
	 * @return action id
	 */
	public String deletePaths(List<Path> paths) {
		return deletePaths(paths, Long.toString(this.actionCounter++));
	}
	
	/**
	 * Requests deletion of specified paths.
	 * 
	 * @param paths paths to delete
	 * @param action_id user specified action id
	 * @return action id
	 */
	public String deletePaths(List<Path> paths, String action_id) {
		Map<String, String> params = new HashMap<>();
		params.put("action_id", action_id);
		
		int index = 0;
		for(Path path : paths) {
			params.put("paths[" + Integer.toString(index++) + "]", Integer.toString(path.getId()));
		}
		
		request(createUrl("delete_paths", params));
		
		return action_id;
	}
	
	/**
	 * Sets plain authorization string.
	 * 
	 * @param aAuth 
	 */
	public void setAuth(String aAuth) {
		auth = aAuth;
	}
	
	/**
	 * Builds authorization string from specified parameters.
	 * 
	 * @param login
	 * @param password 
	 */
	public void setAuth(String login, char[] password) {
		
		try {
			setAuth(login + ":" + Tools.sha256(password));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
			Logger.getLogger(External.class.getName()).log(Level.SEVERE, null, ex);
		}
		
	}
	
	/**
	 * Returns authorization string.
	 * 
	 * @return authorization string
	 */
	public String getAuth() {
		return auth;
	}
}
