package com.eyeq.pivot4j.analytics.ui;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UISelectItem;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapDataSource;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Schema;
import org.primefaces.component.panelgrid.PanelGrid;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.ModelChangeEvent;
import com.eyeq.pivot4j.ModelChangeListener;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.QueryEvent;
import com.eyeq.pivot4j.QueryListener;
import com.eyeq.pivot4j.analytics.datasource.ConnectionInfo;
import com.eyeq.pivot4j.impl.PivotModelImpl;
import com.eyeq.pivot4j.transform.NonEmpty;
import com.eyeq.pivot4j.transform.SwapAxes;
import com.eyeq.pivot4j.ui.PivotUIRenderer;
import com.eyeq.pivot4j.ui.command.BasicDrillThroughCommand;
import com.eyeq.pivot4j.ui.command.CellCommand;
import com.eyeq.pivot4j.ui.command.CellParameters;
import com.eyeq.pivot4j.ui.command.DrillDownCommand;
import com.eyeq.pivot4j.util.OlapUtils;

@ManagedBean(name = "pivotGridHandler")
@RequestScoped
public class PivotGridHandler implements QueryListener, ModelChangeListener {

	@ManagedProperty(value = "#{pivotStateManager}")
	private PivotStateManager stateManager;

	@ManagedProperty(value = "#{navigatorHandler}")
	private NavigatorHandler navigator;

	@ManagedProperty(value = "#{drillThroughHandler}")
	private DrillThroughHandler drillThroughHandler;

	private PivotModel model;

	private PrimeFacesPivotRenderer renderer;

	private List<UISelectItem> cubeItems;

	private String cubeName;

	private String currentMdx;

	private Long duration;

	@PostConstruct
	protected void initialize() {
		this.model = stateManager.getModel();

		if (model != null) {
			model.addQueryListener(this);
			model.addModelChangeListener(this);

			if (model.isInitialized()) {
				this.cubeName = model.getCube().getName();
			} else {
				ConnectionInfo connectionInfo = stateManager
						.getConnectionInfo();

				if (connectionInfo != null) {
					if (!model.isInitialized()) {
						this.cubeName = connectionInfo.getCubeName();

						onCubeChange();
					}
				}
			}
		}

		FacesContext context = FacesContext.getCurrentInstance();

		this.renderer = new PrimeFacesPivotRenderer(context);

		Serializable state = stateManager.getRendererState();

		if (state == null) {
			renderer.setShowDimensionTitle(true);
			renderer.setShowParentMembers(false);
			renderer.setHideSpans(false);
			renderer.setDrillDownMode(DrillDownCommand.MODE_POSITION);
			renderer.setEnableDrillThrough(false);
		} else {
			renderer.restoreState(state);
		}

		boolean readOnly = stateManager.isReadOnly();

		renderer.setEnableColumnDrillDown(!readOnly);
		renderer.setEnableRowDrillDown(!readOnly);
		renderer.setEnableSort(!readOnly);

		renderer.addCommand(new DrillThroughCommandImpl(renderer));
	}

	@PreDestroy
	protected void destroy() {
		if (model != null) {
			model.removeQueryListener(this);
			model.removeModelChangeListener(this);
		}
	}

	/**
	 * @return the renderer
	 */
	public PrimeFacesPivotRenderer getRenderer() {
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
	 * @return the pivotGrid
	 */
	public PanelGrid getPivotGrid() {
		return renderer.getComponent();
	}

	/**
	 * @param pivotGrid
	 *            the pivotGrid to set
	 */
	public void setPivotGrid(PanelGrid pivotGrid) {
		renderer.setComponent(pivotGrid);
	}

	/**
	 * @return the duration
	 */
	public Long getDuration() {
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
					UISelectItem item = new UISelectItem();
					item.setItemLabel(cube.getCaption());
					item.setItemValue(cube.getName());

					cubeItems.add(item);
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
			String mdx;

			if (OlapUtils.isEmptySetSupported(model.getMetadata())) {
				mdx = String.format(
						"select {} on columns, {} on rows from [%s]", cubeName);
			} else {
				mdx = String.format("select from [%s]", cubeName);
			}

			model.setMdx(mdx);

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

	public void render() {
		if (model != null && model.isInitialized()) {
			FacesContext context = FacesContext.getCurrentInstance();

			Map<String, String> parameters = context.getExternalContext()
					.getRequestParameterMap();

			if ("true".equals(parameters.get("skipRender"))) {
				return;
			}

			renderer.render(model);

			stateManager.setRendererState(renderer.saveState());
		}
	}

	public void executeCommand() {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, String> requestParameters = context.getExternalContext()
				.getRequestParameterMap();

		CellParameters parameters = new CellParameters();

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

		CellCommand<?> command = renderer.getCommand(requestParameters
				.get("command"));
		command.execute(model, parameters);
	}

	public void executeMdx() {
		String oldMdx = model.getCurrentMdx();

		try {
			model.setMdx(currentMdx);

			if (!model.isInitialized()) {
				model.initialize();
			}
		} catch (Exception e) {
			FacesContext context = FacesContext.getCurrentInstance();

			ResourceBundle bundle = context.getApplication().getResourceBundle(
					context, "msg");

			String title = bundle.getString("error.execute.title");

			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_ERROR, title, e.getMessage()));

			if (oldMdx != null) {
				model.setMdx(oldMdx);
			}
		}
	}

	/**
	 * @return the currentMdx
	 */
	public String getCurrentMdx() {
		if (model == null) {
			return null;
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
	 * @see com.eyeq.pivot4j.QueryListener#queryExecuted(com.eyeq.pivot4j.QueryEvent)
	 */
	@Override
	public void queryExecuted(QueryEvent e) {
		this.duration = e.getDuration();

		if (model.getCube() == null) {
			this.cubeName = null;
		} else {
			this.cubeName = model.getCube().getName();
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ModelChangeListener#modelInitialized(com.eyeq.pivot4j.ModelChangeEvent)
	 */
	@Override
	public void modelInitialized(ModelChangeEvent e) {
	}

	/**
	 * @see com.eyeq.pivot4j.ModelChangeListener#modelDestroyed(com.eyeq.pivot4j.ModelChangeEvent)
	 */
	@Override
	public void modelDestroyed(ModelChangeEvent e) {
	}

	/**
	 * @see com.eyeq.pivot4j.ModelChangeListener#modelChanged(com.eyeq.pivot4j.ModelChangeEvent)
	 */
	@Override
	public void modelChanged(ModelChangeEvent e) {
	}

	/**
	 * @see com.eyeq.pivot4j.ModelChangeListener#structureChanged(com.eyeq.pivot4j.ModelChangeEvent)
	 */
	@Override
	public void structureChanged(ModelChangeEvent e) {
		render();
	}

	class DrillThroughCommandImpl extends BasicDrillThroughCommand {

		/**
		 * @param renderer
		 */
		public DrillThroughCommandImpl(PivotUIRenderer renderer) {
			super(renderer);
		}

		/**
		 * @see com.eyeq.pivot4j.ui.command.BasicDrillThroughCommand#execute(com.eyeq.pivot4j.PivotModel,
		 *      com.eyeq.pivot4j.ui.command.CellParameters)
		 */
		@Override
		public ResultSet execute(PivotModel model, CellParameters parameters) {
			Cell cell = model.getCellSet().getCell(parameters.getCellOrdinal());

			drillThroughHandler.update(cell);

			RequestContext context = RequestContext.getCurrentInstance();
			context.update("drillthrough-form");
			context.execute("drillThroughDialog.show()");

			return null;
		}
	}
}
