/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.el.freemarker;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

import com.eyeq.pivot4j.el.AbstractExpressionEvaluator;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class FreeMarkerExpressionEvaluator extends AbstractExpressionEvaluator {

	public static final String NAMESPACE = "fm";

	private Configuration configuration;

	public FreeMarkerExpressionEvaluator() {
		this.configuration = createConfiguration();
	}

	/**
	 * @see com.eyeq.pivot4j.el.ExpressionEvaluator#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	/**
	 * @return configuration
	 */
	protected Configuration createConfiguration() {
		Configuration configuration = new Configuration();
		configuration.setObjectWrapper(new BeansWrapper());

		return configuration;
	}

	/**
	 * @return configuration
	 */
	protected Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * @param expression
	 * @param configuration
	 * @return
	 * @throws IOException
	 */
	protected Template createTemplate(String expression,
			Configuration configuration) throws IOException {
		return new Template(expression, new StringReader(expression),
				configuration);
	}

	/**
	 * @see com.eyeq.pivot4j.el.AbstractExpressionEvaluator#doEvaluate(java.lang.
	 *      String, java.util.Map)
	 */
	@Override
	protected Object doEvaluate(String expression, Map<String, Object> context)
			throws Exception {
		Template template = createTemplate(expression, getConfiguration());

		Locale locale = (Locale) context.get("locale");
		if (locale != null) {
			template.setLocale(locale);
		}

		StringWriter writer = new StringWriter();

		template.process(context, writer);
		writer.flush();

		return writer.toString();
	}
}
