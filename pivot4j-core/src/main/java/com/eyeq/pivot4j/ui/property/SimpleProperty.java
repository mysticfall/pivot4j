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
import org.apache.commons.lang.ObjectUtils;

import com.eyeq.pivot4j.el.ExpressionEvaluator;
import com.eyeq.pivot4j.ui.RenderContext;

public class SimpleProperty extends AbstractProperty {

	private String value;

	SimpleProperty() {
	}

	/**
	 * @param name
	 * @param value
	 */
	public SimpleProperty(String name, String value) {
		super(name);

		this.value = value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.Property#getValue(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public String getValue(RenderContext context) {
		if (value == null) {
			return null;
		}

		ExpressionEvaluator evaluator = context.getExpressionEvaluator();

		return ObjectUtils.toString(evaluator.evaluate(value,
				context.getExpressionContext()));
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.AbstractProperty#saveState()
	 */
	@Override
	public Serializable saveState() {
		return new Serializable[] { super.saveState(), value };
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.AbstractProperty#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		Serializable[] states = (Serializable[]) state;

		super.restoreState(states[0]);

		this.value = (String) states[1];
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.AbstractProperty#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		super.saveSettings(configuration);

		if (value != null) {
			configuration.setProperty("value", value);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.AbstractProperty#restoreSettings(org.apache
	 *      .commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		super.restoreSettings(configuration);

		this.value = configuration.getString("value");
	}
}
