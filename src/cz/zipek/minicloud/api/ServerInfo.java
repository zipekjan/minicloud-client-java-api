package cz.zipek.minicloud.api;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Contains informations about minicloud server.
 * 
 * @author Kamen
 */
public class ServerInfo {
	
	private final External source;
	
	private final String name;
	private final String description;
	private final String logo;
	
	private boolean niceUrl;
	
	public ServerInfo(External from, JSONObject info) throws JSONException {
		source = from;
		name = info.optString("name", "");
		description = info.optString("description", "");
		niceUrl = info.optBoolean("nice_url", false);
		logo = info.optString("logo", null);
	}

	/**
	 * @return server name
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
	 * @return server logo url
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
	 * @return the source
	 */
	public External getSource() {
		return source;
	}
	
}
