package com.eyeq.pivot4j.analytics.property;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.eyeq.pivot4j.ui.property.PropertySource;
import com.eyeq.pivot4j.ui.property.SimpleProperty;

public class FontSizePropertyEditor extends IntegerPropertyEditor {

	public FontSizePropertyEditor() {
		super(1, null, 6);
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.property.AbstractPropertyInputEditor#createComponent(com.eyeq.pivot4j.analytics.property.PropertyDescriptor,
	 *      javax.faces.component.UIComponent, javax.el.ValueExpression,
	 *      javax.el.MethodExpression, java.lang.String)
	 */
	@Override
	public void createComponent(PropertyDescriptor descriptor,
			UIComponent parent, ValueExpression expression,
			MethodExpression listener, String update) {
		super.createComponent(descriptor, parent, expression, listener, update);

		HtmlOutputText unitText = new HtmlOutputText();
		unitText.setStyleClass("unit-text");
		unitText.setValue("(pt)");

		parent.getChildren().add(unitText);
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.property.IntegerPropertyEditor#getValue(com.eyeq.pivot4j.ui.property.SimpleProperty)
	 */
	@Override
	protected Object getValue(SimpleProperty property) {
		String stringValue = StringUtils.trimToNull(property.getValue());

		Object value = null;

		if (stringValue != null && stringValue.matches("[0-9]+pt")) {
			value = Integer.parseInt(stringValue.substring(0,
					stringValue.length() - 2));
		} else {
			value = null;
		}

		return value;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.property.AbstractPropertyEditor#setValue(com.eyeq.pivot4j.analytics.property.PropertyDescriptor,
	 *      com.eyeq.pivot4j.ui.property.PropertySource, java.lang.Object)
	 */
	@Override
	public void setValue(PropertyDescriptor descriptor,
			PropertySource properties, Object value) {
		String fontSize = StringUtils.trimToNull(ObjectUtils.toString(value));

		if (fontSize != null) {
			fontSize += "pt";
		}

		super.setValue(descriptor, properties, fontSize);
	}
}
