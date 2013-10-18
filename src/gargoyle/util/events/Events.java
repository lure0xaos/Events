package gargoyle.util.events;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for event-driven programming
 * 
 * @author gargoyle
 * 
 */
public class Events {
	public static class ExceptionEvent extends EventObject {
		private static final long serialVersionUID = 1L;
		private final Throwable exception;
		private String sourceClass;
		private String sourceMethod;

		public ExceptionEvent(final EventListener source, final Throwable exception, final String sourceClass,
				final String sourceMethod) {
			super(source);
			this.exception = exception;
		}

		public Throwable getException() {
			return this.exception;
		}

		public String getSourceClass() {
			return this.sourceClass;
		}

		public String getSourceMethod() {
			return this.sourceMethod;
		}
	}

	public static interface ExceptionListener extends EventListener {
		public abstract void exceptionThrown(ExceptionEvent event);
	}

	private static final Map<Class<? extends EventObject>, List<? extends EventListener>> listeners;
	static {
		listeners = new HashMap<Class<? extends EventObject>, List<? extends EventListener>>();
		Events.listen(ExceptionEvent.class, new ExceptionListener() {
			@Override
			public void exceptionThrown(final ExceptionEvent event) {
				Logger.getLogger(Events.class.getName()).logp(Level.SEVERE, event.getSourceClass(),
						event.getSourceMethod(), event.getException().getMessage(), event.getException());
			}
		});
	}

	private static void exception(final InvocationTargetException e, final EventListener source,
			final String sourceClass, final String sourceMethod) {
		Events.fire(new ExceptionEvent(source, e.getTargetException(), sourceClass, sourceMethod), "exceptionThrown");
	}

	private static void exception(final Throwable e, final EventListener source, final String sourceClass,
			final String sourceMethod) {
		Events.fire(new ExceptionEvent(source, e, sourceClass, sourceMethod), "exceptionThrown");
	}

	/**
	 * fire event (notify all listeners)
	 * 
	 * @param event
	 *            - the instance of event
	 * @param method
	 *            - method name to be invoked on all listeners, must accept
	 *            exactly 1 argument of event object
	 * 
	 *            Example:
	 * 
	 *            <pre>
	 * Events.listen(ActionEvent.class, new ActionListener() {
	 * 	&#064;Override
	 * 	public void actionPerformed(final ActionEvent e) {
	 * 		System.out.println(e);
	 * 	}
	 * });
	 * Events.fire(new ActionEvent(new Object(), 0, &quot;&quot;), &quot;actionPerformed&quot;);
	 * Events.forget(ActionEvent.class);
	 * </pre>
	 */
	public static synchronized void fire(final EventObject event, final String method) {
		final Class<? extends EventObject> eventClass = event.getClass();
		if (Events.listeners.containsKey(eventClass)) {
			for (final EventListener listener : Events.listeners.get(eventClass)) {
				try {
					listener.getClass().getMethod(method, eventClass).invoke(listener, event);
				} catch (final IllegalAccessException e) {
					Events.exception(e, listener, listener.getClass().getName(), method);
					throw new RuntimeException(String.format("%s.%s(%s) %s", listener.getClass().getName(), method,
							eventClass.getName(), " is not accessible"), e);
				} catch (final IllegalArgumentException e) {
					Events.exception(e, listener, listener.getClass().getName(), method);
					throw new RuntimeException(String.format("%s.%s(%s) %s", listener.getClass().getName(), method,
							eventClass.getName(), " wrong event"), e);
				} catch (final InvocationTargetException e) {
					Events.exception(e, listener, listener.getClass().getName(), method);
				} catch (final NoSuchMethodException e) {
					Events.exception(e, listener, listener.getClass().getName(), method);
					throw new RuntimeException(String.format("%s.%s(%s) %s", listener.getClass().getName(), method,
							eventClass.getName(), " no method"), e);
				} catch (final SecurityException e) {
					Events.exception(e, listener, listener.getClass().getName(), method);
					throw new RuntimeException(String.format("%s.%s(%s) %s", listener.getClass().getName(), method,
							eventClass.getName(), " is not accessible"), e);
				} catch (final Throwable e) {
					Events.exception(e, listener, listener.getClass().getName(), method);
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * remove all event listeners, subscribed to this event type
	 * 
	 * @param event
	 *            - exact class, not instance
	 */
	public static synchronized <E extends EventObject> void forget(final Class<? extends EventObject> event) {
		if (Events.listeners.containsKey(event)) {
			Events.listeners.get(event).clear();
			Events.listeners.remove(event);
		}
	}

	/**
	 * remove event listener from listener list
	 * 
	 * @param event
	 *            - event type
	 * @param listener
	 */
	public static synchronized void forget(final Class<? extends EventObject> event, final EventListener listener) {
		if (Events.listeners.containsKey(event)) {
			Events.listeners.get(event).remove(listener);
		}
	}

	/**
	 * add listener to listener list
	 * 
	 * @param event
	 *            -event type
	 * @param listener
	 *            <p>
	 *            * generally event listener's methods should not throw
	 *            exceptions
	 */
	public static synchronized void listen(final Class<? extends EventObject> event, final EventListener listener) {
		if (!Events.listeners.containsKey(event)) {
			Events.listeners.put(event, new ArrayList<EventListener>());
		}
		@SuppressWarnings("unchecked")
		final List<EventListener> list = (List<EventListener>) Events.listeners.get(event);
		if (!list.contains(listener)) {
			list.add(listener);
		}
	}
}
