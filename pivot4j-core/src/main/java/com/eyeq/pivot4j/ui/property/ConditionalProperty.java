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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;

import com.eyeq.pivot4j.el.ExpressionEvaluator;
import com.eyeq.pivot4j.ui.RenderContext;
import com.eyeq.pivot4j.ui.condition.Condition;
import com.eyeq.pivot4j.ui.condition.ConditionFactory;

public class ConditionalProperty extends AbstractProperty {

	private String defaultValue;

	private ConditionFactory conditionFactory;

	private List<ConditionalValue> values;

	/**
	 * @param conditionFactory
	 */
	ConditionalProperty(ConditionFactory conditionFactory) {
		if (conditionFactory == null) {
			throw new NullArgumentException("conditionFactory");
		}

		this.conditionFactory = conditionFactory;
	}

	/**
	 * @param name
	 * @param defaultValue
	 * @param values
	 * @param conditionFactory
	 */
	public ConditionalProperty(String name, String defaultValue,
			List<ConditionalValue> values, ConditionFactory conditionFactory) {
		super(name);

		if (conditionFactory == null) {
			throw new NullArgumentException("conditionFactory");
		}

		this.defaultValue = defaultValue;
		this.values = values;
		this.conditionFactory = conditionFactory;
	}

	/**
	 * @return the conditionFactory
	 */
	protected ConditionFactory getConditionFactory() {
		return conditionFactory;
	}

	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue
	 *            the defaultValue to set
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the values
	 */
	public List<ConditionalValue> getValues() {
		return values;
	}

	/**
	 * @param values
	 *            the values to set
	 */
	public void setValues(List<ConditionalValue> values) {
		this.values = values;
	}

	@Override
	public String getValue(RenderContext context) {
		if (values == null) {
			return null;
		}

		String value = null;

		for (ConditionalValue conditionValue : values) {
			if (conditionValue.getCondition().matches(context)) {
				value = conditionValue.getValue();
			}
		}

		if (value == null) {
			value = defaultValue;
		}

		if (value != null) {
			ExpressionEvaluator evaluator = context.getExpressionEvaluator();
			value = ObjectUtils.toString(evaluator.evaluate(value,
					context.getExpressionContext()));
		}

		return value;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.AbstractProperty#saveState()
	 */
	@Override
	public Serializable saveState() {
		Serializable[] states = null;

		if (values != null) {
			states = new Serializable[values.size()];

			int index = 0;
			for (ConditionalValue value : values) {
				Condition condition = value.getCondition();

				states[index++] = new Serializable[] { condition.getName(),
						condition.saveState(), value.getValue() };
			}
		}

		return new Serializable[] { super.saveState(), defaultValue, states };
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.AbstractProperty#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		Serializable[] states = (Serializable[]) state;

		super.restoreState(states[0]);

		this.defaultValue = (String) states[1];

		Serializable[] conditionStates = (Serializable[]) states[2];

		if (conditionStates == null) {
			this.values = null;
		} else {
			this.values = new LinkedList<ConditionalValue>();

			for (Serializable conditionState : conditionStates) {
				Serializable[] stateValues = (Serializable[]) conditionState;

				Condition condition = conditionFactory
						.createCondition((String) stateValues[0]);
				condition.restoreState(stateValues[1]);

				values.add(new ConditionalValue(condition,
						(String) stateValues[2]));
			}
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.AbstractProperty#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		super.saveSettings(configuration);

		if (defaultValue != null) {
			configuration.setProperty("default", defaultValue);
		}

		if (values != null) {
			int index = 0;

			configuration.setProperty("conditions", "");

			SubnodeConfiguration configurations = configuration
					.configurationAt("conditions");

			for (ConditionalValue value : values) {
				String name = String.format("condition-property(%s)", index++);

				configurations.setProperty(name, "");

				SubnodeConfiguration conditionConfig = configurations
						.configurationAt(name);

				value.getCondition().saveSettings(conditionConfig);
				conditionConfig.setProperty("value", value.getValue());
			}
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.AbstractProperty#restoreSettings(org.apache
	 *      .commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		super.restoreSettings(configuration);

		this.defaultValue = configuration.getString("default");
		this.values = new LinkedList<ConditionalValue>();

		try {
			List<HierarchicalConfiguration> conditionConfigs = configuration
					.configurationsAt("conditions.condition-property");

			for (HierarchicalConfiguration conditionConfig : conditionConfigs) {
				String name = conditionConfig.getString("condition[@name]");

				Condition condition = conditionFactory.createCondition(name);
				condition.restoreSettings(conditionConfig);

				String value = conditionConfig.getString("value");

				values.add(new ConditionalValue(condition, value));
			}
		} catch (IllegalArgumentException e) {
		}
	}
}
