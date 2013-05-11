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
import org.apache.commons.lang.ObjectUtils;
import org.olap4j.Axis;

import com.eyeq.pivot4j.ui.RenderContext;

public class AxisCondition extends AbstractCondition {

	public static final String NAME = "axis";

	private Axis axis;

	/**
	 * @param conditionFactory
	 */
	public AxisCondition(ConditionFactory conditionFactory) {
		super(conditionFactory);
	}

	/**
	 * @param conditionFactory
	 * @param axis
	 */
	public AxisCondition(ConditionFactory conditionFactory, Axis axis) {
		super(conditionFactory);

		this.axis = axis;
	}

	/**
	 * @see com.eyeq.kona.equation.AbstractCondition#getName()
	 */
	public String getName() {
		return NAME;
	}

	/**
	 * @return the axis
	 */
	public Axis getAxis() {
		return axis;
	}

	/**
	 * @param axis
	 *            the axis to set
	 */
	public void setAxis(Axis axis) {
		this.axis = axis;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.condition.Condition#matches(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public boolean matches(RenderContext context) {
		if (axis == null) {
			throw new IllegalStateException("Axis is not specified.");
		}

		return ObjectUtils.equals(axis, context.getAxis());
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#saveState()
	 */
	@Override
	public Serializable saveState() {
		if (axis == null) {
			return null;
		}

		return axis.name();
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		if (state == null) {
			this.axis = null;
		} else {
			this.axis = Axis.Standard.valueOf((String) state);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.condition.AbstractCondition#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		super.saveSettings(configuration);

		if (axis == null) {
			return;
		}

		configuration.addProperty("axis", axis.name());
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		String name = configuration.getString("axis");

		if (name == null) {
			this.axis = null;
		} else {
			this.axis = Axis.Standard.valueOf(name);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.condition.AbstractCondition#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("axis = '");

		if (axis != null) {
			builder.append(axis.name());
		}

		builder.append("'");

		return builder.toString();
	}
}
