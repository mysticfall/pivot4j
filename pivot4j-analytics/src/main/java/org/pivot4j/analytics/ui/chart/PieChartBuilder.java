package org.pivot4j.analytics.ui.chart;

import javax.faces.context.FacesContext;

import org.pivot4j.ui.chart.ChartRenderContext;
import org.primefaces.component.chart.pie.PieChart;
import org.primefaces.model.chart.PieChartModel;

public class PieChartBuilder extends
		AbstractChartBuilder<PieChart, PieChartModel> {

	public static String NAME = "Pie";

	/**
	 * @param context
	 */
	public PieChartBuilder(FacesContext context) {
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
	protected PieChart createChart(ChartRenderContext context) {
		return new PieChart();
	}

	/**
	 * @see org.pivot4j.analytics.ui.chart.AbstractChartBuilder#configureChart(org.pivot4j.ui.chart.ChartRenderContext,
	 *      org.primefaces.component.chart.UIChart)
	 */
	@Override
	protected void configureChart(ChartRenderContext context, PieChart chart) {
		super.configureChart(context, chart);

		chart.setShowDataLabels(true);
		chart.setDataFormat("value");
		chart.setShadow(true);
	}

	/**
	 * @see org.pivot4j.analytics.ui.chart.AbstractChartBuilder#createModel(org.pivot4j.ui.chart.ChartRenderContext)
	 */
	@Override
	protected PieChartModel createModel(ChartRenderContext context) {
		return new PieChartModel();
	}

	/**
	 * @see org.pivot4j.ui.RenderCallback#renderContent(org.pivot4j.ui.RenderContext,
	 *      java.lang.String, java.lang.Double)
	 */
	@Override
	public void renderContent(ChartRenderContext context, String label,
			Double value) {
		getModel().set(label, value);
	}
}
