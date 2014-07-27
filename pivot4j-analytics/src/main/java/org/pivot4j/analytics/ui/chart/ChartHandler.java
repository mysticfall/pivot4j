package org.pivot4j.analytics.ui.chart;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.pivot4j.ModelChangeEvent;
import org.pivot4j.ModelChangeListener;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.ui.PivotStateManager;
import org.pivot4j.analytics.ui.chart.DefaultChartRenderer.Position;
import org.pivot4j.ui.chart.ChartRenderer;
import org.pivot4j.util.OlapUtils;

@ManagedBean(name = "chartHandler")
@ViewScoped
public class ChartHandler implements ModelChangeListener, Serializable {

	private static final long serialVersionUID = 8929886836467291035L;

	@ManagedProperty(value = "#{pivotStateManager}")
	private PivotStateManager stateManager;

	@ManagedProperty(value = "#{chartBuilderFactory}")
	private ChartBuilderFactory chartBuilderFactory;

	private PivotModel model;

	private DefaultChartRenderer renderer;

	private HtmlPanelGroup component;

	private List<SelectItem> charts;

	private Axis pageAxis;

	private Axis chartAxis;

	private Axis seriesAxis;

	private Axis plotAxis;

	private int width;

	private int height;

	private Position legendPosition;

	private int xAxisAngle;

	private int yAxisAngle;

	@PostConstruct
	protected void initialize() {
		this.model = stateManager.getModel();

		if (model != null) {
			model.addModelChangeListener(this);
		}

		this.renderer = new DefaultChartRenderer();

		Serializable state = stateManager.getChartState();

		if (state != null) {
			renderer.restoreState(state);
		}

		this.charts = new LinkedList<SelectItem>();

		reset();

		FacesContext context = FacesContext.getCurrentInstance();

		ResourceBundle resources = context.getApplication().getResourceBundle(
				context, "msg");

		String prefix = "label.chart.items.";

		for (String builder : chartBuilderFactory.getBuilderNames()) {
			charts.add(new SelectItem(builder, resources.getString(prefix
					+ builder)));
		}
	}

	@PreDestroy
	protected void destroy() {
		if (model != null) {
			model.removeModelChangeListener(this);
		}
	}

	/**
	 * @return the renderer
	 */
	public ChartRenderer getRenderer() {
		return renderer;
	}

	/**
	 * @return the component
	 */
	public HtmlPanelGroup getComponent() {
		return component;
	}

	/**
	 * @param component
	 *            the component to set
	 */
	public void setComponent(HtmlPanelGroup component) {
		this.component = component;
	}

	/**
	 * @return the stateManager
	 */
	public PivotStateManager getStateManager() {
		return stateManager;
	}

	/**
	 * @param stateManager
	 *            the stateManager to set
	 */
	public void setStateManager(PivotStateManager stateManager) {
		this.stateManager = stateManager;
	}

	/**
	 * @return the chartBuilderFactory
	 */
	public ChartBuilderFactory getChartBuilderFactory() {
		return chartBuilderFactory;
	}

	/**
	 * @param chartBuilderFactory
	 *            the chartBuilderFactory to set
	 */
	public void setChartBuilderFactory(ChartBuilderFactory chartBuilderFactory) {
		this.chartBuilderFactory = chartBuilderFactory;
	}

	/**
	 * @return the charts
	 */
	public List<SelectItem> getCharts() {
		return charts;
	}

	/**
	 * @return the chartName
	 */
	public String getChartName() {
		return renderer.getChartName();
	}

	/**
	 * @param chartName
	 *            the chartName to set
	 */
	public void setChartName(String chartName) {
		renderer.setChartName(chartName);
	}

	/**
	 * @return the pageAxis
	 */
	public Axis getPageAxis() {
		return pageAxis;
	}

	/**
	 * @param pageAxis
	 *            the pageAxis to set
	 */
	public void setPageAxis(Axis pageAxis) {
		this.pageAxis = pageAxis;
	}

	/**
	 * @return the chartAxis
	 */
	public Axis getChartAxis() {
		return chartAxis;
	}

	/**
	 * @param chartAxis
	 *            the chartAxis to set
	 */
	public void setChartAxis(Axis chartAxis) {
		this.chartAxis = chartAxis;
	}

	/**
	 * @return the seriesAxis
	 */
	public Axis getSeriesAxis() {
		return seriesAxis;
	}

	/**
	 * @param seriesAxis
	 *            the seriesAxis to set
	 */
	public void setSeriesAxis(Axis seriesAxis) {
		this.seriesAxis = seriesAxis;
	}

	/**
	 * @return the plotAxis
	 */
	public Axis getPlotAxis() {
		return plotAxis;
	}

	/**
	 * @param plotAxis
	 *            the plotAxis to set
	 */
	public void setPlotAxis(Axis plotAxis) {
		this.plotAxis = plotAxis;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the legendPosition
	 */
	public Position getLegendPosition() {
		return legendPosition;
	}

	/**
	 * @param legendPosition
	 *            the legendPosition to set
	 */
	public void setLegendPosition(Position legendPosition) {
		this.legendPosition = legendPosition;
	}

	/**
	 * @return the xAxisAngle
	 */
	public int getxAxisAngle() {
		return xAxisAngle;
	}

	/**
	 * @param xAxisAngle
	 *            the xAxisAngle to set
	 */
	public void setxAxisAngle(int xAxisAngle) {
		this.xAxisAngle = xAxisAngle;
	}

	/**
	 * @return the yAxisAngle
	 */
	public int getyAxisAngle() {
		return yAxisAngle;
	}

	/**
	 * @param yAxisAngle
	 *            the yAxisAngle to set
	 */
	public void setyAxisAngle(int yAxisAngle) {
		this.yAxisAngle = yAxisAngle;
	}

	public boolean isValid() {
		if (model == null || !model.isInitialized()) {
			return false;
		}

		CellSet cellSet = model.getCellSet();

		if (cellSet == null) {
			return false;
		}

		List<CellSetAxis> axes = model.getCellSet().getAxes();
		if (axes.size() < 2) {
			return false;
		}

		return axes.get(0).getPositionCount() > 0
				&& axes.get(1).getPositionCount() > 0;
	}

	public void onPreRenderView() {
		FacesContext context = FacesContext.getCurrentInstance();

		if (!context.isPostback()) {
			render();
		}
	}

	public void reset() {
		this.pageAxis = renderer.getPageAxis();
		this.chartAxis = renderer.getChartAxis();
		this.seriesAxis = renderer.getSeriesAxis();
		this.plotAxis = renderer.getPlotAxis();

		this.width = renderer.getWidth();
		this.height = renderer.getHeight();
		this.xAxisAngle = renderer.getXAxisAngle();
		this.yAxisAngle = renderer.getYAxisAngle();
		this.legendPosition = renderer.getLegendPosition();
	}

	public void apply() {
		boolean valid = false;

		valid |= pageAxis != null && !OlapUtils.equals(plotAxis, pageAxis);
		valid |= chartAxis != null && !OlapUtils.equals(plotAxis, chartAxis);
		valid |= seriesAxis != null && !OlapUtils.equals(plotAxis, seriesAxis);

		if (valid) {
			renderer.setPageAxis(pageAxis);
			renderer.setChartAxis(chartAxis);
			renderer.setSeriesAxis(seriesAxis);
			renderer.setPlotAxis(plotAxis);

			renderer.setWidth(width);
			renderer.setHeight(height);

			renderer.setXAxisAngle(xAxisAngle);
			renderer.setYAxisAngle(yAxisAngle);

			renderer.setLegendPosition(legendPosition);

			render();
		} else {
			FacesContext context = FacesContext.getCurrentInstance();

			ResourceBundle messages = context.getApplication()
					.getResourceBundle(context, "msg");

			String title = messages.getString("warn.chart.axis.unused.title");
			String msg = messages.getString("warn.chart.axis.unused.message");

			context.addMessage("axis-plot", new FacesMessage(
					FacesMessage.SEVERITY_WARN, title, msg));

		}
	}

	public void render() {
		String chartName = getChartName();

		if (model != null && model.isInitialized()
				&& StringUtils.isNotBlank(chartName)) {
			FacesContext context = FacesContext.getCurrentInstance();

			ChartBuilder builder = chartBuilderFactory.createChartBuilder(
					chartName, context);
			builder.setComponent(component);

			renderer.render(model, builder);
		}

		if (renderer != null) {
			stateManager.setChartState(renderer.saveState());
		}
	}

	/**
	 * @see org.pivot4j.ModelChangeListener#modelInitialized(org.pivot4j.ModelChangeEvent)
	 */
	@Override
	public void modelInitialized(ModelChangeEvent e) {
	}

	/**
	 * @see org.pivot4j.ModelChangeListener#modelDestroyed(org.pivot4j.ModelChangeEvent)
	 */
	@Override
	public void modelDestroyed(ModelChangeEvent e) {
	}

	/**
	 * @see org.pivot4j.ModelChangeListener#modelChanged(org.pivot4j.ModelChangeEvent)
	 */
	@Override
	public void modelChanged(ModelChangeEvent e) {
	}

	/**
	 * @see org.pivot4j.ModelChangeListener#structureChanged(org.pivot4j.ModelChangeEvent)
	 */
	@Override
	public void structureChanged(ModelChangeEvent e) {
		render();
	}
}
