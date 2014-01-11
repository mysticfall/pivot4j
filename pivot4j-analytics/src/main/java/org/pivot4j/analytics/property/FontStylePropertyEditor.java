package org.pivot4j.analytics.property;

import java.util.Arrays;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItem;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.pivot4j.ui.property.RenderPropertyList;
import org.pivot4j.ui.property.SimpleRenderProperty;
import org.primefaces.component.selectonemenu.SelectOneMenu;

public class FontStylePropertyEditor extends AbstractPropertyInputEditor {

	private Collection<String> styles = Arrays.asList("", "normal", "bold",
			"italic", "bolditalic");

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

		ResourceBundle resources = application
				.getResourceBundle(context, "msg");

		for (String style : styles) {
			select.getChildren().add(createItem(style, resources));
		}

		return select;
	}

	/**
	 * @param styleName
	 * @param resources
	 * @return
	 */
	private UISelectItem createItem(String styleName, ResourceBundle resources) {
		UISelectItem item = new UISelectItem();

		if (StringUtils.isEmpty(styleName)) {
			item.setItemLabel("");
		} else {
			String key = "properties.fontStyle.option." + styleName;
			item.setItemLabel(resources.getString(key));
		}

		item.setItemValue(styleName);

		return item;
	}

	/**
	 * @see org.pivot4j.analytics.property.AbstractPropertyEditor#getValue(org.pivot4j.ui.property.SimpleRenderProperty)
	 */
	@Override
	protected Object getValue(SimpleRenderProperty property) {
		String stringValue = StringUtils.trimToNull(property.getValue());

		boolean matches = false;

		if (stringValue != null) {
			for (String style : styles) {
				if (stringValue.equals(style)) {
					matches = true;
					break;
				}
			}
		}

		if (!matches) {
			stringValue = null;
		}

		return stringValue;
	}

	/**
	 * @see org.pivot4j.analytics.property.AbstractPropertyEditor#setValue(org.pivot4j.analytics.property.PropertyDescriptor,
	 *      org.pivot4j.ui.property.RenderPropertyList, java.lang.Object)
	 */
	@Override
	public void setValue(PropertyDescriptor descriptor,
			RenderPropertyList properties, Object value) {
		super.setValue(descriptor, properties,
				StringUtils.trimToNull(ObjectUtils.toString(value)));
	}
}
