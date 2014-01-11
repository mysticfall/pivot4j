/*
 * ====================================================================
 * This software is subject to the terms of the Common Publilc License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.transform.impl;

import org.olap4j.OlapConnection;
import org.pivot4j.query.QueryAdapter;
import org.pivot4j.transform.AbstractTransform;
import org.pivot4j.transform.NonEmpty;

/**
 * Implementation of the NonEmpty transform
 */
public class NonEmptyImpl extends AbstractTransform implements NonEmpty {

	/**
	 * @param queryAdapter
	 * @param connection
	 */
	public NonEmptyImpl(QueryAdapter queryAdapter, OlapConnection connection) {
		super(queryAdapter, connection);
	}

	/**
	 * @see org.pivot4j.transform.NonEmpty#isNonEmpty()
	 */
	public boolean isNonEmpty() {
		return getQueryAdapter().isNonEmpty();
	}

	/**
	 * @see org.pivot4j.transform.NonEmpty#setNonEmpty(boolean)
	 */
	public void setNonEmpty(boolean nonEmpty) {
		getQueryAdapter().setNonEmpty(nonEmpty);
	}
}
