package com.eyeq.pivot4j.analytics.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;

import org.primefaces.component.menuitem.MenuItem;
import org.primefaces.component.submenu.Submenu;
import org.primefaces.extensions.event.CompleteEvent;
import org.primefaces.model.DefaultMenuModel;
import org.primefaces.model.MenuModel;

import com.eyeq.pivot4j.analytics.property.PropertyCategory;
import com.eyeq.pivot4j.analytics.property.PropertyDescriptor;
import com.eyeq.pivot4j.analytics.property.PropertyDescriptorFactory;
import com.eyeq.pivot4j.analytics.property.PropertyEditor;
import com.eyeq.pivot4j.ui.PivotRenderer;
import com.eyeq.pivot4j.ui.property.Property;
import com.eyeq.pivot4j.ui.property.PropertySource;
import com.eyeq.pivot4j.ui.property.SimpleProperty;

@ManagedBean(name = "propertiesHandler")
@RequestScoped
public class PropertiesHandler {

	@ManagedProperty(value = "#{propertyDescriptorFactory}")
	private PropertyDescriptorFactory descriptorFactory;

	@ManagedProperty(value = "#{pivotGridHandler.renderer}")
	private PivotRenderer renderer;

	private ResourceBundle bundle;

	private PropertyDescriptor descriptor;

	private MenuModel menu;

	private UIComponent editorPanel;

	private Object value;

	private String expression;

	@PostConstruct
	protected void initialize() {
		FacesContext context = FacesContext.getCurrentInstance();

		this.bundle = context.getApplication()
				.getResourceBundle(context, "msg");

		String key = getKey();
		PropertyCategory category = getCategory();

		if (key != null && category != null) {
			this.descriptor = descriptorFactory.getDescriptor(category, key);
		}
	}

	/**
	 * @return bundle
	 */
	protected ResourceBundle getBundle() {
		return bundle;
	}

	public String getName() {
		if (descriptor == null) {
			return null;
		}

		FacesContext context = FacesContext.getCurrentInstance();
		return descriptor.getName(context);
	}

	public String getDescription() {
		if (descriptor == null) {
			return null;
		}

		FacesContext context = FacesContext.getCurrentInstance();
		return descriptor.getDescription(context);
	}

	public String getCategoryName() {
		if (descriptor == null) {
			return null;
		}

		return bundle.getString("properties.category."
				+ descriptor.getCategory().name());
	}

	public void selectProperty() {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, String> parameters = context.getExternalContext()
				.getRequestParameterMap();

		PropertyCategory category = PropertyCategory.valueOf(parameters
				.get("category"));
		String key = parameters.get("key");

		setKey(key);
		setCategory(category);

		this.descriptor = descriptorFactory.getDescriptor(category, key);

		PropertySource properties = getProperties(descriptor.getCategory());

		SimpleProperty property = (SimpleProperty) properties
				.getProperty(descriptor.getKey());

		if (property == null) {
			this.expression = null;
		} else {
			this.expression = property.getValue();
		}

		if (expression != null
				&& (expression.contains("${") || expression.contains("<#"))) {
			setUseExpression(true);
		} else {
			setUseExpression(false);
		}

		editorPanel.getChildren().clear();

		PropertyEditor editor = descriptor.getEditor();

		if (editor == null) {
			this.value = property.getValue();
		} else {
			Application application = context.getApplication();

			ExpressionFactory factory = application.getExpressionFactory();

			ValueExpression expression = factory.createValueExpression(
					context.getELContext(), "#{propertiesHandler.value}",
					Object.class);

			MethodExpression listener = factory.createMethodExpression(
					context.getELContext(),
					"#{propertiesHandler.onPropertyChange}", Void.TYPE,
					new Class[0]);

			editor.createComponent(descriptor, editorPanel, expression,
					listener, "button-bar");

			this.value = editor.getValue(descriptor, properties);
		}

		setDirty(false);
	}

	public void onPropertyChange() {
		setDirty(true);
	}

	public void onEditorModeChange() {
		PropertySource properties = getProperties(getCategory());
		SimpleProperty property = (SimpleProperty) properties
				.getProperty(getKey());

		if (getUseExpression()) {
			if (expression == null) {
				if (property == null) {
					this.expression = null;
				} else {
					this.expression = property.getValue();
				}
			}
		} else {
			PropertyEditor editor = descriptor.getEditor();

			if (editor == null) {
				this.value = null;
			} else {
				this.value = editor.getValue(descriptor, properties);
			}
		}
	}

	/**
	 * @param category
	 * @return
	 */
	protected PropertySource getProperties(PropertyCategory category) {
		PropertySource properties = null;

		switch (category) {
		case Header:
			properties = renderer.getHeaderProperties();
			break;
		case Cell:
			properties = renderer.getCellProperties();
			break;
		default:
			assert false;
		}

		return properties;
	}

	/**
	 * @param event
	 * @return
	 */
	public List<String> complete(CompleteEvent event) {
		List<String> suggestions = new LinkedList<String>();

		suggestions.add("context: " + event.getContext());
		suggestions.add("token: " + event.getToken());

		return suggestions;
	}

	public void apply() {
		PropertySource properties = getProperties(descriptor.getCategory());

		if (getUseExpression()) {
			if (expression == null) {
				properties.removeProperty(descriptor.getKey());
			} else {
				SimpleProperty property = new SimpleProperty(
						descriptor.getKey(), expression);
				properties.setProperty(property);
			}
		} else {
			PropertyEditor editor = descriptor.getEditor();
			editor.setValue(descriptor, properties, value);
		}

		setDirty(false);
	}

	/**
	 * @return the menu
	 */
	public MenuModel getMenu() {
		if (menu == null) {
			this.menu = new DefaultMenuModel();

			menu.addSubmenu(createSubMenu(PropertyCategory.Header));
			menu.addSubmenu(createSubMenu(PropertyCategory.Cell));
		}

		return menu;
	}

	/**
	 * @param category
	 * @return
	 */
	protected Submenu createSubMenu(PropertyCategory category) {
		Submenu categoryMenu = new Submenu();
		categoryMenu.setLabel(bundle.getString("properties.category."
				+ category.name()));

		Submenu colorMenu = new Submenu();
		colorMenu.setLabel(bundle.getString("properties.category.color"));
		colorMenu.setIcon("ui-icon-image");
		colorMenu.getChildren().add(createMenuItem(category, "fgColor"));
		colorMenu.getChildren().add(createMenuItem(category, "bgColor"));

		categoryMenu.getChildren().add(colorMenu);

		Submenu fontMenu = new Submenu();
		fontMenu.setLabel(bundle.getString("properties.category.font"));
		fontMenu.setIcon("ui-icon-pencil");
		fontMenu.getChildren().add(createMenuItem(category, "fontFamily"));
		fontMenu.getChildren().add(createMenuItem(category, "fontSize"));
		fontMenu.getChildren().add(createMenuItem(category, "fontStyle"));

		categoryMenu.getChildren().add(fontMenu);

		categoryMenu.getChildren().add(createMenuItem(category, "label"));
		categoryMenu.getChildren().add(createMenuItem(category, "link"));
		categoryMenu.getChildren().add(createMenuItem(category, "styleClass"));

		return categoryMenu;
	}

	/**
	 * @param category
	 * @param key
	 * @return
	 */
	protected MenuItem createMenuItem(PropertyCategory category, String key) {
		PropertyDescriptor property = descriptorFactory.getDescriptor(category,
				key);

		FacesContext context = FacesContext.getCurrentInstance();

		Application application = context.getApplication();
		ExpressionFactory factory = application.getExpressionFactory();

		MenuItem item = new MenuItem();
		item.setValue(property.getName(context));
		item.setTitle(property.getDescription(context));
		item.setIcon(property.getIcon());

		MethodExpression expression = factory.createMethodExpression(
				context.getELContext(), "#{propertiesHandler.selectProperty}",
				Void.TYPE, new Class[0]);
		item.setActionExpression(expression);
		item.setUpdate("content,button-bar,:growl");
		item.setOncomplete("applyThemeToCMEditor('.properties-config .CodeMirror')");

		UIParameter keyParam = new UIParameter();
		keyParam.setName("key");
		keyParam.setValue(key);

		item.getChildren().add(keyParam);

		UIParameter categoryParam = new UIParameter();
		categoryParam.setName("category");
		categoryParam.setValue(category.name());

		item.getChildren().add(categoryParam);

		UIParameter renderParam = new UIParameter();
		renderParam.setName("skipRender");
		renderParam.setValue(true);

		item.getChildren().add(renderParam);

		return item;
	}

	/**
	 * @return key
	 */
	public String getKey() {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, Object> attributes = context.getViewRoot().getAttributes();

		return (String) attributes.get("propertyKey");
	}

	/**
	 * @param key
	 */
	public void setKey(String key) {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, Object> attributes = context.getViewRoot().getAttributes();
		attributes.put("propertyKey", key);
	}

	/**
	 * @return category
	 */
	public PropertyCategory getCategory() {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, Object> attributes = context.getViewRoot().getAttributes();

		return (PropertyCategory) attributes.get("propertyCategory");
	}

	/**
	 * @param category
	 */
	public void setCategory(PropertyCategory category) {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, Object> attributes = context.getViewRoot().getAttributes();
		attributes.put("propertyCategory", category);
	}

	/**
	 * @return dirty
	 */
	public boolean isDirty() {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, Object> attributes = context.getViewRoot().getAttributes();

		Object attribute = attributes.get("propertyChanged");

		return attribute != null && (Boolean) attribute;
	}

	/**
	 * @param dirty
	 */
	public void setDirty(boolean dirty) {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, Object> attributes = context.getViewRoot().getAttributes();
		attributes.put("propertyChanged", dirty);
	}

	public boolean isSet() {
		boolean isSet = false;

		if (descriptor != null) {
			Property property = getProperties(getCategory()).getProperty(
					getKey());
			isSet = property != null;
		}

		return isSet;
	}

	/**
	 * @return the useExpression
	 */
	public boolean getUseExpression() {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, Object> attributes = context.getViewRoot().getAttributes();

		Object attribute = attributes.get("useExpression");

		return attribute != null && (Boolean) attribute;
	}

	/**
	 * @param useExpression
	 *            the useExpression to set
	 */
	public void setUseExpression(boolean useExpression) {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, Object> attributes = context.getViewRoot().getAttributes();
		attributes.put("useExpression", useExpression);
	}

	public void reset() {
		this.value = null;
		this.expression = null;

		setDirty(true);
		setUseExpression(false);
	}

	/**
	 * @return the descriptor
	 */
	public PropertyDescriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * @return the descriptorFactory
	 */
	public PropertyDescriptorFactory getDescriptorFactory() {
		return descriptorFactory;
	}

	/**
	 * @param descriptorFactory
	 *            the descriptorFactory to set
	 */
	public void setDescriptorFactory(PropertyDescriptorFactory descriptorFactory) {
		this.descriptorFactory = descriptorFactory;
	}

	/**
	 * @return the renderer
	 */
	public PivotRenderer getRenderer() {
		return renderer;
	}

	/**
	 * @param renderer
	 *            the renderer to set
	 */
	public void setRenderer(PivotRenderer renderer) {
		this.renderer = renderer;
	}

	/**
	 * @return the editorPanel
	 */
	public UIComponent getEditorPanel() {
		return editorPanel;
	}

	/**
	 * @param editorPanel
	 *            the editorPanel to set
	 */
	public void setEditorPanel(UIComponent editorPanel) {
		this.editorPanel = editorPanel;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * @return the expression
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * @param expression
	 *            the expression to set
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}
}
