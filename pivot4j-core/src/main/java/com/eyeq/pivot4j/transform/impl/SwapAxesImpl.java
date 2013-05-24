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
import com.eyeq.pivot4j.transform.SwapAxes;

/**
 * Implementation of the SwapAxesf transform
 */
public class SwapAxesImpl extends AbstractTransform implements SwapAxes {

	/**
	 * @param queryAdapter
	 */
	public SwapAxesImpl(QueryAdapter queryAdapter) {
		super(queryAdapter);
	}

	/**
	 * @see com.eyeq.pivot4j.transform.SwapAxes#canSwapAxes()
	 */
	public boolean canSwapAxes() {
		return getQueryAdapter().getAxes().size() > 1;
	}

	/**
	 * @see com.eyeq.pivot4j.transform.SwapAxes#isSwapAxes()
	 */
	public boolean isSwapAxes() {
		return getQueryAdapter().isAxesSwapped();
	}

	/**
	 * @see com.eyeq.pivot4j.transform.SwapAxes#setSwapAxes(boolean)
	 */
	public void setSwapAxes(boolean swap) {
		getQueryAdapter().setAxesSwapped(swap);
	}
}
