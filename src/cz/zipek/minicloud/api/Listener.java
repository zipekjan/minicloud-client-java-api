package cz.zipek.minicloud.api;

/**
 * Basic listener class listening for API events
 * 
 * @author Kamen
 * @param <E>
 */
public interface Listener<E> {
	/**
	 * Handle emitted event.
	 * <p><b>Note:</b> do not call removeListener/addListsner from this method,
	 * use removeListenerLater/addListsnerLater.</p>
	 * 
	 * @param event emitted event
	 * @param sender instance that emitted this event
	 */
	public void handleEvent(E event, Object sender);
}
