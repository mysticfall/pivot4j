package com.eyeq.pivot4j.pentaho.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.NavigationHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UISelectItem;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.ConfigurationException;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCube;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianSchema;

import com.eyeq.pivot4j.pentaho.datasource.PentahoDataSourceManager;
import com.eyeq.pivot4j.analytics.datasource.ConnectionMetadata;
import com.eyeq.pivot4j.analytics.state.ViewState;
import com.eyeq.pivot4j.analytics.state.ViewStateHolder;

@ManagedBean(name = "pentahoCatalogChooser")
@ViewScoped
public class PentahoCatalogChooser {

	@ManagedProperty(value = "#{dataSourceManager}")
	private PentahoDataSourceManager dataSourceManager;

	@ManagedProperty(value = "#{viewStateHolder}")
	private ViewStateHolder viewStateHolder;

	@ManagedProperty(value = "#{pentahoFileHandler}")
	private PentahoFileHandler fileHandler;

	private List<UISelectItem> catalogItems;

	private List<UISelectItem> cubeItems;

	private String catalogName;

	private String cubeName;

	private String viewId;

	private boolean editable;

	public List<UISelectItem> getCatalogs() {
		if (catalogItems == null) {
			List<MondrianCatalog> catalogs = dataSourceManager.getCatalogs();

			this.catalogItems = new ArrayList<UISelectItem>(catalogs.size());

			UISelectItem defaultItem = new UISelectItem();
			defaultItem.setItemLabel("---- Please select a catalog ----");
			defaultItem.setItemValue("");

			catalogItems.add(defaultItem);

			for (MondrianCatalog catalog : catalogs) {
				UISelectItem item = new UISelectItem();

				item.setItemLabel(catalog.getName());
				item.setItemValue(catalog.getDefinition());
				catalogItems.add(item);
			}
		}

		return catalogItems;
	}

	public List<UISelectItem> getCubes() {
		if (cubeItems == null) {
			this.cubeItems = new ArrayList<UISelectItem>();

			UISelectItem defaultItem = new UISelectItem();
			defaultItem.setItemLabel("---- Please select a cube ----");
			defaultItem.setItemValue("");

			cubeItems.add(defaultItem);

			MondrianCatalog catalog = getDataSourceManager().getCatalog(
					catalogName);

			if (catalog != null) {
				MondrianSchema schema = catalog.getSchema();

				List<MondrianCube> cubes = schema.getCubes();

				for (MondrianCube cube : cubes) {
					UISelectItem item = new UISelectItem();
					item.setItemValue(cube.getId());
					item.setItemLabel(cube.getName());

					cubeItems.add(item);
				}
			}
		}

		return cubeItems;
	}

	public void onCatalogChanged() {
		this.cubeItems = null;

		if (getCubes().size() > 1) {
			this.cubeName = (String) getCubes().get(1).getItemValue();
		} else {
			this.cubeName = null;
		}
	}

	public boolean isNew() {
		return viewId == null || viewStateHolder.getState(viewId) == null;
	}

	public void checkState() throws IOException, ClassNotFoundException,
			ConfigurationException {
		if (viewId != null) {
			FacesContext facesContext = FacesContext.getCurrentInstance();

			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();

			RepositoryFile file = (RepositoryFile) request.getAttribute("file");

			ViewState state;

			if (file == null) {
				state = viewStateHolder.getState(viewId);
			} else {
				state = fileHandler.load(viewId, file);

				if (state != null) {
					state.setReadOnly(!editable);
					viewStateHolder.registerState(state);
				}
			}

			if (state != null) {
				ConnectionMetadata connectionInfo = state.getConnectionInfo();

				this.catalogName = connectionInfo.getCatalogName();
				this.cubeName = connectionInfo.getCubeName();

				NavigationHandler navigationHandler = facesContext
						.getApplication().getNavigationHandler();
				navigationHandler.handleNavigation(facesContext, null,
						"view?faces-redirect=true&ts=" + viewId);
			}
		}
	}

	public String proceed() {
		FacesContext context = FacesContext.getCurrentInstance();
		Flash flash = context.getExternalContext().getFlash();

		ConnectionMetadata connectionInfo = new ConnectionMetadata(null,
				catalogName, cubeName);

		ViewState state = viewStateHolder
				.createNewState(connectionInfo, viewId);
		viewStateHolder.registerState(state);

		flash.put("connectionInfo", connectionInfo);
		flash.put("viewId", viewId);

		return "view?faces-redirect=true&ts=" + viewId;
	}

	/**
	 * @return the dataSourceManager
	 */
	public PentahoDataSourceManager getDataSourceManager() {
		return dataSourceManager;
	}

	/**
	 * @param dataSourceManager
	 *            the dataSourceManager to set
	 */
	public void setDataSourceManager(PentahoDataSourceManager dataSourceManager) {
		this.dataSourceManager = dataSourceManager;
	}

	/**
	 * @return the fileHandler
	 */
	public PentahoFileHandler getFileHandler() {
		return fileHandler;
	}

	/**
	 * @param fileHandler
	 *            the fileHandler to set
	 */
	public void setFileHandler(PentahoFileHandler fileHandler) {
		this.fileHandler = fileHandler;
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
	 * @return the catalogName
	 */
	public String getCatalogName() {
		return catalogName;
	}

	/**
	 * @param catalogName
	 *            the catalogName to set
	 */
	public void setCatalogName(String catalogName) {
		this.catalogName = catalogName;
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
	 * @return the viewId
	 */
	public String getViewId() {
		return viewId;
	}

	/**
	 * @param viewId
	 *            the viewId to set
	 */
	public void setViewId(String viewId) {
		this.viewId = viewId;
	}

	/**
	 * @return the editable
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * @param editable
	 *            the editable to set
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
}
