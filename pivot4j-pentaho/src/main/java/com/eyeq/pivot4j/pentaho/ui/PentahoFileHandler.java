package com.eyeq.pivot4j.pentaho.ui;

import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.ConfigurationException;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.analytics.datasource.DataSourceManager;
import com.eyeq.pivot4j.analytics.repository.DataSourceNotFoundException;
import com.eyeq.pivot4j.analytics.repository.ReportContent;
import com.eyeq.pivot4j.analytics.repository.ReportFile;
import com.eyeq.pivot4j.analytics.state.ViewState;
import com.eyeq.pivot4j.analytics.state.ViewStateHolder;
import com.eyeq.pivot4j.pentaho.repository.PentahoReportFile;
import com.eyeq.pivot4j.pentaho.repository.PentahoReportRepository;

@ManagedBean(name = "pentahoFileHandler")
@SessionScoped
public class PentahoFileHandler {

	@ManagedProperty(value = "#{viewStateHolder}")
	private ViewStateHolder viewStateHolder;

	@ManagedProperty(value = "#{dataSourceManager}")
	private DataSourceManager dataSourceManager;

	@ManagedProperty(value = "#{reportRepository}")
	private PentahoReportRepository reportRepository;

	public void load() throws IOException, ClassNotFoundException,
			ConfigurationException, DataSourceNotFoundException {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();

		Map<String, String> parameters = externalContext
				.getRequestParameterMap();

		String viewId = parameters.get("ts");
		String editable = parameters.get("editable");

		HttpServletRequest request = (HttpServletRequest) context
				.getExternalContext().getRequest();

		RepositoryFile file = (RepositoryFile) request.getAttribute("file");

		ViewState state;

		if (file == null) {
			state = viewStateHolder.getState(viewId);
		} else {
			state = load(viewId, file);

			if (state != null) {
				state.setReadOnly("false".equalsIgnoreCase(editable));
				viewStateHolder.registerState(state);
			}
		}

		if (state != null) {
			NavigationHandler navigationHandler = context.getApplication()
					.getNavigationHandler();
			navigationHandler.handleNavigation(context, null,
					"view?faces-redirect=true&ts=" + state.getId());
		}
	}

	/**
	 * @param viewId
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws ConfigurationException
	 * @throws DataSourceNotFoundException
	 */
	public ViewState load(String viewId, RepositoryFile file)
			throws IOException, ClassNotFoundException, ConfigurationException,
			DataSourceNotFoundException {
		Logger logger = LoggerFactory.getLogger(getClass());
		if (logger.isInfoEnabled()) {
			logger.info("Saving report content to repository :");
			logger.info("	- viewId : " + viewId);
			logger.info("	- path : " + file.getPath());
			logger.info("	- fileName : " + file.getName());
		}

		ReportFile report = reportRepository.getFile(file.getPath());
		ReportContent content = reportRepository.getReportContent(report);

		ViewState state;

		if (viewId == null) {
			state = viewStateHolder.createNewState();
			state.setName(file.getTitle());
		} else {
			state = new ViewState(viewId, file.getTitle());
		}

		content.read(state, dataSourceManager);

		return state;
	}

	/**
	 * @throws PentahoAccessControlException
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public void save() throws PentahoAccessControlException, IOException,
			ConfigurationException {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, String> parameters = context.getExternalContext()
				.getRequestParameterMap();
		String viewId = parameters.get("viewId");

		String extension = "." + PentahoReportFile.DEFAULT_EXTENSION;

		String fileName = parameters.get("fileName");
		if (!fileName.endsWith(extension)) {
			fileName += extension;
		}

		String path = parameters.get("path");

		if (path.endsWith(fileName)) {
			path = path.substring(0, path.length() - fileName.length() - 1);
		}

		if (!path.endsWith(RepositoryFile.SEPARATOR)) {
			path += RepositoryFile.SEPARATOR;
		}

		boolean overwrite = Boolean.parseBoolean(parameters.get("overwrite"));

		save(viewId, path, fileName, overwrite);
	}

	/**
	 * @param viewId
	 * @param path
	 * @param fileName
	 * @param overwrite
	 * @throws PentahoAccessControlException
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public void save(String viewId, String path, String fileName,
			boolean overwrite) throws PentahoAccessControlException,
			IOException, ConfigurationException {
		Logger logger = LoggerFactory.getLogger(getClass());
		if (logger.isInfoEnabled()) {
			logger.info("Saving report content to repository :");
			logger.info("	- viewId : " + viewId);
			logger.info("	- path : " + path);
			logger.info("	- fileName : " + fileName);
			logger.info("	- overwrite : " + overwrite);
		}

		ViewState state = viewStateHolder.getState(viewId);

		ReportContent content = new ReportContent(state);

		String filePath = path + fileName;

		ReportFile file = reportRepository.getFile(filePath);

		if (file == null) {
			ReportFile parent = reportRepository.getFile(path);
			file = reportRepository.createFile(parent, fileName, content,
					overwrite);
		} else {
			reportRepository.setReportContent(file, content);
		}

		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(
				context, "msg");

		String title = bundle.getString("message.save.report.title");
		String message = bundle.getString("message.saveAs.report.message")
				+ file.getPath();

		context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
				title, message));
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
	 * @return the reportRepository
	 */
	public PentahoReportRepository getReportRepository() {
		return reportRepository;
	}

	/**
	 * @param reportRepository
	 *            the reportRepository to set
	 */
	public void setReportRepository(PentahoReportRepository reportRepository) {
		this.reportRepository = reportRepository;
	}
}
