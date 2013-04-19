package com.eyeq.pivot4j.pentaho.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.olap4j.OlapDataSource;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.RepositoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.impl.PivotModelImpl;
import com.eyeq.pivot4j.pentaho.datasource.PentahoDataSourceManager;
import com.eyeq.pivot4j.analytics.datasource.ConnectionMetadata;
import com.eyeq.pivot4j.analytics.state.ViewState;
import com.eyeq.pivot4j.analytics.state.ViewStateHolder;
import com.eyeq.pivot4j.analytics.ui.PrimeFacesPivotRenderer;

@ManagedBean(name = "pentahoFileHandler")
@SessionScoped
public class PentahoFileHandler {

	@ManagedProperty(value = "#{viewStateHolder}")
	private ViewStateHolder viewStateHolder;

	@ManagedProperty(value = "#{dataSourceManager}")
	private PentahoDataSourceManager dataSourceManager;

	private IPentahoSession session;

	private IUnifiedRepository repository;

	@PostConstruct
	protected void initialize() {
		this.session = PentahoSessionHolder.getSession();
		this.repository = PentahoSystem.get(IUnifiedRepository.class, session);
	}

	/**
	 * @param viewId
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws ConfigurationException
	 */
	public ViewState load(String viewId, RepositoryFile file)
			throws IOException, ClassNotFoundException, ConfigurationException {
		Logger logger = LoggerFactory.getLogger(getClass());
		if (logger.isInfoEnabled()) {
			logger.info("Saving report content to repository :");
			logger.info("	- viewId : " + viewId);
			logger.info("	- path : " + file.getPath());
			logger.info("	- fileName : " + file.getName());
		}

		SimpleRepositoryFileData data = repository.getDataForRead(file.getId(),
				SimpleRepositoryFileData.class);

		ViewState state;

		InputStream in = null;

		try {
			in = data.getInputStream();

			XMLConfiguration configuration = new XMLConfiguration();
			configuration.setRootElementName("report");
			configuration.setDelimiterParsingDisabled(true);
			configuration.load(in);

			if (logger.isDebugEnabled()) {
				StringWriter writer = new StringWriter();
				configuration.save(writer);
				writer.flush();
				writer.close();

				logger.debug("Loading report content :"
						+ System.getProperty("line.separator"));
				logger.debug(writer.getBuffer().toString());
			}

			ConnectionMetadata connectionInfo = new ConnectionMetadata();
			connectionInfo.restoreSettings(configuration);

			OlapDataSource dataSource = dataSourceManager
					.createDataSource(connectionInfo);

			PivotModel model = new PivotModelImpl(dataSource);
			model.restoreSettings(configuration);

			PrimeFacesPivotRenderer renderer = new PrimeFacesPivotRenderer(
					FacesContext.getCurrentInstance());
			renderer.restoreSettings(configuration);

			Serializable rendererState = renderer.saveState();

			state = new ViewState(viewId, viewId, connectionInfo, model, null);
			state.setReadOnly(true);
			state.setRendererState(rendererState);
		} finally {
			if (in != null) {
				in.close();
			}
		}

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

		String fileName = parameters.get("fileName");
		if (!fileName.endsWith(".pivot4j")) {
			fileName += ".pivot4j";
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

		ByteArrayOutputStream bout = new ByteArrayOutputStream();

		XMLConfiguration configuration = new XMLConfiguration();
		configuration.setRootElementName("report");
		configuration.setDelimiterParsingDisabled(true);

		ConnectionMetadata connectionInfo = state.getConnectionInfo();
		connectionInfo.saveSettings(configuration);

		PivotModel model = state.getModel();
		model.saveSettings(configuration);

		if (state.getRendererState() != null) {
			PrimeFacesPivotRenderer renderer = new PrimeFacesPivotRenderer(
					FacesContext.getCurrentInstance());

			renderer.restoreState(state.getRendererState());
			renderer.saveSettings(configuration);
		}

		if (logger.isDebugEnabled()) {
			StringWriter writer = new StringWriter();
			configuration.save(writer);
			writer.flush();
			writer.close();

			logger.debug("Saving new report :"
					+ System.getProperty("line.separator"));
			logger.debug(writer.getBuffer().toString());
		}

		configuration.save(bout);

		bout.flush();
		bout.close();

		String filePath = path + fileName;

		IRepositoryFileData data = new SimpleRepositoryFileData(
				new ByteArrayInputStream(bout.toByteArray()), "UTF-8",
				"text/xml");

		RepositoryFile file = repository.getFile(filePath);

		if (file == null) {
			RepositoryUtils utils = new RepositoryUtils(repository);
			utils.saveFile(filePath, data, true, overwrite, false, false, null);
		} else {
			repository.updateFile(file, data, null);
		}

		FacesContext context = FacesContext.getCurrentInstance();

		context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
				"Report Saved",
				"Current report has been successfully saved to : " + fileName));
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
	 * @return the session
	 */
	protected IPentahoSession getSession() {
		return session;
	}

	/**
	 * @return the repository
	 */
	protected IUnifiedRepository getRepository() {
		return repository;
	}
}
