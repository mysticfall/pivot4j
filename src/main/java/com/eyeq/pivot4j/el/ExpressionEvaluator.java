/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.el;

import java.util.Map;

public interface ExpressionEvaluator {

	String getNamespace();

	/**
	 * @param expression
	 * @param context
	 * @return
	 * @throws EvaluationFailedException
	 */
	Object evaluate(String expression, Map<String, Object> context)
			throws EvaluationFailedException;
}
