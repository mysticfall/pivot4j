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

	private boolean editorPaneVisible = false;

	private boolean navigatorPaneVisible = true;

	private LayoutOptions layoutOptions;

	private LayoutOptions workspaceLayoutOptions;

	/**
	 * @return the layoutOptions
	 */
	public LayoutOptions getLayoutOptions() {
		if (layoutOptions == null) {
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

			childCenterOptions.setNorthOptions(filterOptions);

			LayoutOptions editorOptions = new LayoutOptions();
			editorOptions.addOption("resizable", true);
			editorOptions.addOption("closable", true);
			editorOptions.addOption("slidable", true);
			editorOptions.addOption("size", 180);

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

	/**
	 * @return the workspaceLayoutOptions
	 */
	public LayoutOptions getWorkspaceLayoutOptions() {
		if (workspaceLayoutOptions == null) {
			this.workspaceLayoutOptions = new LayoutOptions();
			workspaceLayoutOptions.addOption("enableCursorHotkey", false);

			LayoutOptions toolbarOptions = new LayoutOptions();
			toolbarOptions.addOption("resizable", false);
			toolbarOptions.addOption("resizable", false);
			toolbarOptions.addOption("closable", false);

			workspaceLayoutOptions.setNorthOptions(toolbarOptions);

			LayoutOptions navigatorOptions = new LayoutOptions();
			navigatorOptions.addOption("resizable", true);
			navigatorOptions.addOption("closable", true);
			navigatorOptions.addOption("slidable", true);
			navigatorOptions.addOption("size", 200);

			workspaceLayoutOptions.setWestOptions(navigatorOptions);

			LayoutOptions contentOptions = new LayoutOptions();
			contentOptions.addOption("contentSelector", "#tab-panel");
			contentOptions.addOption("maskIframesOnResize", true);

			workspaceLayoutOptions.setCenterOptions(contentOptions);
		}

		return workspaceLayoutOptions;
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
	 * @return the editorPaneVisible
	 */
	public boolean isEditorPaneVisible() {
		return editorPaneVisible;
	}

	/**
	 * @param editorPaneVisible
	 *            the editorPaneVisible to set
	 */
	public void setEditorPaneVisible(boolean editorPaneVisible) {
		this.editorPaneVisible = editorPaneVisible;
	}

	/**
	 * @return the navigatorPaneVisible
	 */
	public boolean isNavigatorPaneVisible() {
		return navigatorPaneVisible;
	}

	/**
	 * @param navigatorPaneVisible
	 *            the navigatorPaneVisible to set
	 */
	public void setNavigatorPaneVisible(boolean navigatorPaneVisible) {
		this.navigatorPaneVisible = navigatorPaneVisible;
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
