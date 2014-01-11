/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.mdx;

public abstract class AbstractExp implements Exp {

	private static final long serialVersionUID = -3736674439789473278L;

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toMdx();
	}
}
