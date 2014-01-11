package org.pivot4j.analytics.property;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.commons.lang.NullArgumentException;

@ManagedBean(name = "propertyDescriptorFactory")
@ApplicationScoped
public class PropertyDescriptorFactory {

	private Map<String, PropertyDescriptor> cellProperties;

	private Map<String, PropertyDescriptor> headerProperties;

	@PostConstruct
	protected void initialize() {
		this.cellProperties = new HashMap<String, PropertyDescriptor>();
		this.headerProperties = new HashMap<String, PropertyDescriptor>();

		populateProperties(PropertyCategory.Cell);
		populateProperties(PropertyCategory.Header);
	}

	/**
	 * @param category
	 */
	protected void populateProperties(PropertyCategory category) {
		Map<String, PropertyDescriptor> properties = getDescriptors(category);

		properties.put("label", new PropertyDescriptor(category, "label",
				"ui-icon-info", new StringPropertyEditor()));
		properties.put("link", new PropertyDescriptor(category, "link",
				"ui-icon-link", new StringPropertyEditor(40)));
		properties.put("styleClass", new PropertyDescriptor(category,
				"styleClass", "ui-icon-tag", new StringPropertyEditor(30)));
		properties.put("fgColor", new PropertyDescriptor(category, "fgColor",
				"ui-icon-image", new ColorPropertyEditor()));
		properties.put("bgColor", new PropertyDescriptor(category, "bgColor",
				"ui-icon-image", new ColorPropertyEditor()));
		properties
				.put("fontFamily", new PropertyDescriptor(category,
						"fontFamily", "ui-icon-pencil",
						new FontFamilyPropertyEditor()));
		properties.put("fontSize", new PropertyDescriptor(category, "fontSize",
				"ui-icon-pencil", new FontSizePropertyEditor()));
		properties.put("fontStyle",
				new PropertyDescriptor(category, "fontStyle", "ui-icon-pencil",
						new FontStylePropertyEditor()));
	}

	/**
	 * @param category
	 * @param key
	 * @return
	 */
	public PropertyDescriptor getDescriptor(PropertyCategory category,
			String key) {
		if (key == null) {
			throw new NullArgumentException("key");
		}

		Map<String, PropertyDescriptor> properties = getDescriptors(category);

		return properties.get(key);
	}

	/**
	 * @param category
	 * @return
	 */
	protected Map<String, PropertyDescriptor> getDescriptors(
			PropertyCategory category) {
		if (category == null) {
			throw new NullArgumentException("category");
		}

		Map<String, PropertyDescriptor> properties = null;

		switch (category) {
		case Cell:
			properties = cellProperties;
			break;
		case Header:
			properties = headerProperties;
			break;
		default:
			assert false;
		}

		return properties;
	}
}
