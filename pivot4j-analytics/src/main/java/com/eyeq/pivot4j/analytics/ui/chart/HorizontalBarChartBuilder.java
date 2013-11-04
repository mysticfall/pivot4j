package com.eyeq.pivot4j.analytics.ui.chart;

import javax.faces.context.FacesContext;

import org.primefaces.component.chart.bar.BarChart;

import com.eyeq.pivot4j.ui.chart.ChartRenderContext;

public class HorizontalBarChartBuilder extends BarChartBuilder {

	public static String NAME = "HorizontalBar";

	/**
	 * @param context
	 */
	public HorizontalBarChartBuilder(FacesContext context) {
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
	 * @see com.eyeq.pivot4j.analytics.ui.chart.AbstractChartBuilder#configureChart(com.eyeq.pivot4j.ui.chart.ChartRenderContext,
	 *      org.primefaces.component.chart.UIChart)
	 */
	@Override
	protected void configureChart(ChartRenderContext context, BarChart chart) {
		super.configureChart(context, chart);

		chart.setLegendPosition("e");
		chart.setOrientation("horizontal");
	}
}
