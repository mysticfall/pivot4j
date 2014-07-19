package org.pivot4j.pentaho.ui;

import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.ConfigurationException;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pivot4j.analytics.repository.ReportContent;
import org.pivot4j.analytics.repository.ReportFile;
import org.pivot4j.analytics.state.ViewState;
import org.pivot4j.analytics.ui.ReportOpener;
import org.pivot4j.pentaho.repository.PentahoReportFile;
import org.pivot4j.pentaho.repository.PentahoReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PentahoReportOpener extends ReportOpener {

	/**
	 * @throws IOException
	 * @see org.pivot4j.analytics.ui.ReportOpener#getReportFromRequest(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected ReportFile getReportFromRequest(HttpServletRequest request)
			throws IOException {
		ReportFile file = null;

		RepositoryFile repositoryFile = (RepositoryFile) request
				.getAttribute("file");

		if (repositoryFile == null) {
			file = super.getReportFromRequest(request);
		} else {
			file = getReportRepository().getFile(repositoryFile.getPath());
		}

		return file;
	}

	/**
	 * @see org.pivot4j.analytics.ui.ReportOpener#createViewWithRequest(javax.servlet.http.HttpServletRequest,
	 *      org.pivot4j.analytics.repository.ReportFile)
	 */
	@Override
	protected ViewState createViewWithRequest(HttpServletRequest request,
			ReportFile file) {
		ViewState state = super.createViewWithRequest(request, file);

		PentahoReportFile pentahoFile = (PentahoReportFile) file;

		state.setName(pentahoFile.getTitle());
		state.setReadOnly(!file.canWrite());
		state.setEditable(!state.isReadOnly()
				&& !"false".equalsIgnoreCase(request.getParameter("editable")));

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

		ViewState state = getViewStateHolder().getState(viewId);

		ReportContent content = new ReportContent(state);

		String filePath = path + fileName;

		PentahoReportRepository repository = (PentahoReportRepository) getReportRepository();

		ReportFile file = repository.getFile(filePath);

		if (file == null) {
			ReportFile parent = repository.getFile(path);
			file = repository.createFile(parent, fileName, content, overwrite);
		} else {
			repository.setReportContent(file, content);
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
}
