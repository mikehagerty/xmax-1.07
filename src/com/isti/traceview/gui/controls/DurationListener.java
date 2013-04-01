package com.isti.traceview.gui.controls;

import java.util.EventListener;

public interface DurationListener extends EventListener {

	// **********************************************************************
	// Public Constants
	// **********************************************************************

	// **********************************************************************
	// Public
	// **********************************************************************

	/**
	 * This method is called each time a duration changes.
	 * 
	 * @param e
	 *            The duration event information.
	 */
	public void durationChanged(DurationEvent e);
}