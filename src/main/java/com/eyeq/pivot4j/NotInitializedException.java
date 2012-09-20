/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j;

public class NotInitializedException extends PivotException {

	private static final long serialVersionUID = 397853501108728229L;

	/**
	 * Constructor for PivotException.
	 */
	public NotInitializedException() {
	}

	/**
	 * Constructor for PivotException.
	 * 
	 * @param msg
	 */
	public NotInitializedException(String msg) {
		super(msg);
	}
}
