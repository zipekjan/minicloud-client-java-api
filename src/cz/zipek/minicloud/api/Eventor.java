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
import java.util.List;

/**
 * Event emittor.
 * 
 * @author Jan Zípek
 * @param <E> expected event class
 */
public class Eventor<E> {
	
	/**
	 * List of listeners assigned to this event emittor.
	 */
	protected final List<Listener> listeners = new ArrayList<>();

	/**
	 * List of listeners that will be lazy removed. This prevents collisions
	 * with multiple threads.
	 */
	protected final List<Listener> toRemove = new ArrayList<>();

	/**
	 * List of listeners that will be lazy added. This prevents collisions
	 * with multiple threads.
	 */
	protected final List<Listener> toAdd = new ArrayList<>();
	
	/**
	 * Adds new listener.
	 * @param listener
	 */
	public synchronized void addListener(Listener<E> listener) {
		listeners.add(listener);
	}
	
	/**
	 * Adds listener when possible. This prevents collisions
	 * with multiple threads.
	 * @param listener
	 */
	public synchronized void addListenerLater(Listener<E> listener) {
		toAdd.add(listener);
	}

	/**
	 * Removes listener. Don't call this method from handler,
	 * it will result in thread collision. Use removeListenerLater.
	 * 
	 * @param listener 
	 */
	public synchronized void removeListener(Listener<E> listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Removes listener when possible. This method solves collision
	 * problems when calling from event listener or different thread.
	 * 
	 * @param listener 
	 */
	public void removeListenerLater(Listener<E> listener) {
		toRemove.add(listener);
	}
	
	/**
	 * Emitts event to all listeners.
	 * 
	 * @param event event to be emitted
	 */
	protected synchronized void fireEvent(E event) {
		if (toRemove.size() > 0) {
			for(Listener l : toRemove) {
				listeners.remove(l);
			}
			toRemove.clear();
		}
		
		if (toAdd.size() > 0) {
			for(Listener l : toAdd) {
				listeners.add(l);
			}
			toAdd.clear();
		}

		for(Listener listener : listeners) {
			listener.handleEvent(event, this);
		}
		
		if (toRemove.size() > 0) {
			for(Listener l : toRemove) {
				listeners.remove(l);
			}
			toRemove.clear();
		}
		
		if (toAdd.size() > 0) {
			for(Listener l : toAdd) {
				listeners.add(l);
			}
			toAdd.clear();
		}
	}
}
