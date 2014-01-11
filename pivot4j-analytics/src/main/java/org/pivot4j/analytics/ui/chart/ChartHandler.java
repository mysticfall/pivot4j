package org.pivot4j.analytics.ui.chart;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.pivot4j.ModelChangeEvent;
import org.pivot4j.ModelChangeListener;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.ui.PivotStateManager;
import org.pivot4j.ui.chart.ChartRenderer;

@ManagedBean(name = "chartHandler")
@ViewScoped
public class ChartHandler implements ModelChangeListener {

	@ManagedProperty(value = "#{pivotStateManager}")
	private PivotStateManager stateManager;

	@ManagedProperty(value = "#{chartBuilderFactory}")
	private ChartBuilderFactory chartBuilderFactory;

	private PivotModel model;

	private ChartRenderer renderer;

	private String chartName;

	private HtmlPanelGroup component;

	private List<SelectItem> charts;

	@PostConstruct
	protected void initialize() {
		this.model = stateManager.getModel();

		if (model != null) {
			model.addModelChangeListener(this);
		}

		this.renderer = new ChartRenderer();
		this.charts = new LinkedList<SelectItem>();

		FacesContext context = FacesContext.getCurrentInstance();

		ResourceBundle resources = context.getApplication().getResourceBundle(
				context, "msg");

		String prefix = "label.chart.items.";

		charts.add(new SelectItem("", resources.getString(prefix + "Grid")));

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
		return chartName;
	}

	/**
	 * @param chartName
	 *            the chartName to set
	 */
	public void setChartName(String chartName) {
		this.chartName = chartName;
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

	/**
	 * 
	 */
	public void render() {
		if (model != null && model.isInitialized()
				&& StringUtils.isNotBlank(chartName)) {
			FacesContext context = FacesContext.getCurrentInstance();

			Map<String, String> parameters = context.getExternalContext()
					.getRequestParameterMap();

			if ("true".equals(parameters.get("skipRender"))) {
				return;
			}

			ChartBuilder builder = chartBuilderFactory.createChartBuilder(
					chartName, context);
			builder.setComponent(component);

			renderer.render(model, builder);
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
