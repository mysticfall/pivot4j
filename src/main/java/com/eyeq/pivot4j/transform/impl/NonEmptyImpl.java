/*
 * ====================================================================
 * This software is subject to the terms of the Common Publilc License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.transform.impl;

import com.eyeq.pivot4j.query.QueryAdapter;
import com.eyeq.pivot4j.transform.AbstractTransform;
import com.eyeq.pivot4j.transform.NonEmpty;

/**
 * Implementation of the NonEmpty transform
 */
public class NonEmptyImpl extends AbstractTransform implements NonEmpty {

	/**
	 * @param queryAdapter
	 */
	public NonEmptyImpl(QueryAdapter queryAdapter) {
		super(queryAdapter);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.NonEmpty#isNonEmpty()
	 */
	public boolean isNonEmpty() {
		return getQueryAdapter().isNonEmpty();
	}

	/**
	 * @see com.tonbeller.jpivot.olap.navi.NonEmpty#setNonEmpty(boolean)
	 */
	public void setNonEmpty(boolean nonEmpty) {
		getQueryAdapter().setNonEmpty(nonEmpty);
	}
}
