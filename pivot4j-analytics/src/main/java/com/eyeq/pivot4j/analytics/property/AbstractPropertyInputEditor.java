package com.eyeq.pivot4j.analytics.property;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;

import org.primefaces.component.behavior.ajax.AjaxBehavior;
import org.primefaces.component.behavior.ajax.AjaxBehaviorListenerImpl;

public abstract class AbstractPropertyInputEditor extends
		AbstractPropertyEditor {

	/**
	 * @see com.eyeq.pivot4j.analytics.property.PropertyEditor#createComponent(com.eyeq.pivot4j.analytics.property.PropertyDescriptor,
	 *      javax.faces.component.UIComponent, javax.el.ValueExpression,
	 *      javax.el.MethodExpression, java.lang.String)
	 */
	@Override
	public void createComponent(PropertyDescriptor descriptor,
			UIComponent parent, ValueExpression expression,
			MethodExpression listener, String update) {
		FacesContext context = FacesContext.getCurrentInstance();

		UIInput input = createInput(descriptor, parent, context);

		input.setValueExpression("value", expression);

		String eventName = getEventName();
		if (eventName != null) {
			AjaxBehavior behavior = new AjaxBehavior();
			behavior.addAjaxBehaviorListener(new AjaxBehaviorListenerImpl(
					listener, listener));
			behavior.setUpdate(update);

			input.addClientBehavior("change", behavior);

			UIParameter parameter = new UIParameter();
			parameter.setName("skipRender");
			parameter.setValue("true");

			input.getChildren().add(parameter);
		}

		parent.getChildren().add(input);
	}

	protected String getEventName() {
		return "change";
	}

	/**
	 * @param descriptor
	 * @param parent
	 * @param context
	 * @return
	 */
	protected abstract UIInput createInput(PropertyDescriptor descriptor,
			UIComponent parent, FacesContext context);
}
