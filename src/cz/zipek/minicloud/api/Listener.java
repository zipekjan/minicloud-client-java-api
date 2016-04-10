package cz.zipek.minicloud.api;

/**
 * Basic listener class listening for API events
 * 
 * @author Kamen
 * @param <E>
 */
public interface Listener<E> {
	public void handleEvent(E event, Object sender);
}
