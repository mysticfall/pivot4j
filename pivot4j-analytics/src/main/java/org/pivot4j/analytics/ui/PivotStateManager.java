package org.pivot4j.analytics.ui;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.FacesException;
import javax.faces.application.ProjectStage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

import org.pivot4j.PivotModel;
import org.pivot4j.analytics.config.Settings;
import org.pivot4j.analytics.datasource.ConnectionInfo;
import org.pivot4j.analytics.state.ViewState;
import org.pivot4j.analytics.state.ViewStateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name = "pivotStateManager")
@ViewScoped
public class PivotStateManager implements Serializable {

	private static final long serialVersionUID = -146698046524588064L;

	private Logger log = LoggerFactory.getLogger(getClass());

	@ManagedProperty(value = "#{settings}")
	private Settings settings;

	@ManagedProperty(value = "#{viewStateHolder}")
	private ViewStateHolder viewStateHolder;

	private String viewId;

	@PostConstruct
	protected void initialize() {
		FacesContext context = FacesContext.getCurrentInstance();

		ExternalContext externalContext = context.getExternalContext();
		Flash flash = externalContext.getFlash();

		Map<String, String> parameters = externalContext
				.getRequestParameterMap();

		this.viewId = parameters.get(settings.getViewParameterName());

		if (viewId == null) {
			this.viewId = (String) flash.get("viewId");
		}

		ViewState state = null;

		if (viewId != null) {
			state = viewStateHolder.getState(viewId);
		}

		if (state == null) {
			ProjectStage stage = context.getApplication().getProjectStage();

			if (stage == ProjectStage.UnitTest) {
				state = viewStateHolder.createNewState();
				viewStateHolder.registerState(state);

				this.viewId = state.getId();
			} else {
				throw new FacesException("No view state data is available : "
						+ viewId);
			}
		}

		if (log.isInfoEnabled()) {
			log.info("Using an existing view state : {}", viewId);
		}
	}

	@PreDestroy
	public void destroy() {
		viewStateHolder.unregisterState(viewId);
	}

	/**
	 * @return the viewId
	 */
	public String getViewId() {
		return viewId;
	}

	public ViewState getState() {
		return viewStateHolder.getState(viewId);
	}

	/**
	 * @return the model
	 */
	public PivotModel getModel() {
		ViewState state = getState();
		if (state == null) {
			return null;
		}

		return state.getModel();
	}

	/**
	 * @return the readOnly
	 */
	public boolean isReadOnly() {
		ViewState state = getState();
		if (state == null) {
			return true;
		}

		return state.isReadOnly();
	}

	/**
	 * @return the dirty
	 */
	public boolean isDirty() {
		ViewState state = getState();
		if (state == null) {
			return false;
		}

		return state.isDirty();
	}

	/**
	 * @return the settings
	 */
	public Settings getSettings() {
		return settings;
	}

	/**
	 * @param settings
	 *            the settings to set
	 */
	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	/**
	 * @return the viewStateHolder
	 */
	public ViewStateHolder getViewStateHolder() {
		return viewStateHolder;
	}

	/**
	 * @param viewStateHolder
	 *            the viewStateHolder to set
	 */
	public void setViewStateHolder(ViewStateHolder viewStateHolder) {
		this.viewStateHolder = viewStateHolder;
	}

	/**
	 * @return the rendererState
	 */
	public Serializable getRendererState() {
		ViewState state = getState();
		if (state == null) {
			return null;
		}

		return state.getRendererState();
	}

	/**
	 * @param rendererState
	 *            the rendererState to set
	 */
	public void setRendererState(Serializable rendererState) {
		ViewState state = getState();
		if (state == null) {
			return;
		}

		state.setRendererState(rendererState);
	}

	/**
	 * @return the chartState
	 */
	public Serializable getChartState() {
		ViewState state = getState();
		if (state == null) {
			return null;
		}

		return state.getChartState();
	}

	/**
	 * @param chartState
	 *            the chartState to set
	 */
	public void setChartState(Serializable chartState) {
		ViewState state = getState();
		if (state == null) {
			return;
		}

		state.setChartState(chartState);
	}

	public ConnectionInfo getConnectionInfo() {
		ViewState state = getState();
		if (state == null) {
			return null;
		}

		return state.getConnectionInfo();
	}

	public void keepAlive() {
		viewStateHolder.keepAlive(viewId);
	}
}
