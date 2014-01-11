package org.pivot4j.analytics.ui.chart;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;

import org.olap4j.Position;
import org.olap4j.metadata.Member;
import org.pivot4j.ui.AbstractRenderCallback;
import org.pivot4j.ui.chart.ChartRenderContext;
import org.pivot4j.ui.command.UICommand;
import org.primefaces.component.breadcrumb.BreadCrumb;
import org.primefaces.component.chart.UIChart;
import org.primefaces.component.menuitem.UIMenuItem;
import org.primefaces.model.chart.ChartModel;

public abstract class AbstractChartBuilder<C extends UIChart, M extends ChartModel>
		extends AbstractRenderCallback<ChartRenderContext> implements
		ChartBuilder {

	private FacesContext context;

	private UIComponent component;

	private HtmlPanelGrid pageComponent;

	private C chart;

	private M model;

	/**
	 * @param context
	 */
	public AbstractChartBuilder(FacesContext context) {
		this.context = context;
	}

	/**
	 * @return the context
	 */
	protected FacesContext getContext() {
		return context;
	}

	/**
	 * @return the component
	 */
	public UIComponent getComponent() {
		return component;
	}

	/**
	 * @param component
	 *            the component to set
	 */
	public void setComponent(UIComponent component) {
		this.component = component;
	}

	/**
	 * @return the pageComponent
	 */
	protected HtmlPanelGrid getPageComponent() {
		return pageComponent;
	}

	/**
	 * @see org.pivot4j.ui.chart.ChartRenderCallback#renderSeries()
	 */
	@Override
	public boolean renderSeries() {
		return false;
	}

	/**
	 * @return the chart
	 */
	protected C getChart() {
		return chart;
	}

	/**
	 * @return the model
	 */
	protected M getModel() {
		return model;
	}

	/**
	 * @see org.pivot4j.ui.AbstractRenderCallback#startRender(org.pivot4j.ui.RenderContext)
	 */
	@Override
	public void startRender(ChartRenderContext context) {
		component.getChildren().clear();
	}

	/**
	 * @see org.pivot4j.ui.chart.ChartRenderCallback#startPage(org.pivot4j.ui.chart.ChartRenderContext)
	 */
	@Override
	public void startPage(ChartRenderContext context) {
		this.pageComponent = createPageComponent(context);
	}

	/**
	 * @see org.pivot4j.ui.chart.ChartRenderCallback#startChart(org.pivot4j.ui.chart.ChartRenderContext)
	 */
	@Override
	public void startChart(ChartRenderContext context) {
		this.chart = createChart(context);
		this.model = createModel(context);

		configureChart(context, chart);
	}

	protected abstract C createChart(ChartRenderContext context);

	protected abstract M createModel(ChartRenderContext context);

	/**
	 * @param context
	 * @return
	 */
	protected BreadCrumb createBreadCrumb(ChartRenderContext context) {
		BreadCrumb breadCrumb = new BreadCrumb();

		UIMenuItem rootItem = new UIMenuItem();

		rootItem.setValue("");
		breadCrumb.getChildren().add(rootItem);

		List<Member> members = context.getPagePath();

		for (Member member : members) {
			UIMenuItem item = new UIMenuItem();

			item.setValue(member.getCaption());
			item.setTitle(member.getDescription());

			breadCrumb.getChildren().add(item);
		}

		return breadCrumb;
	}

	/**
	 * @param context
	 * @return
	 */
	protected HtmlPanelGrid createPageComponent(ChartRenderContext context) {
		HtmlPanelGrid grid = new HtmlPanelGrid();

		grid.setStyle("width: 100%");
		grid.setStyleClass("chart-page");
		grid.setColumns(context.getChartsPerPage());

		return grid;
	}

	/**
	 * @param context
	 * @param chart
	 */
	protected void configureChart(ChartRenderContext context, C chart) {
		Position position = context.getColumnPosition();

		String title = position.getMembers()
				.get(position.getMembers().size() - 1).getCaption();

		chart.setTitle(title);
		chart.setShadow(true);
		chart.setLegendPosition("w");

		// TODO Make it configurable.
		chart.setStyle("width:100%; height:300px;");
	}

	/**
	 * @see org.pivot4j.ui.chart.ChartRenderCallback#startSeries(org.pivot4j.ui.chart.ChartRenderContext)
	 */
	@Override
	public void startSeries(ChartRenderContext context) {
	}

	/**
	 * @see org.pivot4j.ui.RenderCallback#renderCommands(org.pivot4j.ui.RenderContext,
	 *      java.util.List)
	 */
	@Override
	public void renderCommands(ChartRenderContext context,
			List<UICommand<?>> commands) {
	}

	/**
	 * @see org.pivot4j.ui.chart.ChartRenderCallback#endSeries(org.pivot4j.ui.chart.ChartRenderContext)
	 */
	@Override
	public void endSeries(ChartRenderContext context) {
	}

	/**
	 * @see org.pivot4j.ui.chart.ChartRenderCallback#endChart(org.pivot4j.ui.chart.ChartRenderContext)
	 */
	@Override
	public void endChart(ChartRenderContext context) {
		chart.setValue(model);

		pageComponent.getChildren().add(chart);

		this.model = null;
		this.chart = null;
	}

	/**
	 * @see org.pivot4j.ui.chart.ChartRenderCallback#endPage(org.pivot4j.ui.chart.ChartRenderContext)
	 */
	@Override
	public void endPage(ChartRenderContext context) {
		if (!context.getPagePath().isEmpty()) {
			pageComponent.getFacets().put("header", createBreadCrumb(context));
		}

		component.getChildren().add(pageComponent);

		this.pageComponent = null;
	}
}