package cz.zipek.minicloud.api;

import org.json.JSONObject;

/**
 * Base for all API event classes.
 * 
 * @author Kamen
 */
public class Event {
	protected final External sender;
	protected final JSONObject data;

	private String actionId = null;

	public Event(External sender, JSONObject data, String action_id) {
		this.sender = sender;
		this.data = data;
		this.actionId = action_id;
	}

	/**
	 * Basic event data
	 * 
	 * @return the data
	 */
	public JSONObject getData() {
		return data;
	}

	/**
	 * API which fired this event.
	 * 
	 * @return the sender
	 */
	public External getSender() {
		return sender;
	}

	/**
	 * @return the actionId
	 */
	public String getActionId() {
		return actionId;
	}
}
