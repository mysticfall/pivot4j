/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.command;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.ui.RenderContext;

public interface CellCommand<T> {

	String getName();

	String getDescription();

	/**
	 * For commands that have a state that should be represented differently in
	 * UI (i.e. different icons for each sort modes)
	 * 
	 * @param context
	 * @return
	 */
	String getMode(RenderContext context);

	boolean canExecute(RenderContext context);

	CellParameters createParameters(RenderContext context);

	T execute(PivotModel model, CellParameters parameters);
}
