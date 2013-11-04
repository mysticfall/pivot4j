/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.chart;

import com.eyeq.pivot4j.ui.RenderCallback;

public interface ChartRenderCallback extends RenderCallback<ChartRenderContext> {
	
	boolean renderSeries();

	void startPage(ChartRenderContext context);

	void startChart(ChartRenderContext context);

	void startSeries(ChartRenderContext context);

	void endSeries(ChartRenderContext context);

	void endChart(ChartRenderContext context);

	void endPage(ChartRenderContext context);
}
