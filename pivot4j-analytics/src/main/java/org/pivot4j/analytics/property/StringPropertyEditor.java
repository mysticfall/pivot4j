package org.pivot4j.analytics.property;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

import org.primefaces.component.inputtext.InputText;

public class StringPropertyEditor extends AbstractPropertyInputEditor {

	private Integer size;

	public StringPropertyEditor() {
	}

	/**
	 * @param size
	 */
	public StringPropertyEditor(Integer size) {
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

		InputText input = (InputText) application
				.createComponent(InputText.COMPONENT_TYPE);

		if (size != null) {
			input.setSize(size);
		}

		return input;
	}

	/**
	 * @return the size
	 */
	public Integer getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(Integer size) {
		this.size = size;
	}
}
