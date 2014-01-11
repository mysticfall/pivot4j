package org.pivot4j.analytics.state;

import java.util.EventListener;

public interface ViewStateListener extends EventListener {

	void viewRegistered(ViewStateEvent e);

	void viewUnregistered(ViewStateEvent e);
}
