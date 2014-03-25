package org.pivot4j.analytics.ui.chart;

import java.util.LinkedList;
import java.util.List;

import javax.faces.context.FacesContext;

import org.olap4j.metadata.Member;
import org.pivot4j.ui.chart.ChartRenderContext;
import org.pivot4j.ui.chart.ChartRenderer;
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

		List<Member> path = new LinkedList<Member>(context.getSeriesPath());

		if (!path.isEmpty() && context.getSeriesCount() > 1) {
			String label;

			int size = path.size();
			if (size == 1) {
				label = path.get(0).getCaption();
			} else {
				StringBuilder builder = new StringBuilder();

				boolean first = true;
				for (Member member : path) {
					if (first) {
						first = false;
					} else {
						builder.append(ChartRenderer.DEFAULT_MEMBER_SEPARATOR);
					}

					builder.append(member.getCaption());
				}

				label = builder.toString();
			}

			series.setLabel(label);
		}
	}

	/**
	 * @see org.pivot4j.ui.RenderCallback#renderContent(org.pivot4j.ui.RenderContext,
	 *      java.lang.String, java.lang.Double)
	 */
	@Override
	public void renderContent(ChartRenderContext context, String label,
			Double value) {
		if (series.getLabel() == null) {
			series.setLabel(context.getMember().getHierarchy().getCaption());
		}

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
