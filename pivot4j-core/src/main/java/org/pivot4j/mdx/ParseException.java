/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.mdx;

import org.pivot4j.PivotException;

public class ParseException extends PivotException {

	private static final long serialVersionUID = -2185820671708051064L;

	public ParseException() {
	}

	/**
	 * @param msg
	 * @param cause
	 */
	public ParseException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * @param msg
	 */
	public ParseException(String msg) {
		super(msg);
	}

	/**
	 * @param cause
	 */
	public ParseException(Throwable cause) {
		super(cause);
	}
}
