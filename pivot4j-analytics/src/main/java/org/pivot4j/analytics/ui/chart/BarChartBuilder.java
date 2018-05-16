package org.pivot4j.analytics.ui.chart;

import javax.faces.context.FacesContext;

import org.pivot4j.ui.chart.ChartRenderContext;
import org.primefaces.component.chart.Chart;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.BarChartSeries;
import org.primefaces.model.chart.ChartSeries;

public class BarChartBuilder extends AbstractSeriesChartBuilder<BarChartModel> {

    public static String NAME = "Bar";

    /**
     * @param context
     */
    public BarChartBuilder(FacesContext context) {
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
        return new BarChartModel();
    }

    @Override
    protected ChartSeries createSeries() {
        return new BarChartSeries();
    }

    /**
     * @see
     * org.pivot4j.analytics.ui.chart.AbstractChartBuilder#configureChart(org.pivot4j.ui.chart.ChartRenderContext,
     * org.primefaces.component.chart.Chart,
     * org.primefaces.model.chart.ChartModel)
     */
    @Override
    protected void configureChart(ChartRenderContext context, Chart chart, BarChartModel model) {
        super.configureChart(context, chart, model);

        model.setZoom(true);
    }
}
