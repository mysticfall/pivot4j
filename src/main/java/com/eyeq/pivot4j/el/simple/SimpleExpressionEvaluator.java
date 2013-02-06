/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.el.simple;

import java.util.Map;

import com.eyeq.pivot4j.el.AbstractExpressionEvaluator;

public class SimpleExpressionEvaluator extends AbstractExpressionEvaluator {

	public static final String NAMESPACE = "s";

	/**
	 * @see com.eyeq.pivot4j.el.ExpressionEvaluator#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	/**
	 * @see com.eyeq.pivot4j.el.AbstractExpressionEvaluator#doEvaluate(java.lang.
	 *      String, java.util.Map)
	 */
	@Override
	protected Object doEvaluate(String expression, Map<String, Object> context)
			throws Exception {
		return context.get(expression);
	}
}
