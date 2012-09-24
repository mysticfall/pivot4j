/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.transform.impl;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.query.QueryAdapter;
import com.eyeq.pivot4j.transform.Transform;
import com.eyeq.pivot4j.transform.TransformFactory;

public class TransformFactoryImpl implements TransformFactory {

	/**
	 * @see com.eyeq.pivot4j.transform.TransformFactory#getTransform(java.lang.Class,
	 *      com.eyeq.pivot4j.PivotModel, com.eyeq.pivot4j.query.QueryAdapter)
	 */
	public <T extends Transform> T getTransform(Class<T> type,
			PivotModel model, QueryAdapter queryAdapter) {
		// TODO Auto-generated method stub
		return null;
	}

}
