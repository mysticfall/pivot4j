/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.condition;

import java.io.Serializable;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.eyeq.pivot4j.ui.RenderContext;

public class NotCondition extends AbstractCondition {

	public static final String NAME = "NOT";

	private Condition subCondition;

	/**
	 * @param conditionFactory
	 */
	public NotCondition(ConditionFactory conditionFactory) {
		super(conditionFactory);
	}

	/**
	 * @param conditionFactory
	 * @param subCondition
	 */
	public NotCondition(ConditionFactory conditionFactory,
			Condition subCondition) {
		super(conditionFactory);

		this.subCondition = subCondition;
	}

	/**
	 * @see com.eyeq.kona.equation.AbstractCondition#getName()
	 */
	public String getName() {
		return NAME;
	}

	/**
	 * @return the subCondition
	 */
	public Condition getSubCondition() {
		return subCondition;
	}

	/**
	 * @param subCondition
	 *            the subCondition to set
	 */
	public void setSubCondition(Condition subCondition) {
		this.subCondition = subCondition;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.condition.Condition#matches(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public boolean matches(RenderContext context) {
		if (subCondition == null) {
			throw new IllegalStateException("Sub condition was not specified.");
		}

		return !subCondition.matches(context);
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#saveState()
	 */
	@Override
	public Serializable saveState() {
		if (subCondition == null) {
			return null;
		}

		return new Object[] { subCondition.getName(), subCondition.saveState() };
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		Serializable[] states = (Serializable[]) state;

		if (states == null) {
			this.subCondition = null;
		} else {
			this.subCondition = getConditionFactory().createCondition(
					(String) states[0]);
			this.subCondition.restoreState(states[1]);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		configuration.addProperty("[@name]", getName());

		if (subCondition == null) {
			return;
		}

		subCondition.saveSettings(configuration.configurationAt("condition"));
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		HierarchicalConfiguration subConfig = configuration
				.configurationAt("condition");

		String name = subConfig.getString("[@name]");

		if (name == null) {
			this.subCondition = null;
		} else {
			this.subCondition = getConditionFactory().createCondition(name);
			this.subCondition.restoreSettings(subConfig);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.condition.AbstractCondition#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("!");

		if (subCondition == null) {
			builder.append("[MISSING]");
		} else {
			builder.append(subCondition.toString());
		}

		return builder.toString();
	}
}
