package com.eyeq.pivot4j.analytics.property;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.eyeq.pivot4j.ui.property.Property;
import com.eyeq.pivot4j.ui.property.PropertySource;
import com.eyeq.pivot4j.ui.property.SimpleProperty;

public abstract class AbstractPropertyEditor implements PropertyEditor {

	/**
	 * @see com.eyeq.pivot4j.analytics.property.PropertyEditor#getValue(com.eyeq.pivot4j.analytics.property.PropertyDescriptor,
	 *      com.eyeq.pivot4j.ui.property.PropertySource)
	 */
	@Override
	public Object getValue(PropertyDescriptor descriptor,
			PropertySource properties) {
		if (descriptor == null) {
			throw new NullArgumentException("descriptor");
		}

		if (properties == null) {
			throw new NullArgumentException("properties");
		}

		Property property = properties.getProperty(descriptor.getKey());

		if (property == null) {
			return null;
		}

		Object value = null;

		if (property instanceof SimpleProperty) {
			value = getValue((SimpleProperty) property);
		}

		return value;
	}

	/**
	 * @param property
	 * @return
	 */
	protected Object getValue(SimpleProperty property) {
		return property.getValue();
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.property.PropertyEditor#setValue(com.eyeq.pivot4j.analytics.property.PropertyDescriptor,
	 *      com.eyeq.pivot4j.ui.property.PropertySource, java.lang.Object)
	 */
	@Override
	public void setValue(PropertyDescriptor descriptor,
			PropertySource properties, Object value) {
		if (descriptor == null) {
			throw new NullArgumentException("descriptor");
		}

		if (properties == null) {
			throw new NullArgumentException("properties");
		}

		String stringValue = StringUtils
				.trimToNull(ObjectUtils.toString(value));

		if (stringValue == null) {
			properties.removeProperty(descriptor.getKey());
		} else {
			properties.setProperty(new SimpleProperty(descriptor.getKey(),
					stringValue));
		}
	}
}
