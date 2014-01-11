package org.pivot4j.analytics.ui.chart;

import javax.faces.context.FacesContext;

import org.pivot4j.ui.chart.ChartRenderContext;
import org.primefaces.component.chart.bar.BarChart;

public class StackedBarChartBuilder extends BarChartBuilder {

	public static String NAME = "StackedBar";

	/**
	 * @param context
	 */
	public StackedBarChartBuilder(FacesContext context) {
		super(context);
	}

	/**
	 * @see org.pivot4j.analytics.ui.chart.ChartBuilder#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * @see org.pivot4j.analytics.ui.chart.AbstractChartBuilder#createChart(org.pivot4j.ui.chart.ChartRenderContext)
	 */
	@Override
	protected BarChart createChart(ChartRenderContext context) {
		BarChart chart = super.createChart(context);

		chart.setStacked(true);

		return chart;
	}
}
