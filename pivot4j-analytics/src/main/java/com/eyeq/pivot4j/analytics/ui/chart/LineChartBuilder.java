package com.eyeq.pivot4j.analytics.ui.chart;

import javax.faces.context.FacesContext;

import org.primefaces.component.chart.line.LineChart;

import com.eyeq.pivot4j.ui.chart.ChartRenderContext;

public class LineChartBuilder extends AbstractSeriesChartBuilder<LineChart> {

	public static String NAME = "Line";

	/**
	 * @param context
	 */
	public LineChartBuilder(FacesContext context) {
		super(context);
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.ui.chart.ChartBuilder#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.ui.chart.AbstractChartBuilder#createChart(com.eyeq.pivot4j.ui.chart.ChartRenderContext)
	 */
	@Override
	protected LineChart createChart(ChartRenderContext context) {
		return new LineChart();
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.ui.chart.AbstractChartBuilder#configureChart(com.eyeq.pivot4j.ui.chart.ChartRenderContext,
	 *      org.primefaces.component.chart.UIChart)
	 */
	@Override
	protected void configureChart(ChartRenderContext context, LineChart chart) {
		super.configureChart(context, chart);

		chart.setZoom(true);
	}
}
