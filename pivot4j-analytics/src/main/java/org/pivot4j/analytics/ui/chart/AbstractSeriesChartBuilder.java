package org.pivot4j.analytics.ui.chart;

import java.util.List;

import javax.faces.context.FacesContext;

import org.olap4j.metadata.Member;
import org.pivot4j.ui.chart.ChartRenderContext;
import org.primefaces.component.chart.CartesianChart;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;

public abstract class AbstractSeriesChartBuilder<C extends CartesianChart>
		extends AbstractChartBuilder<C, CartesianChartModel> {

	private ChartSeries series;

	/**
	 * @param context
	 */
	public AbstractSeriesChartBuilder(FacesContext context) {
		super(context);
	}

	/**
	 * @see org.pivot4j.analytics.ui.chart.AbstractChartBuilder#renderSeries()
	 */
	@Override
	public boolean renderSeries() {
		return true;
	}

	/**
	 * @return series
	 */
	protected ChartSeries getSeries() {
		return series;
	}

	/**
	 * @see org.pivot4j.analytics.ui.chart.AbstractChartBuilder#createModel(org.pivot4j.ui.chart.ChartRenderContext)
	 */
	@Override
	protected CartesianChartModel createModel(ChartRenderContext context) {
		return new CartesianChartModel();
	}

	/**
	 * @see org.pivot4j.analytics.ui.chart.AbstractChartBuilder#startSeries(org.pivot4j.ui.chart.ChartRenderContext)
	 */
	@Override
	public void startSeries(ChartRenderContext context) {
		this.series = new ChartSeries();

		List<Member> path = context.getSeriesPath();

		if (path.isEmpty()) {
			series.setLabel("");
		} else {
			series.setLabel(path.get(path.size() - 1).getCaption());
		}
	}

	/**
	 * @see org.pivot4j.analytics.ui.chart.AbstractChartBuilder#renderContent(org.pivot4j.ui.chart.ChartRenderContext,
	 *      java.lang.String, java.lang.Double)
	 */
	@Override
	public void renderContent(ChartRenderContext context, String label,
			Double value) {
		series.set(label, value);
	}

	/**
	 * @see org.pivot4j.analytics.ui.chart.AbstractChartBuilder#endSeries(org.pivot4j.ui.chart.ChartRenderContext)
	 */
	@Override
	public void endSeries(ChartRenderContext context) {
		getModel().addSeries(series);

		this.series = null;
	}
}
