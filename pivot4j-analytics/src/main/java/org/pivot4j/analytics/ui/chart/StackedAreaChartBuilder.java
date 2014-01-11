package org.pivot4j.analytics.ui.chart;

import javax.faces.context.FacesContext;

import org.pivot4j.ui.chart.ChartRenderContext;
import org.primefaces.component.chart.line.LineChart;

public class StackedAreaChartBuilder extends LineChartBuilder {

	public static String NAME = "StackedArea";

	/**
	 * @param context
	 */
	public StackedAreaChartBuilder(FacesContext context) {
		super(context);
	}

	/**
	 * @see org.pivot4j.analytics.ui.chart.LineChartBuilder#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * @see org.pivot4j.analytics.ui.chart.AbstractChartBuilder#configureChart(org.pivot4j.ui.chart.ChartRenderContext,
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
