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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractExpressionEvaluator implements
		ExpressionEvaluator {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * @see com.eyeq.pivot4j.el.ExpressionEvaluator#evaluate(java.lang.String,
	 *      java.util.Map)
	 */
	@Override
	public Object evaluate(String expression, Map<String, Object> context)
			throws EvaluationFailedException {
		if (logger.isDebugEnabled()) {
			logger.debug("Evaluating expression : ");
			logger.debug("	- namespace : " + getNamespace());
			logger.debug("	- expression : " + expression);
		}

		Object result;

		try {
			result = doEvaluate(expression, context);
		} catch (EvaluationFailedException e) {
			throw e;
		} catch (Exception e) {
			throw new EvaluationFailedException(
					"Failed to evaluate the expression : " + e, getNamespace(),
					expression, e);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("	- result : " + result);
		}

		return result;
	}

	/**
	 * @param expression
	 * @param context
	 * @return
	 * @throws EvaluationFailedException
	 */
	protected abstract Object doEvaluate(String expression,
			Map<String, Object> context) throws Exception;
}
