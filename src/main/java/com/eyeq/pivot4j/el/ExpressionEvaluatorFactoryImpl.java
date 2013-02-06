/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.el;

import java.util.HashMap;
import java.util.Map;

import com.eyeq.pivot4j.el.freemarker.FreeMarkerExpressionEvaluator;
import com.eyeq.pivot4j.el.simple.SimpleExpressionEvaluator;

public class ExpressionEvaluatorFactoryImpl implements
		ExpressionEvaluatorFactory {

	private Map<String, ExpressionEvaluator> evaluators;

	public ExpressionEvaluatorFactoryImpl() {
		this(null);
	}

	/**
	 * @param evaluators
	 */
	public ExpressionEvaluatorFactoryImpl(
			Map<String, ExpressionEvaluator> evaluators) {
		if (evaluators == null) {
			this.evaluators = new HashMap<String, ExpressionEvaluator>();

			registerDefaultEvaluators();
		} else {
			this.evaluators = evaluators;
		}
	}

	protected void registerDefaultEvaluators() {
		this.evaluators.put(SimpleExpressionEvaluator.NAMESPACE,
				new SimpleExpressionEvaluator());
		this.evaluators.put(FreeMarkerExpressionEvaluator.NAMESPACE,
				new FreeMarkerExpressionEvaluator());
	}

	/**
	 * @param namespace
	 * @param evaluator
	 */
	public void registerEvaluator(String namespace,
			ExpressionEvaluator evaluator) {
		if (evaluator == null) {
			unregisterEvaluator(namespace);
		} else {
			evaluators.put(namespace, evaluator);
		}
	}

	/**
	 * @param namespace
	 */
	public void unregisterEvaluator(String namespace) {
		evaluators.remove(namespace);
	}

	/**
	 * @see com.eyeq.pivot4j.el.ExpressionEvaluatorFactory#getEvaluator(java.lang.String)
	 */
	@Override
	public ExpressionEvaluator getEvaluator(String namespace) {
		return evaluators.get(namespace);
	}
}
