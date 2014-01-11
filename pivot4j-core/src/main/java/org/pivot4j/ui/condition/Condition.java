/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.condition;

import org.pivot4j.state.Bookmarkable;
import org.pivot4j.state.Configurable;
import org.pivot4j.ui.RenderContext;

public interface Condition extends Bookmarkable, Configurable {

	String getName();

	/**
	 * @param context
	 * @return
	 */
	boolean matches(RenderContext context);
}