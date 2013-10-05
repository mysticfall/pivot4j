/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.property;

import java.util.List;

import com.eyeq.pivot4j.state.Bookmarkable;
import com.eyeq.pivot4j.state.Configurable;

public interface RenderPropertyList extends Configurable, Bookmarkable {

	List<RenderProperty> getRenderProperties();

	/**
	 * @param property
	 */
	void setRenderProperty(RenderProperty property);

	/**
	 * @param name
	 */
	RenderProperty getRenderProperty(String name);

	/**
	 * @param name
	 */
	void removeRenderProperty(String name);

	/**
	 * @param name
	 */
	boolean hasRenderProperty(String name);
}
