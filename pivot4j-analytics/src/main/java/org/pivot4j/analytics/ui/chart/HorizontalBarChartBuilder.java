package org.pivot4j.analytics.ui.chart;

import javax.faces.context.FacesContext;

import org.pivot4j.ui.chart.ChartRenderContext;
import org.primefaces.component.chart.Chart;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.HorizontalBarChartModel;

public class HorizontalBarChartBuilder extends BarChartBuilder {

	public static String NAME = "HorizontalBar";

	/**
	 * @param context
	 */
	public HorizontalBarChartBuilder(FacesContext context) {
		super(context);
	}

	/**
	 * @see org.pivot4j.analytics.ui.chart.ChartBuilder#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected BarChartModel createModel(ChartRenderContext context) {
		return new HorizontalBarChartModel();
	}

	/**
	 * @see org.pivot4j.analytics.ui.chart.AbstractChartBuilder#configureChart(org.pivot4j.ui.chart.ChartRenderContext,
	 *      org.primefaces.component.chart.Chart, org.primefaces.model.chart.ChartModel)
	 */
	@Override
	protected void configureChart(ChartRenderContext context, Chart chart, BarChartModel model) {
		super.configureChart(context, chart, model);

		chart.setType("bar");
		model.setLegendPosition("e");
	}
}
