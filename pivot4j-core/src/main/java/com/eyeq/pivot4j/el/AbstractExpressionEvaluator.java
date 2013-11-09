/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.el;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractExpressionEvaluator implements
		ExpressionEvaluator {

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * @see com.eyeq.pivot4j.el.ExpressionEvaluator#evaluate(java.lang.String,
	 *      com.eyeq.pivot4j.el.ExpressionContext)
	 */
	@Override
	public Object evaluate(String expression, ExpressionContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Evaluating expression : ");
			logger.debug("	- expression : {}", expression);
		}

		Object result;

		try {
			result = doEvaluate(expression, context);
		} catch (EvaluationFailedException e) {
			throw e;
		} catch (Exception e) {
			throw new EvaluationFailedException(
					"Failed to evaluate the expression : " + e, expression, e);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("	- result : {}", result);
		}

		return result;
	}

	/**
	 * @return the logger
	 */
	protected Logger getLogger() {
		return logger;
	}

	/**
	 * @param expression
	 * @param context
	 * @return
	 * @throws EvaluationFailedException
	 */
	protected abstract Object doEvaluate(String expression,
			ExpressionContext context) throws Exception;
}
