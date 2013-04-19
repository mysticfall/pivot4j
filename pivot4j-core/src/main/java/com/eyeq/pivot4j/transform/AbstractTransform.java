/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.transform;

import org.apache.commons.lang.NullArgumentException;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.query.QueryAdapter;

public abstract class AbstractTransform implements Transform {

	private QueryAdapter queryAdapter;

	/**
	 * @param queryAdapter
	 */
	public AbstractTransform(QueryAdapter queryAdapter) {
		if (queryAdapter == null) {
			throw new NullArgumentException("queryAdapter");
		}

		this.queryAdapter = queryAdapter;
	}

	/**
	 * @return the model
	 * @see com.eyeq.pivot4j.transform.Transform#getModel()
	 */
	public PivotModel getModel() {
		return getQueryAdapter().getModel();
	}

	/**
	 * @return the queryAdapter
	 */
	protected QueryAdapter getQueryAdapter() {
		return queryAdapter;
	}
}
