package org.pivot4j.analytics.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UISelectItem;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

import org.olap4j.OlapDataSource;
import org.pivot4j.analytics.config.Settings;
import org.pivot4j.analytics.datasource.CatalogInfo;
import org.pivot4j.analytics.datasource.ConnectionInfo;
import org.pivot4j.analytics.datasource.CubeInfo;
import org.pivot4j.analytics.datasource.DataSourceManager;
import org.pivot4j.analytics.state.ViewState;
import org.pivot4j.analytics.state.ViewStateHolder;
import org.pivot4j.impl.PivotModelImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name = "catalogChooser")
@ViewScoped
public class CatalogChooser implements Serializable {

	private static final long serialVersionUID = 9032548845357820921L;

	@ManagedProperty(value = "#{settings}")
	private Settings settings;

	@ManagedProperty(value = "#{dataSourceManager}")
	private DataSourceManager dataSourceManager;

	@ManagedProperty(value = "#{viewStateHolder}")
	private ViewStateHolder viewStateHolder;

	private List<UISelectItem> catalogItems;

	private List<UISelectItem> cubeItems;

	private String catalogName;

	private String cubeName;

	private String viewId;

	private boolean editable;

	public List<UISelectItem> getCatalogs() {
		if (catalogItems == null) {
			FacesContext context = FacesContext.getCurrentInstance();
			ResourceBundle messages = context.getApplication()
					.getResourceBundle(context, "msg");

			try {
				List<CatalogInfo> catalogs = dataSourceManager.getCatalogs();

				this.catalogItems = new ArrayList<UISelectItem>(catalogs.size());

				UISelectItem defaultItem = new UISelectItem();
				defaultItem.setItemLabel(messages
						.getString("message.catalog.chooser.default"));
				defaultItem.setItemValue("");

				catalogItems.add(defaultItem);

				for (CatalogInfo catalog : catalogs) {
					UISelectItem item = new UISelectItem();

					item.setItemValue(catalog.getName());
					item.setItemLabel(catalog.getLabel());
					item.setItemDescription(catalog.getDescription());

					catalogItems.add(item);
				}
			} catch (Exception e) {
				String title = messages.getString("error.catalogList.title");
				String msg = e.getMessage();

				context.addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_ERROR, title, msg));

				Logger log = LoggerFactory.getLogger(getClass());
				if (log.isErrorEnabled()) {
					log.error(msg, e);
				}
			}
		}

		return catalogItems;
	}

	public List<UISelectItem> getCubes() {
		if (cubeItems == null) {
			FacesContext context = FacesContext.getCurrentInstance();
			ResourceBundle messages = context.getApplication()
					.getResourceBundle(context, "msg");

			this.cubeItems = new ArrayList<UISelectItem>();

			UISelectItem defaultItem = new UISelectItem();
			defaultItem.setItemLabel(messages
					.getString("message.cubeList.default"));
			defaultItem.setItemValue("");

			cubeItems.add(defaultItem);

			if (catalogName != null) {
				try {
					List<CubeInfo> cubes = dataSourceManager
							.getCubes(catalogName);

					for (CubeInfo cube : cubes) {
						UISelectItem item = new UISelectItem();

						item.setItemValue(cube.getName());
						item.setItemLabel(cube.getLabel());
						item.setItemDescription(cube.getDescription());

						cubeItems.add(item);
					}
				} catch (Exception e) {
					ResourceBundle bundle = context.getApplication()
							.getResourceBundle(context, "msg");

					String title = bundle.getString("error.cubeList.title");
					String msg = e.getMessage();

					context.addMessage(null, new FacesMessage(
							FacesMessage.SEVERITY_ERROR, title, msg));

					Logger log = LoggerFactory.getLogger(getClass());
					if (log.isErrorEnabled()) {
						log.error(msg, e);
					}
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

	public boolean isNewReport() {
		if (viewId == null) {
			return true;
		}

		ViewState state = viewStateHolder.getState(viewId);

		return state == null || state.getConnectionInfo() == null;
	}

	public String proceed() {
		FacesContext context = FacesContext.getCurrentInstance();
		Flash flash = context.getExternalContext().getFlash();

		ConnectionInfo connectionInfo = new ConnectionInfo(catalogName,
				cubeName);

		ViewState state = viewStateHolder.getState(viewId);

		if (state == null) {
			state = viewStateHolder.createNewState(connectionInfo, viewId);
			viewStateHolder.registerState(state);
		} else {
			OlapDataSource dataSource = dataSourceManager
					.getDataSource(connectionInfo);
			state.setModel(new PivotModelImpl(dataSource));
			state.setConnectionInfo(connectionInfo);
		}

		flash.put("connectionInfo", connectionInfo);
		flash.put("viewId", viewId);

		StringBuilder builder = new StringBuilder();
		builder.append("view");
		builder.append("?faces-redirect=true");
		builder.append("&");
		builder.append(settings.getViewParameterName());
		builder.append("=");
		builder.append(viewId);

		return builder.toString();
	}

	/**
	 * @return the dataSourceManager
	 */
	public DataSourceManager getDataSourceManager() {
		return dataSourceManager;
	}

	/**
	 * @param dataSourceManager
	 *            the dataSourceManager to set
	 */
	public void setDataSourceManager(DataSourceManager dataSourceManager) {
		this.dataSourceManager = dataSourceManager;
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
