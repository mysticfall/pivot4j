/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.transform.impl;

import com.eyeq.pivot4j.query.QueryAdapter;
import com.eyeq.pivot4j.transform.Transform;
import com.eyeq.pivot4j.transform.TransformFactory;

public class TransformFactoryImpl implements TransformFactory {

	/**
	 * @see com.eyeq.pivot4j.transform.TransformFactory#createTransform(java.lang.Class,
	 *      com.eyeq.pivot4j.query.QueryAdapter)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Transform> T createTransform(Class<T> type,
			QueryAdapter queryAdapter) {
		// TODO Improve lookup method
		if (type.isAssignableFrom(DrillExpandMemberImpl.class)) {
			return (T) new DrillExpandMemberImpl(queryAdapter);
		} else if (type.isAssignableFrom(DrillExpandPositionImpl.class)) {
			return (T) new DrillExpandPositionImpl(queryAdapter);
		} else if (type.isAssignableFrom(ChangeSlicerImpl.class)) {
			return (T) new ChangeSlicerImpl(queryAdapter);
		} else if (type.isAssignableFrom(DrillReplaceImpl.class)) {
			return (T) new DrillReplaceImpl(queryAdapter);
		} else if (type.isAssignableFrom(NonEmptyImpl.class)) {
			return (T) new NonEmptyImpl(queryAdapter);
		} else if (type.isAssignableFrom(SwapAxesImpl.class)) {
			return (T) new SwapAxesImpl(queryAdapter);
		}

		return null;
	}
}
