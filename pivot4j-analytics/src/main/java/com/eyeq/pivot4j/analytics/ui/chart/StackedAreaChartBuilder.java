package com.eyeq.pivot4j.analytics.ui.chart;

import javax.faces.context.FacesContext;

import org.primefaces.component.chart.line.LineChart;

import com.eyeq.pivot4j.ui.chart.ChartRenderContext;

public class StackedAreaChartBuilder extends LineChartBuilder {

	public static String NAME = "StackedArea";

	/**
	 * @param context
	 */
	public StackedAreaChartBuilder(FacesContext context) {
		super(context);
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.ui.chart.LineChartBuilder#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.ui.chart.AbstractChartBuilder#configureChart(com.eyeq.pivot4j.ui.chart.ChartRenderContext,
	 *      org.primefaces.component.chart.UIChart)
	 */
	@Override
	protected void configureChart(ChartRenderContext context, LineChart chart) {
		super.configureChart(context, chart);

		chart.setFill(true);
		chart.setStacked(true);
		chart.setLegendPosition("n");
	}
}
