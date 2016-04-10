/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zipek.minicloud.sync.events;

import cz.zipek.minicloud.api.Event;
import cz.zipek.minicloud.sync.SyncEvent;

/**
 *
 * @author Kamen
 */
public class SyncExternalEvent extends SyncEvent {
	private final Event event;

	public SyncExternalEvent(Event event) {
		this.event = event;
	}

	/**
	 * @return the event
	 */
	public Event getEvent() {
		return event;
	}
}
