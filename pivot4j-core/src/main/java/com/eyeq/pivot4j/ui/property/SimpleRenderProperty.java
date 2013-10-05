/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.property;

import java.io.Serializable;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;

import com.eyeq.pivot4j.el.ExpressionEvaluator;
import com.eyeq.pivot4j.ui.RenderContext;

public class SimpleRenderProperty extends AbstractRenderProperty {

	private String value;

	SimpleRenderProperty() {
	}

	/**
	 * @param name
	 */
	public SimpleRenderProperty(String name) {
		super(name);
	}

	/**
	 * @param name
	 * @param value
	 */
	public SimpleRenderProperty(String name, String value) {
		super(name);

		this.value = value;
	}

	/**
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.RenderProperty#getValue(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public Object getValue(RenderContext context) {
		if (context == null) {
			throw new NullArgumentException("context");
		}

		if (value == null) {
			return null;
		}

		ExpressionEvaluator evaluator = context.getExpressionEvaluator();

		return ObjectUtils.toString(evaluator.evaluate(value,
				context.getExpressionContext()));
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.AbstractRenderProperty#saveState()
	 */
	@Override
	public Serializable saveState() {
		return new Serializable[] { super.saveState(), value };
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.AbstractRenderProperty#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		Serializable[] states = (Serializable[]) state;

		super.restoreState(states[0]);

		this.value = (String) states[1];
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.AbstractRenderProperty#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		super.saveSettings(configuration);

		if (value != null) {
			configuration.setProperty("value", value);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.AbstractRenderProperty#restoreSettings(org.apache
	 *      .commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		super.restoreSettings(configuration);

		this.value = configuration.getString("value");
	}
}
