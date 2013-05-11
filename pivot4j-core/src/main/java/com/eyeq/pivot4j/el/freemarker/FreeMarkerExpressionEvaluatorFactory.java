/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.el.freemarker;

import com.eyeq.pivot4j.el.ExpressionEvaluator;
import com.eyeq.pivot4j.el.ExpressionEvaluatorFactory;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;

public class FreeMarkerExpressionEvaluatorFactory implements
		ExpressionEvaluatorFactory {

	private Configuration configuration;

	public FreeMarkerExpressionEvaluatorFactory() {
		this.configuration = createConfiguration();
	}

	/**
	 * @return configuration
	 */
	protected Configuration createConfiguration() {
		Configuration config = new Configuration();
		config.setObjectWrapper(new BeansWrapper());

		return config;
	}

	/**
	 * @return configuration
	 */
	protected Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * @see com.eyeq.pivot4j.el.ExpressionEvaluatorFactory#createEvaluator()
	 */
	@Override
	public ExpressionEvaluator createEvaluator() {
		return new FreeMarkerExpressionEvaluator(configuration);
	}
}