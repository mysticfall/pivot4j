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
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import com.eyeq.pivot4j.ui.RenderContext;
import com.eyeq.pivot4j.ui.condition.ConditionFactory;

public class PropertySupport implements PropertySource {

	private SortedMap<String, Property> properties = new TreeMap<String, Property>();

	private ConditionFactory conditionFactory;

	/**
	 * @param conditionFactory
	 */
	public PropertySupport(ConditionFactory conditionFactory) {
		if (conditionFactory == null) {
			throw new NullArgumentException("conditionFactory");
		}

		this.conditionFactory = conditionFactory;
	}

	protected ConditionFactory getConditionFactory() {
		return conditionFactory;
	}

	/**
	 * @param name
	 * @see com.eyeq.pivot4j.ui.property.PropertySource#getProperty(java.lang.String)
	 */
	public Property getProperty(String name) {
		if (name == null) {
			throw new NullArgumentException("name");
		}

		return properties.get(name);
	}

	/**
	 * @param property
	 * @see com.eyeq.pivot4j.ui.property.PropertySource#setProperty(com.eyeq.pivot4j.ui.property.Property)
	 */
	public void setProperty(Property property) {
		if (property == null) {
			throw new NullArgumentException("property");
		}

		properties.put(property.getName(), property);
	}

	/**
	 * @param name
	 * @see com.eyeq.pivot4j.ui.property.PropertySource#removeProperty(java.lang.String)
	 */
	public void removeProperty(String name) {
		if (name == null) {
			throw new NullArgumentException("name");
		}

		properties.remove(name);
	}

	/**
	 * @param name
	 * @see com.eyeq.pivot4j.ui.property.PropertySource#hasProperty(java.lang.String)
	 */
	public boolean hasProperty(String name) {
		if (name == null) {
			throw new NullArgumentException("name");
		}

		return properties.containsKey(name);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.PropertySource#clearProperties()
	 */
	public void clearProperties() {
		properties.clear();
	}

	/**
	 * @param name
	 * @param defaultValue
	 * @param context
	 * @return
	 */
	public String getString(String name, String defaultValue,
			RenderContext context) {
		if (name == null) {
			throw new NullArgumentException("name");
		}

		if (context == null) {
			throw new NullArgumentException("context");
		}

		String value = null;

		Property property = properties.get(name);

		if (property != null) {
			value = StringUtils.trimToNull(property.getValue(context));
		}

		if (value == null) {
			value = defaultValue;
		}

		return value;
	}

	/**
	 * @param name
	 * @param defaultValue
	 * @param context
	 * @return
	 */
	public Integer getInteger(String name, Integer defaultValue,
			RenderContext context) {
		Integer intValue = null;

		String value = getString(name, null, context);

		if (value != null) {
			intValue = Integer.parseInt(value);
		}

		if (intValue == null) {
			intValue = defaultValue;
		}

		return intValue;
	}

	/**
	 * @param name
	 * @param defaultValue
	 * @param context
	 * @return
	 */
	public Float getFloat(String name, Float defaultValue, RenderContext context) {
		Float floatValue = null;

		String value = getString(name, null, context);

		if (value != null) {
			floatValue = Float.parseFloat(value);
		}

		if (floatValue == null) {
			floatValue = defaultValue;
		}

		return floatValue;
	}

	/**
	 * @param name
	 * @param defaultValue
	 * @param context
	 * @return
	 */
	public Boolean getBoolean(String name, Boolean defaultValue,
			RenderContext context) {
		Boolean booleanValue = null;

		String value = getString(name, null, context);

		if (value != null) {
			booleanValue = Boolean.parseBoolean(value);
		}

		if (booleanValue == null) {
			booleanValue = defaultValue;
		}

		return booleanValue;
	}

	/**
	 * @param name
	 * @param defaultValue
	 * @param context
	 * @return
	 */
	public Double getDouble(String name, Double defaultValue,
			RenderContext context) {
		Double doubleValue = null;

		String value = getString(name, null, context);

		if (value != null) {
			doubleValue = Double.parseDouble(value);
		}

		if (doubleValue == null) {
			doubleValue = defaultValue;
		}

		return doubleValue;
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#saveState()
	 */
	@Override
	public Serializable saveState() {
		Collection<Property> properties = this.properties.values();

		Serializable[] states = new Serializable[properties.size()];

		int index = 0;
		for (Property property : properties) {
			boolean conditional = property instanceof ConditionalProperty;

			states[index++] = new Serializable[] { conditional,
					property.saveState() };
		}

		return states;
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		if (state == null) {
			throw new NullArgumentException("state");
		}

		Serializable[] states = (Serializable[]) state;

		this.properties.clear();

		for (Serializable st : states) {
			Serializable[] pair = (Serializable[]) st;

			boolean conditional = (Boolean) pair[0];

			Property property;

			// TODO Need more robust method to determine property types.
			if (conditional) {
				property = new ConditionalProperty(conditionFactory);
			} else {
				property = new SimpleProperty();
			}

			property.restoreState(pair[1]);

			this.properties.put(property.getName(), property);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		int index = 0;

		for (Property property : properties.values()) {
			String name = String.format("property(%s)", index++);

			configuration.setProperty(name, "");

			SubnodeConfiguration propertyConfig = configuration
					.configurationAt(name);
			property.saveSettings(propertyConfig);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		this.properties.clear();

		try {
			List<HierarchicalConfiguration> configurations = configuration
					.configurationsAt("property");

			for (HierarchicalConfiguration propertyConfig : configurations) {
				boolean conditional = propertyConfig.containsKey("conditions");

				Property property;

				// TODO Need more robust method to determine property types.
				if (conditional) {
					property = new ConditionalProperty(conditionFactory);
				} else {
					property = new SimpleProperty();
				}

				property.restoreSettings(propertyConfig);

				this.properties.put(property.getName(), property);
			}
		} catch (IllegalArgumentException e) {
		}
	}
}
