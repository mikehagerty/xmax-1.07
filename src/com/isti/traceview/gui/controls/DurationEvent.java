package com.isti.traceview.gui.controls;

import java.util.EventObject;

public class DurationEvent extends EventObject {

	// **********************************************************************
	// Private Members
	// **********************************************************************

	private Long duration;

	// **********************************************************************
	// Constructors
	// **********************************************************************

	/**
	 * Create a duration event.
	 * 
	 * @param source
	 *            The object on which the event occurred.
	 * @param duration
	 *            The duration.
	 */

	public DurationEvent(Object source, Long duration) {
		super(source);
		this.duration = duration;
	}

	// **********************************************************************
	// Public
	// **********************************************************************
	public Long getDuration() {
		return duration;
	}

	// The default equals and hashCode methods are acceptable.

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return super.toString() + ",duration=" + duration;
	}
}

