package org.pivot4j.analytics.ui;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.ConfigurationException;
import org.pivot4j.analytics.config.Settings;
import org.pivot4j.analytics.datasource.DataSourceManager;
import org.pivot4j.analytics.repository.DataSourceNotFoundException;
import org.pivot4j.analytics.repository.ReportContent;
import org.pivot4j.analytics.repository.ReportFile;
import org.pivot4j.analytics.repository.ReportRepository;
import org.pivot4j.analytics.state.ViewState;
import org.pivot4j.analytics.state.ViewStateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name = "reportOpener")
@RequestScoped
public class ReportOpener {

	private Logger log = LoggerFactory.getLogger(getClass());

	@ManagedProperty(value = "#{settings}")
	private Settings settings;

	@ManagedProperty(value = "#{viewStateHolder}")
	private ViewStateHolder viewStateHolder;

	@ManagedProperty(value = "#{dataSourceManager}")
	private DataSourceManager dataSourceManager;

	@ManagedProperty(value = "#{reportRepository}")
	private ReportRepository reportRepository;

	private String fileId;

	private String path;

	private boolean embeded = false;

	public void load() throws IOException, ClassNotFoundException,
			ConfigurationException, DataSourceNotFoundException {
		FacesContext context = FacesContext.getCurrentInstance();

		HttpServletRequest request = (HttpServletRequest) context
				.getExternalContext().getRequest();

		ReportFile file = getReportFromRequest(request);

		if (file == null) {
			throw new FacesException("Unable to find requested report file.");
		}

		ViewState state = createViewWithRequest(request, file);

		if (state == null) {
			throw new FacesException("Unable to create a view state.");
		}

		ReportContent content = reportRepository.getReportContent(file);
		content.read(state, dataSourceManager, settings.getConfiguration());

		viewStateHolder.registerState(state);

		NavigationHandler navigationHandler = context.getApplication()
				.getNavigationHandler();

		String target = embeded ? "embed" : "view";

		String path = String.format("%s?faces-redirect=true&%s=%s", target,
				settings.getViewParameterName(), state.getId());

		navigationHandler.handleNavigation(context, null, path);
	}

	/**
	 * @param request
	 * @param file
	 * @return
	 */
	protected ViewState createViewWithRequest(HttpServletRequest request,
			ReportFile file) {
		String viewId = request.getParameter(settings.getViewParameterName());

		if (log.isInfoEnabled()) {
			log.info("Creating a view '{}' with a report: {}", viewId, file);
		}

		ViewState state;

		String name = file.getName();

		if (name.toLowerCase().endsWith(".pivot4j")) {
			name = name.substring(0, name.length() - 8);
		}

		if (viewId == null) {
			state = viewStateHolder.createNewState();
			state.setName(name);
		} else {
			state = new ViewState(viewId, name);
		}

		@SuppressWarnings("unchecked")
		Map<String, String[]> parameterMap = request.getParameterMap();

		Map<String, Object> parameters = new HashMap<String, Object>(
				parameterMap.size());

		for (String key : parameterMap.keySet()) {
			String[] values = parameterMap.get(key);

			if (values == null) {
				continue;
			}

			if (values.length == 1) {
				parameters.put(key, values[0]);
			} else {
				parameters.put(key, Arrays.asList(values));
			}
		}

		state.setFile(file);
		state.setParameters(parameters);
		state.setReadOnly(!file.canWrite());
		state.setEditable(!state.isReadOnly() && !embeded);

		return state;
	}

	/**
	 * @param request
	 * @return
	 * @throws IOException
	 */
	protected ReportFile getReportFromRequest(HttpServletRequest request)
			throws IOException {
		ReportFile file = null;

		if (fileId != null) {
			if (log.isDebugEnabled()) {
				log.debug("Opening report file with id: {}", fileId);
			}

			file = reportRepository.getFileById(fileId);
		} else if (path != null) {
			if (log.isDebugEnabled()) {
				log.debug("Opening report file with path: {}", path);
			}

			file = reportRepository.getFile(path);
		}

		return file;
	}

	/**
	 * @return the fileId
	 */
	public String getFileId() {
		return fileId;
	}

	/**
	 * @param fileId
	 *            the fileId to set
	 */
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the embeded
	 */
	public boolean isEmbeded() {
		return embeded;
	}

	/**
	 * @param embeded
	 *            the embeded to set
	 */
	public void setEmbeded(boolean embeded) {
		this.embeded = embeded;
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
	public ReportRepository getReportRepository() {
		return reportRepository;
	}

	/**
	 * @param reportRepository
	 *            the reportRepository to set
	 */
	public void setReportRepository(ReportRepository reportRepository) {
		this.reportRepository = reportRepository;
	}
}
