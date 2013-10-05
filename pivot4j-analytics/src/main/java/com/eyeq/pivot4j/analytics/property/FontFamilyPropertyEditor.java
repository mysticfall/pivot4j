package com.eyeq.pivot4j.analytics.property;

import java.util.Arrays;
import java.util.List;

import javax.faces.component.UISelectItem;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.eyeq.pivot4j.ui.property.RenderPropertyList;
import com.eyeq.pivot4j.ui.property.SimpleRenderProperty;

public class FontFamilyPropertyEditor extends SelectStringPropertyEditor {

	public FontFamilyPropertyEditor() {
		super(Arrays.asList(new UISelectItem[] { createItem(""),
				createItem("serif"), createItem("sans-serif"),
				createItem("monospace"), createItem("cursive"),
				createItem("fantasy") }));
	}

	/**
	 * @param fontName
	 * @return
	 */
	private static UISelectItem createItem(String fontName) {
		UISelectItem item = new UISelectItem();
		item.setItemLabel(fontName);
		item.setItemValue(fontName);

		return item;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.property.AbstractPropertyEditor#getValue(com.eyeq.pivot4j.ui.property.SimpleRenderProperty)
	 */
	@Override
	protected Object getValue(SimpleRenderProperty property) {
		String stringValue = StringUtils.trimToNull(property.getValue());

		List<UISelectItem> items = getItems();

		boolean matches = false;

		if (stringValue != null && items != null) {
			for (UISelectItem item : items) {
				if (stringValue.equals(item.getItemValue())) {
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
	 * @see com.eyeq.pivot4j.analytics.property.AbstractPropertyEditor#setValue(com.eyeq.pivot4j.analytics.property.PropertyDescriptor,
	 *      com.eyeq.pivot4j.ui.property.RenderPropertyList, java.lang.Object)
	 */
	@Override
	public void setValue(PropertyDescriptor descriptor,
			RenderPropertyList properties, Object value) {
		super.setValue(descriptor, properties,
				StringUtils.trimToNull(ObjectUtils.toString(value)));
	}
}
