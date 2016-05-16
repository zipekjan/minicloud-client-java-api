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

import org.json.JSONObject;

/**
 * Base for all API event classes.
 * 
 * @author Jan Zípek <jan at zipek.cz>
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
