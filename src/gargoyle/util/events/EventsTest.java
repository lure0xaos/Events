package gargoyle.util.events;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EventsTest {
	public static void main(final String[] args) {
		new EventsTest().test();
	}

	private void test() {
		final ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				System.out.println(e);
			}
		};
		Events.listen(ActionEvent.class, listener);
		//
		final ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "command");
		Events.fire(event, "actionPerformed");// ActionListener.actionPerformed(ActionEvent)
		//
		Events.forget(ActionEvent.class);
	}
}
