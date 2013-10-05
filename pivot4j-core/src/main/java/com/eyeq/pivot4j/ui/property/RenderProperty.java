/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.property;

import com.eyeq.pivot4j.state.Bookmarkable;
import com.eyeq.pivot4j.state.Configurable;
import com.eyeq.pivot4j.ui.RenderContext;

public interface RenderProperty extends Comparable<RenderProperty>,
		Configurable, Bookmarkable {

	String getName();

	/**
	 * @param context
	 * @return
	 */
	Object getValue(RenderContext context);
}
