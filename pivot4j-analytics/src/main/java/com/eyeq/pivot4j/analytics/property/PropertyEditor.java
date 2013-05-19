package com.eyeq.pivot4j.analytics.property;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;

import com.eyeq.pivot4j.ui.property.PropertySource;

public interface PropertyEditor {

	/**
	 * @param descriptor
	 * @param parent
	 * @param expression
	 * @param listener
	 * @param update
	 */
	void createComponent(PropertyDescriptor descriptor, UIComponent parent,
			ValueExpression expression, MethodExpression listener, String update);

	/**
	 * @param descriptor
	 * @param properties
	 * @return
	 */
	Object getValue(PropertyDescriptor descriptor, PropertySource properties);

	/**
	 * @param descriptor
	 * @param properties
	 * @param value
	 */
	void setValue(PropertyDescriptor descriptor, PropertySource properties,
			Object value);
}
