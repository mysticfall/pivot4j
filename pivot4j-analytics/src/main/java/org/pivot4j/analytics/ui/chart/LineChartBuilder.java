package org.pivot4j.analytics.ui.chart;

import javax.faces.context.FacesContext;

import org.pivot4j.ui.chart.ChartRenderContext;
import org.primefaces.component.chart.Chart;
import org.primefaces.model.chart.*;

public class LineChartBuilder extends AbstractSeriesChartBuilder<LineChartModel> {

    public static String NAME = "Line";

    /**
     * @param context
     */
    public LineChartBuilder(FacesContext context) {
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
    protected LineChartModel createModel(ChartRenderContext context) {
        return new LineChartModel();
    }

    @Override
    protected ChartSeries createSeries() {
        return new LineChartSeries();
    }

    /**
     * @see
     * org.pivot4j.analytics.ui.chart.AbstractChartBuilder#configureChart(org.pivot4j.ui.chart.ChartRenderContext,
     * org.primefaces.component.chart.Chart,
     * org.primefaces.model.chart.ChartModel)
     */
    @Override
    protected void configureChart(ChartRenderContext context, Chart chart, LineChartModel model) {
        super.configureChart(context, chart, model);

        model.setZoom(true);
        model.setShowPointLabels(true);
        model.getAxes().put(AxisType.X, new CategoryAxis(""));
    }
}
