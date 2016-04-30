package cz.zipek.minicloud.api;

import org.json.JSONObject;

/**
 * Base for all API event classes.
 * 
 * @author Kamen
 */
public class Event {

	/**
	 * API instance that sent this event.
	 */
	protected final External sender;
	
	/**
	 * Raw data of event.
	 */
	protected final JSONObject data;

	private String actionId = null;

	/**
	 * Creates new event.
	 * 
	 * @param sender API instance that sent this event.
	 * @param data Raw event data.
	 * @param action_id Event action id used to identify responses to requests.
	 */
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
