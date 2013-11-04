package com.eyeq.pivot4j.analytics.ui.chart;

import javax.faces.context.FacesContext;

import org.primefaces.component.chart.bar.BarChart;

import com.eyeq.pivot4j.ui.chart.ChartRenderContext;

public class BarChartBuilder extends AbstractSeriesChartBuilder<BarChart> {

	public static String NAME = "Bar";

	/**
	 * @param context
	 */
	public BarChartBuilder(FacesContext context) {
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
	protected BarChart createChart(ChartRenderContext context) {
		return new BarChart();
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.ui.chart.AbstractChartBuilder#configureChart(com.eyeq.pivot4j.ui.chart.ChartRenderContext,
	 *      org.primefaces.component.chart.UIChart)
	 */
	@Override
	protected void configureChart(ChartRenderContext context, BarChart chart) {
		super.configureChart(context, chart);

		chart.setLegendPosition("n");
		chart.setZoom(true);
	}
}
