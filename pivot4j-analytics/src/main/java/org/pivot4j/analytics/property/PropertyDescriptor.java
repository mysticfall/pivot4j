package org.pivot4j.analytics.property;

import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import org.apache.commons.lang.NullArgumentException;

public class PropertyDescriptor {

	private String key;

	private PropertyCategory category;

	private String icon;

	private PropertyEditor editor;

	/**
	 * @param category
	 * @param key
	 * @param icon
	 * @param editor
	 */
	public PropertyDescriptor(PropertyCategory category, String key,
			String icon, PropertyEditor editor) {
		if (category == null) {
			throw new NullArgumentException("category");
		}

		if (key == null) {
			throw new NullArgumentException("key");
		}

		if (editor == null) {
			editor = new StringPropertyEditor();
		}

		this.category = category;
		this.key = key;
		this.icon = icon;
		this.editor = editor;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return the category
	 */
	public PropertyCategory getCategory() {
		return category;
	}

	/**
	 * @return the editor
	 */
	public PropertyEditor getEditor() {
		return editor;
	}

	/**
	 * @return the icon
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * @param icon
	 *            the icon to set
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}

	/**
	 * @param context
	 * @return
	 */
	public String getName(FacesContext context) {
		if (context == null) {
			throw new NullArgumentException("context");
		}

		ResourceBundle bundle = context.getApplication().getResourceBundle(
				context, "msg");
		return bundle.getString("properties." + key);
	}

	/**
	 * @param context
	 * @return
	 */
	public String getDescription(FacesContext context) {
		if (context == null) {
			throw new NullArgumentException("context");
		}

		ResourceBundle bundle = context.getApplication().getResourceBundle(
				context, "msg");
		return bundle.getString("properties." + key + ".description");
	}
}
