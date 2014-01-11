/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.analytics.component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.FacesEvent;

import org.primefaces.component.colorpicker.ColorPicker;
import org.primefaces.util.Constants;

/**
 * Workaround for lack of client behavior support in PrimeFaces' ColorPicker
 * component.
 */
@FacesComponent("org.pivot4j.component.ColorPicker")
@ResourceDependencies({ @ResourceDependency(library = "pivot4j", name = "js/colorpicker.js") })
public class AjaxColorPicker extends ColorPicker implements
		ClientBehaviorHolder {

	public static final String COMPONENT_FAMILY = "org.pivot4j.component";

	public static final String COMPONENT_TYPE = "org.pivot4j.component.ColorPicker";

	public static final String RENDERER_TYPE = "org.pivot4j.component.ColorPickerRenderer";

	public AjaxColorPicker() {
		setRendererType(RENDERER_TYPE);
	}

	/**
	 * @see org.primefaces.component.colorpicker.ColorPicker#getFamily()
	 */
	@Override
	public String getFamily() {
		return COMPONENT_FAMILY;
	}

	/**
	 * @see javax.faces.component.UIComponentBase#getEventNames()
	 */
	@Override
	public Collection<String> getEventNames() {
		return Arrays.asList("change");
	}

	/**
	 * @see javax.faces.component.UIComponentBase#queueEvent(javax.faces.event.FacesEvent)
	 */
	@Override
	public void queueEvent(FacesEvent event) {
		FacesContext context = getFacesContext();

		if (event instanceof AjaxBehaviorEvent) {
			AjaxBehaviorEvent behaviorEvent = (AjaxBehaviorEvent) event;
			Map<String, String> params = context.getExternalContext()
					.getRequestParameterMap();
			String eventName = params
					.get(Constants.RequestParams.PARTIAL_BEHAVIOR_EVENT_PARAM);
			String clientId = this.getClientId(context);

			if (eventName.equals("change")) {
				String value = params.get(clientId + "_input");

				if (value != null) {
					ChangeEvent changeEvent = new ChangeEvent(this,
							behaviorEvent.getBehavior(), value);
					changeEvent.setPhaseId(behaviorEvent.getPhaseId());

					super.queueEvent(changeEvent);
				}
			}
		} else {
			super.queueEvent(event);
		}
	}
}
