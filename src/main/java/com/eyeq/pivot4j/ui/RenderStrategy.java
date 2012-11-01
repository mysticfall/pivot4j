/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import com.eyeq.pivot4j.PivotModel;

public interface RenderStrategy {

	/**
	 * @param model
	 * @param renderer
	 */
	void render(PivotModel model, PivotRenderer renderer);
}
