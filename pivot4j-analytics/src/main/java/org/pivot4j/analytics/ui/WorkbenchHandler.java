package org.pivot4j.analytics.ui;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.pivot4j.PivotModel;
import org.pivot4j.analytics.config.Settings;
import org.pivot4j.analytics.state.ViewState;
import org.primefaces.extensions.model.layout.LayoutOptions;

@ManagedBean(name = "workbenchHandler")
@RequestScoped
public class WorkbenchHandler {

	@ManagedProperty(value = "#{settings}")
	private Settings settings;

	@ManagedProperty(value = "#{repositoryHandler}")
	private RepositoryHandler repositoryHandler;

	private LayoutOptions layoutOptions;

	/**
	 * @return the layoutOptions
	 */
	public LayoutOptions getLayoutOptions() {
		if (layoutOptions == null) {
			this.layoutOptions = new LayoutOptions();
			layoutOptions.addOption("enableCursorHotkey", false);

			LayoutOptions toolbarOptions = new LayoutOptions();
			toolbarOptions.addOption("resizable", false);
			toolbarOptions.addOption("resizable", false);
			toolbarOptions.addOption("closable", false);

			layoutOptions.setNorthOptions(toolbarOptions);

			LayoutOptions navigatorOptions = new LayoutOptions();
			navigatorOptions.addOption("resizable", true);
			navigatorOptions.addOption("closable", true);
			navigatorOptions.addOption("slidable", true);
			navigatorOptions.addOption("size", 200);

			layoutOptions.setWestOptions(navigatorOptions);

			LayoutOptions contentOptions = new LayoutOptions();
			contentOptions.addOption("contentSelector", "#tab-panel");
			contentOptions.addOption("maskIframesOnResize", true);

			layoutOptions.setCenterOptions(contentOptions);
		}

		return layoutOptions;
	}

	/**
	 * @return the theme
	 */
	public String getTheme() {
		ExternalContext context = FacesContext.getCurrentInstance()
				.getExternalContext();
		String theme = (String) context.getSessionMap().get("ui-theme");

		if (theme == null) {
			theme = settings.getTheme();
		}

		return theme;
	}

	/**
	 * @param theme
	 *            the theme to set
	 */
	public void setTheme(String theme) {
		ExternalContext context = FacesContext.getCurrentInstance()
				.getExternalContext();

		if (theme == null) {
			context.getSessionMap().remove("ui-theme");
		} else {
			context.getSessionMap().put("ui-theme", theme);
		}
	}

	public boolean isDeleteEnabled() {
		ViewState viewState = repositoryHandler.getActiveView();
		return viewState != null && viewState.getFile() != null;
	}

	public boolean isSaveEnabled() {
		ViewState viewState = repositoryHandler.getActiveView();
		return viewState != null && viewState.isDirty();
	}

	public boolean isViewActive() {
		ViewState viewState = repositoryHandler.getActiveView();
		return viewState != null;
	}

	public boolean isViewValid() {
		ViewState viewState = repositoryHandler.getActiveView();
		if (viewState == null) {
			return false;
		}

		PivotModel model = viewState.getModel();

		return model != null && model.isInitialized();
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
	 * @return the repositoryHandler
	 */
	public RepositoryHandler getRepositoryHandler() {
		return repositoryHandler;
	}

	/**
	 * @param repositoryHandler
	 *            the repositoryHandler to set
	 */
	public void setRepositoryHandler(RepositoryHandler repositoryHandler) {
		this.repositoryHandler = repositoryHandler;
	}
}
