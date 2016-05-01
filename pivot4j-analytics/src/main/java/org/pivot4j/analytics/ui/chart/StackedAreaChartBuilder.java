package org.pivot4j.analytics.ui.chart;

import javax.faces.context.FacesContext;

import org.pivot4j.ui.chart.ChartRenderContext;
import org.primefaces.component.chart.Chart;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

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

	@Override
	protected ChartSeries createSeries() {
		LineChartSeries series = (LineChartSeries) super.createSeries();
		series.setFill(true);

		return series;
	}

	/**
	 * @see org.pivot4j.analytics.ui.chart.AbstractChartBuilder#configureChart(org.pivot4j.ui.chart.ChartRenderContext,
	 *      org.primefaces.component.chart.Chart, org.primefaces.model.chart.ChartModel)
	 */
	@Override
	protected void configureChart(ChartRenderContext context, Chart chart, LineChartModel model) {
		super.configureChart(context, chart, model);

		chart.setType("line");

		model.setStacked(true);
		model.setLegendPosition("n");
	}
}
