package org.pivot4j.analytics.ui.chart;

import org.pivot4j.ui.chart.ChartRenderContext;
import org.primefaces.component.chart.Chart;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.CartesianChartModel;

import javax.faces.context.FacesContext;

public abstract class AbstractCartesianChartBuilder<T extends CartesianChartModel>
        extends AbstractChartBuilder<T> {

    /**
     * @param context
     */
    public AbstractCartesianChartBuilder(FacesContext context) {
        super(context);
    }

    @Override
    protected void configureChart(ChartRenderContext context, Chart chart, T model) {
        super.configureChart(context, chart, model);

        DefaultChartRenderer renderer = (DefaultChartRenderer) context
                .getRenderer();

        model.getAxis(AxisType.X).setTickAngle(renderer.getXAxisAngle());
        model.getAxis(AxisType.Y).setTickAngle(renderer.getYAxisAngle());
    }
}
