package com.eyeq.pivot4j.analytics.property;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;

import com.eyeq.pivot4j.analytics.component.AjaxColorPicker;
import com.eyeq.pivot4j.ui.property.PropertySource;
import com.eyeq.pivot4j.ui.property.SimpleProperty;

public class ColorPropertyEditor extends AbstractPropertyInputEditor {

	/**
	 * @see com.eyeq.pivot4j.analytics.property.AbstractPropertyInputEditor#createInput
	 *      (com.eyeq.pivot4j.analytics.property.PropertyDescriptor,
	 *      javax.faces.component.UIComponent, javax.faces.context.FacesContext)
	 */
	@Override
	protected UIInput createInput(PropertyDescriptor descriptor,
			UIComponent parent, FacesContext context) {
		Application application = FacesContext.getCurrentInstance()
				.getApplication();

		return (UIInput) application
				.createComponent(AjaxColorPicker.COMPONENT_TYPE);
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.property.AbstractPropertyEditor#getValue(com.eyeq.pivot4j.ui.property.SimpleProperty)
	 */
	@Override
	protected Object getValue(SimpleProperty property) {
		String value = StringUtils
				.trimToNull((String) super.getValue(property));

		if (value != null && value.matches("#[a-fA-F0-9]+")) {
			value = value.substring(1);
		} else {
			value = null;
		}

		return value;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.property.AbstractPropertyEditor#setValue(com
	 *      .eyeq.pivot4j.analytics.property.PropertyDescriptor,
	 *      com.eyeq.pivot4j.ui.property.PropertySource, java.lang.Object)
	 */
	@Override
	public void setValue(PropertyDescriptor descriptor,
			PropertySource properties, Object value) {
		String stringValue = StringUtils.trimToNull((String) value);

		if (stringValue != null && stringValue.matches("[a-fA-F0-9]+")) {
			stringValue = "#" + stringValue;
		}

		super.setValue(descriptor, properties, stringValue);
	}
}
