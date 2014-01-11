/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.analytics.component;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.render.FacesRenderer;

import org.primefaces.component.colorpicker.ColorPicker;
import org.primefaces.component.colorpicker.ColorPickerRenderer;
import org.primefaces.util.WidgetBuilder;

@FacesRenderer(componentFamily = "org.pivot4j.component", rendererType = "org.pivot4j.component.ColorPickerRenderer")
public class AjaxColorPickerRenderer extends ColorPickerRenderer {

	public static final String RENDERER_TYPE = "org.pivot4j.component.ColorPickerRenderer";

	/**
	 * @see org.primefaces.component.colorpicker.ColorPickerRenderer#decode(javax.faces.context.FacesContext,
	 *      javax.faces.component.UIComponent)
	 */
	@Override
	public void decode(FacesContext context, UIComponent component) {
		decodeBehaviors(context, component);

		super.decode(context, component);
	}

	/**
	 * @see org.primefaces.component.colorpicker.ColorPickerRenderer#encodeScript(javax.faces.context.FacesContext,
	 *      org.primefaces.component.colorpicker.ColorPicker)
	 */
	@Override
	protected void encodeScript(FacesContext context, ColorPicker colorPicker)
			throws IOException {
		String clientId = colorPicker.getClientId(context);
		String value = (String) colorPicker.getValue();

		WidgetBuilder wb = getWidgetBuilder(context);

		wb.init("AjaxColorPicker", colorPicker.resolveWidgetVar(), clientId,
				"colorpicker").attr("mode", colorPicker.getMode())
				.attr("color", value, null);

		encodeClientBehaviors(context, (ClientBehaviorHolder) colorPicker);

		wb.finish();
	}
}
