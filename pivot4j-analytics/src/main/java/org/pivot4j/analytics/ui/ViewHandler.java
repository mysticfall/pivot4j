package org.pivot4j.analytics.ui;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItem;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.olap4j.AllocationPolicy;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapDataSource;
import org.olap4j.OlapException;
import org.olap4j.Scenario;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Schema;
import org.pivot4j.ModelChangeEvent;
import org.pivot4j.ModelChangeListener;
import org.pivot4j.PivotModel;
import org.pivot4j.QueryEvent;
import org.pivot4j.QueryListener;
import org.pivot4j.analytics.config.Settings;
import org.pivot4j.analytics.datasource.ConnectionInfo;
import org.pivot4j.analytics.state.ViewState;
import org.pivot4j.impl.PivotModelImpl;
import org.pivot4j.transform.NonEmpty;
import org.pivot4j.transform.SwapAxes;
import org.pivot4j.ui.PivotRenderer;
import org.pivot4j.ui.command.BasicDrillThroughCommand;
import org.pivot4j.ui.command.DrillDownCommand;
import org.pivot4j.ui.command.UICommand;
import org.pivot4j.ui.command.UICommandParameters;
import org.pivot4j.ui.table.TableRenderer;
import org.pivot4j.util.OlapUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.extensions.event.CloseEvent;
import org.primefaces.extensions.event.OpenEvent;
import org.primefaces.extensions.model.layout.LayoutOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name = "viewHandler")
@RequestScoped
public class ViewHandler implements QueryListener, ModelChangeListener {

	@ManagedProperty(value = "#{pivotStateManager}")
	private PivotStateManager stateManager;

	@ManagedProperty(value = "#{navigatorHandler}")
	private NavigatorHandler navigator;

	@ManagedProperty(value = "#{drillThroughHandler}")
	private DrillThroughHandler drillThroughHandler;

	@ManagedProperty(value = "#{settings}")
	private Settings settings;

	private LayoutOptions layoutOptions;

	private PivotModel model;

	private DefaultTableRenderer renderer;

	private List<UISelectItem> cubeItems;

	private String cubeName;

	private String currentMdx;

	private long duration;

	private UIComponent component;

	private UIComponent filterComponent;

	private Exception lastError;

	@PostConstruct
	protected void initialize() {
		this.model = stateManager.getModel();

		if (model != null) {
			model.addQueryListener(this);
			model.addModelChangeListener(this);

			if (model.isInitialized()) {
				this.cubeName = model.getCube().getName();

				checkError(model);
			} else {
				ConnectionInfo connectionInfo = stateManager
						.getConnectionInfo();

				if (connectionInfo != null && !model.isInitialized()) {
					this.cubeName = connectionInfo.getCubeName();

					onCubeChange();
				}
			}
		}

		this.renderer = new DefaultTableRenderer();

		Serializable state = stateManager.getRendererState();

		if (state == null) {
			try {
				renderer.restoreSettings(settings.getConfiguration()
						.configurationAt("render"));
			} catch (IllegalArgumentException e) {
			}

			renderer.setVisible(true);
			renderer.setShowDimensionTitle(true);
			renderer.setShowParentMembers(false);
			renderer.setHideSpans(false);
			renderer.setDrillDownMode(DrillDownCommand.MODE_POSITION);
			renderer.setEnableDrillThrough(false);
		} else {
			renderer.restoreState(state);
		}

		renderer.addCommand(new DrillThroughCommandImpl(renderer));
	}

	/**
	 * @param model
	 */
	private void checkError(PivotModel model) {
		Exception error = null;

		try {
			model.getCellSet();
		} catch (Exception e) {
			model.destroy();

			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Failed to get query result.", e);

			error = e;

			FacesContext context = FacesContext.getCurrentInstance();
			ResourceBundle bundle = context.getApplication().getResourceBundle(
					context, "msg");

			String title = bundle.getString("error.unhandled.title");
			String message = bundle.getString("error.unhandled.message") + e;

			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_ERROR, title, message));
		}

		this.lastError = error;
	}

	@PreDestroy
	protected void destroy() {
		if (model != null) {
			model.removeQueryListener(this);
			model.removeModelChangeListener(this);
		}
	}

	/**
	 * @return the settings
	 */
	public Settings getSettings() {
		return settings;
	}

	/**
	 * @param settings
	 */
	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	/**
	 * @return the renderer
	 */
	public TableRenderer getRenderer() {
		return renderer;
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
	 * @return the drillThroughHandler
	 */
	public DrillThroughHandler getDrillThroughHandler() {
		return drillThroughHandler;
	}

	/**
	 * @param drillThroughHandler
	 *            the drillThroughHandler to set
	 */
	public void setDrillThroughHandler(DrillThroughHandler drillThroughHandler) {
		this.drillThroughHandler = drillThroughHandler;
	}

	/**
	 * @return the cubeName
	 */
	public String getCubeName() {
		return cubeName;
	}

	/**
	 * @param cubeName
	 *            the cubeName to set
	 */
	public void setCubeName(String cubeName) {
		this.cubeName = cubeName;
	}

	/**
	 * @return the navigator
	 */
	public NavigatorHandler getNavigator() {
		return navigator;
	}

	/**
	 * @param navigator
	 *            the navigator to set
	 */
	public void setNavigator(NavigatorHandler navigator) {
		this.navigator = navigator;
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
	 * @return the filterComponent
	 */
	public UIComponent getFilterComponent() {
		return filterComponent;
	}

	/**
	 * @param filterComponent
	 *            the filterComponent to set
	 */
	public void setFilterComponent(UIComponent filterComponent) {
		this.filterComponent = filterComponent;
	}

	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}

	public List<UISelectItem> getCubes() throws SQLException {
		if (cubeItems == null) {
			FacesContext context = FacesContext.getCurrentInstance();

			ResourceBundle bundle = context.getApplication().getResourceBundle(
					context, "msg");

			String defaultLabel = bundle.getString("message.cubeList.default");

			this.cubeItems = new ArrayList<UISelectItem>();

			UISelectItem defaultItem = new UISelectItem();
			defaultItem.setItemLabel(defaultLabel);
			defaultItem.setItemValue("");

			cubeItems.add(defaultItem);

			if (model != null) {
				OlapDataSource dataSource = ((PivotModelImpl) model)
						.getDataSource();

				Schema schema = dataSource.getConnection().getOlapSchema();

				List<Cube> cubes = schema.getCubes();

				for (Cube cube : cubes) {
					if (cube.isVisible()) {
						UISelectItem item = new UISelectItem();
						item.setItemLabel(cube.getCaption());
						item.setItemValue(cube.getName());

						cubeItems.add(item);
					}
				}
			}
		}

		return cubeItems;
	}

	public void onCubeChange() {
		if (StringUtils.isEmpty(cubeName)) {
			if (model.isInitialized()) {
				model.destroy();
			}
		} else {
			model.setMdx(getDefaultMdx());

			if (!model.isInitialized()) {
				try {
					model.initialize();
				} catch (Exception e) {
					FacesContext context = FacesContext.getCurrentInstance();
					ResourceBundle bundle = context.getApplication()
							.getResourceBundle(context, "msg");

					String title = bundle.getString("error.unhandled.title");
					String message = bundle
							.getString("error.unhandled.message") + e;

					Logger log = LoggerFactory.getLogger(getClass());
					if (log.isErrorEnabled()) {
						log.error(title, e);
					}

					context.addMessage(null, new FacesMessage(
							FacesMessage.SEVERITY_ERROR, title, message));
				}
			}
		}
	}

	private String getDefaultMdx() {
		String mdx;

		if (OlapUtils.isEmptySetSupported(model.getMetadata())) {
			if (model.getDefaultNonEmpty()) {
				mdx = String
						.format("select non empty {} on columns, non empty {} on rows from [%s]",
								cubeName);
			} else {
				mdx = String.format(
						"select {} on columns, {} on rows from [%s]", cubeName);
			}
		} else {
			mdx = String.format("select from [%s]", cubeName);
		}

		return mdx;
	}

	/**
	 * @return the layoutOptions
	 */
	public LayoutOptions getLayoutOptions() {
		if (layoutOptions == null) {
			ViewState view = stateManager.getState();

			this.layoutOptions = new LayoutOptions();
			layoutOptions.addOption("enableCursorHotkey", false);

			LayoutOptions toolbarOptions = new LayoutOptions();
			toolbarOptions.addOption("resizable", false);
			toolbarOptions.addOption("closable", false);

			layoutOptions.setNorthOptions(toolbarOptions);

			LayoutOptions navigatorOptions = new LayoutOptions();
			navigatorOptions.addOption("resizable", true);
			navigatorOptions.addOption("closable", true);
			navigatorOptions.addOption("slidable", true);
			navigatorOptions.addOption("size", 280);

			if (!view.isRegionVisible(LayoutRegion.Navigator)) {
				navigatorOptions.addOption("initClosed", true);
			}

			layoutOptions.setWestOptions(navigatorOptions);

			LayoutOptions childWestOptions = new LayoutOptions();
			navigatorOptions.setChildOptions(childWestOptions);

			LayoutOptions cubeListOptions = new LayoutOptions();
			cubeListOptions.addOption("resizable", false);
			cubeListOptions.addOption("closable", false);
			cubeListOptions.addOption("slidable", false);
			cubeListOptions.addOption("size", 38);

			childWestOptions.setNorthOptions(cubeListOptions);

			LayoutOptions targetTreeOptions = new LayoutOptions();
			targetTreeOptions.addOption("resizable", true);
			targetTreeOptions.addOption("closable", true);
			targetTreeOptions.addOption("slidable", true);
			targetTreeOptions.addOption("size", 300);

			childWestOptions.setSouthOptions(targetTreeOptions);

			LayoutOptions contentOptions = new LayoutOptions();
			layoutOptions.setCenterOptions(contentOptions);

			LayoutOptions childCenterOptions = new LayoutOptions();
			childCenterOptions.addOption("onresize_end", "onViewResize");
			contentOptions.setChildOptions(childCenterOptions);

			LayoutOptions filterOptions = new LayoutOptions();
			filterOptions.addOption("resizable", false);
			filterOptions.addOption("closable", true);
			filterOptions.addOption("slidable", true);
			filterOptions.addOption("size", 38);

			if (!view.isRegionVisible(LayoutRegion.Filter)) {
				filterOptions.addOption("initClosed", true);
			}

			childCenterOptions.setNorthOptions(filterOptions);

			LayoutOptions editorOptions = new LayoutOptions();
			editorOptions.addOption("resizable", true);
			editorOptions.addOption("closable", true);
			editorOptions.addOption("slidable", true);
			editorOptions.addOption("size", 180);

			if (!view.isRegionVisible(LayoutRegion.Mdx)) {
				editorOptions.addOption("initClosed", true);
			}

			childCenterOptions.setSouthOptions(editorOptions);

			LayoutOptions editorToolBarOptions = new LayoutOptions();
			editorToolBarOptions.addOption("resizable", false);
			editorToolBarOptions.addOption("closable", false);
			editorToolBarOptions.addOption("slidable", false);
			editorToolBarOptions.addOption("size", 38);

			editorOptions.setNorthOptions(editorToolBarOptions);

			LayoutOptions editorContentOptions = new LayoutOptions();
			editorContentOptions.addOption("resizable", false);
			editorContentOptions.addOption("closable", false);
			editorContentOptions.addOption("slidable", false);
			editorContentOptions.addOption("spacing_open", 0);
			editorContentOptions.addOption("spacing_closed", 0);

			editorOptions.setChildOptions(editorContentOptions);
		}

		return layoutOptions;
	}

	public void onPreRenderView() {
		FacesContext context = FacesContext.getCurrentInstance();

		if (!context.isPostback()) {
			render();
		}
	}

	/**
	 * @param event
	 */
	public void onPanelOpened(OpenEvent event) {
		LayoutRegion region = getRegionFromId(event.getComponent().getId());
		if (region != null) {
			stateManager.getState().setRegionVisible(region, true);
		}
	}

	/**
	 * @param event
	 */
	public void onPanelClosed(CloseEvent event) {
		LayoutRegion region = getRegionFromId(event.getComponent().getId());

		if (region != null) {
			stateManager.getState().setRegionVisible(region, false);
		}
	}

	/**
	 * @param id
	 * @return
	 */
	private LayoutRegion getRegionFromId(String id) {
		LayoutRegion region = null;

		if ("mdx-editor-pane".equals(id)) {
			region = LayoutRegion.Mdx;
		} else if ("navigator-pane".equals(id)) {
			region = LayoutRegion.Navigator;
		} else if ("grid-header-pane".equals(id)) {
			region = LayoutRegion.Filter;
		}

		return region;
	}

	public boolean isValid() {
		if (model == null || !model.isInitialized() || lastError != null) {
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
	 * @return the lastError
	 */
	public Exception getLastError() {
		return lastError;
	}

	public String getLastErrorMessage() {
		if (lastError == null) {
			return null;
		}

		return ExceptionUtils.getRootCauseMessage(lastError);
	}

	public void render() {
		if (component != null) {
			component.getChildren().clear();
		}

		if (filterComponent != null) {
			filterComponent.getChildren().clear();
		}

		boolean valid = isValid();

		boolean renderGrid = valid
				&& (component != null && component.isRendered());
		boolean renderFilter = valid
				&& (filterComponent != null && filterComponent.isRendered());

		if (renderGrid || renderFilter) {
			FacesContext context = FacesContext.getCurrentInstance();

			PivotComponentBuilder callback = new PivotComponentBuilder(context);
			callback.setGridPanel(component);
			callback.setFilterPanel(filterComponent);

			renderer.setEnableDrillDown(isEditable());
			renderer.setEnableSort(isEditable());

			renderer.render(model, callback);
		}

		if (renderer != null) {
			stateManager.setRendererState(renderer.saveState());
		}
	}

	public void executeCommand() {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, String> requestParameters = context.getExternalContext()
				.getRequestParameterMap();

		UICommandParameters parameters = new UICommandParameters();

		if (requestParameters.containsKey("axis")) {
			parameters.setAxisOrdinal(Integer.parseInt(requestParameters
					.get("axis")));
		}

		if (requestParameters.containsKey("position")) {
			parameters.setPositionOrdinal(Integer.parseInt(requestParameters
					.get("position")));
		}

		if (requestParameters.containsKey("member")) {
			parameters.setMemberOrdinal(Integer.parseInt(requestParameters
					.get("member")));
		}

		if (requestParameters.containsKey("hierarchy")) {
			parameters.setHierarchyOrdinal(Integer.parseInt(requestParameters
					.get("hierarchy")));
		}

		if (requestParameters.containsKey("cell")) {
			parameters.setCellOrdinal(Integer.parseInt(requestParameters
					.get("cell")));
		}

		UICommand<?> command = renderer.getCommand(requestParameters
				.get("command"));
		command.execute(model, parameters);

		render();
	}

	public void updateCell() {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, String> parameters = context.getExternalContext()
				.getRequestParameterMap();

		int ordinal = Integer.parseInt(parameters.get("cell"));

		String id = "input-" + ordinal;

		UIInput input = (UIInput) component.findComponent(id);
		Double value = (Double) input.getValue();

		Cell cell = model.getCellSet().getCell(ordinal);

		try {
			cell.setValue(value, AllocationPolicy.EQUAL_ALLOCATION);
		} catch (OlapException e) {
			throw new FacesException(e);
		}

		model.refresh();
	}

	public void executeMdx() {
		try {
			model.setMdx(currentMdx);

			if (!model.isInitialized()) {
				model.initialize();
			}

			render();
		} catch (Exception e) {
			this.lastError = e;

			if (model.isInitialized()) {
				model.destroy();
			}

			FacesContext context = FacesContext.getCurrentInstance();

			ResourceBundle bundle = context.getApplication().getResourceBundle(
					context, "msg");

			String title = bundle.getString("error.execute.title");

			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_ERROR, title, e.getMessage()));
		}
	}

	/**
	 * @return the currentMdx
	 */
	public String getCurrentMdx() {
		if (model == null) {
			return null;
		}

		if (lastError != null && StringUtils.isBlank(model.getCurrentMdx())) {
			return model.getMdx();
		}

		return model.getCurrentMdx();
	}

	/**
	 * @param currentMdx
	 */
	public void setCurrentMdx(String currentMdx) {
		this.currentMdx = currentMdx;
	}

	/**
	 * @return the showParentMembers
	 */
	public boolean getShowParentMembers() {
		return renderer.getShowParentMembers();
	}

	/**
	 * @param showParentMembers
	 *            the showParentMembers to set
	 */
	public void setShowParentMembers(boolean showParentMembers) {
		renderer.setShowParentMembers(showParentMembers);
	}

	/**
	 * @return the hideSpans
	 */
	public boolean getHideSpans() {
		return renderer.getHideSpans();
	}

	/**
	 * @param hideSpans
	 *            the hideSpans to set
	 */
	public void setHideSpans(boolean hideSpans) {
		renderer.setHideSpans(hideSpans);
	}

	/**
	 * @return the drillThrough
	 */
	public boolean getDrillThrough() {
		return renderer.getEnableDrillThrough();
	}

	/**
	 * @param drillThrough
	 *            the drillThrough to set
	 */
	public void setDrillThrough(boolean drillThrough) {
		renderer.setEnableDrillThrough(drillThrough);
	}

	public void toggleDrillThrough() {
		setDrillThrough(!getDrillThrough());
		render();
	}

	/**
	 * @return the drillDownMode
	 */
	public String getDrillDownMode() {
		return renderer.getDrillDownMode();
	}

	/**
	 * @param drillDownMode
	 *            the drillDownMode to set
	 */
	public void setDrillDownMode(String drillDownMode) {
		renderer.setDrillDownMode(drillDownMode);
	}

	/**
	 * @return the visible
	 */
	public boolean isVisible() {
		return renderer.isVisible();
	}

	/**
	 * @param visible
	 *            the visible to set
	 */
	public void setVisible(boolean visible) {
		this.renderer.setVisible(visible);
	}

	public void toggleGrid() {
		setVisible(!isVisible());
		render();
	}

	/**
	 * @return the editable
	 */
	public boolean isEditable() {
		return stateManager.getState().isEditable();
	}

	/**
	 * @param editable
	 *            the editable to set
	 */
	public void setEditable(boolean editable) {
		stateManager.getState().setEditable(editable);
	}

	/**
	 * @return the swapAxes
	 */
	public boolean getSwapAxes() {
		if (model == null || !model.isInitialized()) {
			return false;
		}

		SwapAxes transform = model.getTransform(SwapAxes.class);
		return transform.isSwapAxes();
	}

	/**
	 * @param swapAxes
	 *            the swapAxes to set
	 */
	public void setSwapAxes(boolean swapAxes) {
		SwapAxes transform = model.getTransform(SwapAxes.class);

		boolean current = transform.isSwapAxes();
		transform.setSwapAxes(swapAxes);

		if (current != swapAxes) {
			renderer.swapAxes();
		}
	}

	/**
	 * @return the nonEmpty
	 */
	public boolean getNonEmpty() {
		if (model == null || !model.isInitialized()) {
			return false;
		}

		NonEmpty transform = model.getTransform(NonEmpty.class);
		return transform.isNonEmpty();
	}

	/**
	 * @param nonEmpty
	 *            the nonEmpty to set
	 */
	public void setNonEmpty(boolean nonEmpty) {
		NonEmpty transform = model.getTransform(NonEmpty.class);
		transform.setNonEmpty(nonEmpty);
	}

	/**
	 * @return the scenarioEnabled
	 */
	public boolean isScenarioEnabled() {
		return model.isInitialized() && model.isScenarioSupported()
				&& model.getScenario() != null;
	}

	/**
	 * @param scenarioEnabled
	 *            the scenarioEnabled to set
	 */
	public void setScenarioEnabled(boolean scenarioEnabled) {
		if (scenarioEnabled) {
			if (model.getScenario() == null) {
				Scenario scenario = model.createScenario();
				model.setScenario(scenario);
			}
		} else {
			model.setScenario(null);
		}
	}

	/**
	 * @param drillThroughRows
	 *            the drillThroughRows to set
	 */
	protected void setDrillThroughRows(Integer drillThroughRows) {
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, Object> session = context.getExternalContext()
				.getSessionMap();

		if (drillThroughRows == null) {
			session.remove("drillThroughRows");
		} else {
			session.put("drillThroughRows", drillThroughRows);
		}
	}

	/**
	 * @return the renderSlicer
	 */
	public boolean getRenderSlicer() {
		return renderer.getRenderSlicer();
	}

	/**
	 * @param renderSlicer
	 *            the renderSlicer to set
	 */
	public void setRenderSlicer(boolean renderSlicer) {
		renderer.setRenderSlicer(renderSlicer);
	}

	/**
	 * @see org.pivot4j.QueryListener#queryExecuted(org.pivot4j.QueryEvent)
	 */
	@Override
	public void queryExecuted(QueryEvent e) {
		this.duration = e.getDuration();

		if (model.getCube() == null) {
			this.cubeName = null;
		} else {
			this.cubeName = model.getCube().getName();
		}

		this.lastError = null;
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

	class DrillThroughCommandImpl extends BasicDrillThroughCommand {

		/**
		 * @param renderer
		 */
		public DrillThroughCommandImpl(PivotRenderer<?> renderer) {
			super(renderer);
		}

		/**
		 * @see org.pivot4j.ui.command.BasicDrillThroughCommand#execute(org.pivot4j.PivotModel,
		 *      org.pivot4j.ui.command.UICommandParameters)
		 */
		@Override
		public ResultSet execute(PivotModel model,
				UICommandParameters parameters) {
			Cell cell = model.getCellSet().getCell(parameters.getCellOrdinal());

			drillThroughHandler.update(cell);

			RequestContext context = RequestContext.getCurrentInstance();
			context.update("drillthrough-form");
			context.execute("PF('drillThroughDialog').show()");

			return null;
		}
	}
}
