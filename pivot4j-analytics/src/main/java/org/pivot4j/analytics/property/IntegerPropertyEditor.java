package org.pivot4j.analytics.property;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.pivot4j.ui.property.SimpleRenderProperty;
import org.primefaces.component.spinner.Spinner;

public class IntegerPropertyEditor extends AbstractPropertyInputEditor {

	private Integer minimumValue;

	private Integer maximumValue;

	private Integer size;

	public IntegerPropertyEditor() {

	}

	/**
	 * @param minimumValue
	 * @param maximumValue
	 * @param size
	 */
	public IntegerPropertyEditor(Integer minimumValue, Integer maximumValue,
			Integer size) {
		this.minimumValue = minimumValue;
		this.maximumValue = maximumValue;
		this.size = size;
	}

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
		Spinner spinner = (Spinner) application
				.createComponent(Spinner.COMPONENT_TYPE);

		if (minimumValue != null) {
			spinner.setMin(minimumValue);
		}

		if (maximumValue != null) {
			spinner.setMax(maximumValue);
		}

		if (size != null) {
			spinner.setSize(size);
		}

		return spinner;
	}

	/**
	 * @see org.pivot4j.analytics.property.AbstractPropertyEditor#getValue(org.pivot4j.ui.property.SimpleRenderProperty)
	 */
	@Override
	protected Object getValue(SimpleRenderProperty property) {
		String stringValue = StringUtils.trimToNull((String) super
				.getValue(property));

		Object value = null;

		if (NumberUtils.isNumber(stringValue)) {
			value = Integer.parseInt(stringValue);
		} else {
			value = null;
		}

		return value;
	}

	/**
	 * @return the minimumValue
	 */
	public Integer getMinimumValue() {
		return minimumValue;
	}

	/**
	 * @param minimumValue
	 *            the minimumValue to set
	 */
	public void setMinimumValue(Integer minimumValue) {
		this.minimumValue = minimumValue;
	}

	/**
	 * @return the maximumValue
	 */
	public Integer getMaximumValue() {
		return maximumValue;
	}

	/**
	 * @param maximumValue
	 *            the maximumValue to set
	 */
	public void setMaximumValue(Integer maximumValue) {
		this.maximumValue = maximumValue;
	}

	/**
	 * @return the size
	 */
	public Integer getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(Integer size) {
		this.size = size;
	}
}
