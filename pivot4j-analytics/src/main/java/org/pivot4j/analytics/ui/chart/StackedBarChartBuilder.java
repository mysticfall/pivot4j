package org.pivot4j.analytics.ui.chart;

import javax.faces.context.FacesContext;

import org.pivot4j.ui.chart.ChartRenderContext;
import org.primefaces.component.chart.Chart;
import org.primefaces.model.chart.BarChartModel;

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
     * @see
     * org.pivot4j.analytics.ui.chart.AbstractChartBuilder#configureChart(org.pivot4j.ui.chart.ChartRenderContext,
     * org.primefaces.component.chart.Chart,
     * org.primefaces.model.chart.ChartModel)
     */
    @Override
    protected void configureChart(ChartRenderContext context, Chart chart, BarChartModel model) {
        super.configureChart(context, chart, model);

        chart.setType("bar");
        model.setStacked(true);
    }
}
