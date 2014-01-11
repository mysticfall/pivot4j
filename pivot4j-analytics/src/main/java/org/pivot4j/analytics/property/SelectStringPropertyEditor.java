package org.pivot4j.analytics.property;

import java.util.List;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItem;
import javax.faces.context.FacesContext;

import org.primefaces.component.selectonemenu.SelectOneMenu;

public class SelectStringPropertyEditor extends AbstractPropertyInputEditor {

	private List<UISelectItem> items;

	public SelectStringPropertyEditor() {
	}

	/**
	 * @param items
	 */
	public SelectStringPropertyEditor(List<UISelectItem> items) {
		this.items = items;
	}

	/**
	 * @return the items
	 */
	public List<UISelectItem> getItems() {
		return items;
	}

	/**
	 * @param items
	 *            the items to set
	 */
	public void setItems(List<UISelectItem> items) {
		this.items = items;
	}

	/**
	 * @see org.pivot4j.analytics.property.AbstractPropertyInputEditor#createInput(org.pivot4j.analytics.property.PropertyDescriptor,
	 *      javax.faces.component.UIComponent, javax.faces.context.FacesContext)
	 */
	@Override
	protected UIInput createInput(PropertyDescriptor descriptor,
			UIComponent parent, FacesContext context) {
		Application application = FacesContext.getCurrentInstance()
				.getApplication();
		SelectOneMenu select = (SelectOneMenu) application
				.createComponent(SelectOneMenu.COMPONENT_TYPE);

		if (items != null) {
			for (UISelectItem item : items) {
				select.getChildren().add(item);
			}
		}

		return select;
	}
}
