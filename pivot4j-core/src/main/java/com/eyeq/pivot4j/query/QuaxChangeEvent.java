/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.query;

import java.util.EventObject;

/**
 * Informs a listener that the quax has changed.
 */
public class QuaxChangeEvent extends EventObject {

	private static final long serialVersionUID = -2509598338622928944L;

	private boolean changedByNavigator;

	/**
	 * Constructor for QuaxChangeEvent.
	 * 
	 * @param source
	 * @param changedByNavigator
	 */
	public QuaxChangeEvent(Quax source, boolean changedByNavigator) {
		super(source);
		this.changedByNavigator = changedByNavigator;
	}

	public Quax getQuax() {
		return (Quax) getSource();
	}

	/**
	 * @return the changedByNavigator
	 */
	public boolean isChangedByNavigator() {
		return changedByNavigator;
	}
}
