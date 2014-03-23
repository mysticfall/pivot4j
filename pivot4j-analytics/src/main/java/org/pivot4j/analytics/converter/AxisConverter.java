package org.pivot4j.analytics.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang3.StringUtils;
import org.olap4j.Axis;
import org.olap4j.Axis.Standard;

@FacesConverter("axisConverter")
public class AxisConverter implements Converter {

	/**
	 * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext,
	 *      javax.faces.component.UIComponent, java.lang.String)
	 */
	@Override
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {
		if (value == null) {
			return null;
		}

		if (StringUtils.isEmpty(value)) {
			return null;
		}

		Standard axis = Axis.Standard.valueOf(value);

		return Axis.Factory.forOrdinal(axis.axisOrdinal());
	}

	/**
	 * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext,
	 *      javax.faces.component.UIComponent, java.lang.Object)
	 */
	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		if (value == null) {
			return null;
		}

		if (value instanceof String) {
			return (String) value;
		}

		Axis axis = (Axis) value;

		return axis.name();
	}
}
