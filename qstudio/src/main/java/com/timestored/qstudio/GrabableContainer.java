package com.timestored.qstudio;

import java.awt.EventQueue;


/** Allows taking one panel from this container for display elsewhere. */
public interface GrabableContainer {

	/**
	 * MUST be called from the {@link EventQueue} thread.
	 * @return item that was grabbed from this container if possible else null.
	 */
	public GrabItem grab();
	
}
