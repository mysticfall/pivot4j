package org.pivot4j.analytics.property;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.pivot4j.analytics.component.AjaxColorPicker;
import org.pivot4j.ui.property.RenderPropertyList;
import org.pivot4j.ui.property.SimpleRenderProperty;

public class ColorPropertyEditor extends AbstractPropertyInputEditor {

	/**
	 * @see org.pivot4j.analytics.property.AbstractPropertyInputEditor#createInput
	 *      (org.pivot4j.analytics.property.PropertyDescriptor,
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
	 * @see org.pivot4j.analytics.property.AbstractPropertyEditor#getValue(org.pivot4j.ui.property.SimpleRenderProperty)
	 */
	@Override
	protected Object getValue(SimpleRenderProperty property) {
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
	 * @see org.pivot4j.analytics.property.AbstractPropertyEditor#setValue(org.pivot4j.analytics.property.PropertyDescriptor,
	 *      org.pivot4j.ui.property.RenderPropertyList, java.lang.Object)
	 */
	@Override
	public void setValue(PropertyDescriptor descriptor,
			RenderPropertyList properties, Object value) {
		String stringValue = StringUtils.trimToNull((String) value);

		if (stringValue != null && stringValue.matches("[a-fA-F0-9]+")) {
			stringValue = "#" + stringValue;
		}

		super.setValue(descriptor, properties, stringValue);
	}
}
