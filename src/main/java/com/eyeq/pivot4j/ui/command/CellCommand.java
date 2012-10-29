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

public interface CellCommand {

	String getName();

	boolean canExecute(RenderContext context);

	CellParameters createParameters(RenderContext context);

	void execute(PivotModel model, CellParameters parameters);
}
