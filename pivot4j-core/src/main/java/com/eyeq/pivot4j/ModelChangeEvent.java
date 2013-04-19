/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j;

import java.util.EventObject;

/**
 * Informs a listener that the model has changed.
 */
public class ModelChangeEvent extends EventObject {

	private static final long serialVersionUID = 8035651445908017817L;

	/**
	 * Constructor for ModelChangeEvent.
	 * 
	 * @param source
	 */
	public ModelChangeEvent(PivotModel source) {
		super(source);
	}

	public PivotModel getModel() {
		return (PivotModel) getSource();
	}
}
