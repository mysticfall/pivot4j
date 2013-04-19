/*
 * ====================================================================
 * This software is subject to the terms of the Common Publilc License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.transform;


/**
 * Suppresses the display of empty rows/columns on an axis.
 */
public interface NonEmpty extends Transform {

	/**
	 * @return true if non-empty rows are currently suppressed
	 */
	boolean isNonEmpty();

	/**
	 * Change the visability of non-empty rows
	 */
	void setNonEmpty(boolean nonEmpty);
}
