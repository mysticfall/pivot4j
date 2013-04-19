/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.transform;

import com.eyeq.pivot4j.query.QueryAdapter;

public interface TransformFactory {

	<T extends Transform> T createTransform(Class<T> type,
			QueryAdapter queryAdapter);
}
