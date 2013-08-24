package com.eyeq.pivot4j.pentaho.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.IOUtils;
import org.olap4j.OlapDataSource;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.analytics.config.Settings;
import com.eyeq.pivot4j.analytics.datasource.ConnectionInfo;
import com.eyeq.pivot4j.analytics.datasource.CubeInfo;
import com.eyeq.pivot4j.analytics.datasource.DataSourceManager;
import com.eyeq.pivot4j.analytics.repository.DataSourceNotFoundException;
import com.eyeq.pivot4j.analytics.repository.ReportContent;
import com.eyeq.pivot4j.analytics.repository.ReportFile;
import com.eyeq.pivot4j.analytics.repository.ReportRepository;
import com.eyeq.pivot4j.analytics.repository.RepositoryFileFilter;
import com.eyeq.pivot4j.analytics.state.ViewState;
import com.eyeq.pivot4j.analytics.state.ViewStateHolder;
import com.eyeq.pivot4j.analytics.ui.PrimeFacesPivotRenderer;
import com.eyeq.pivot4j.analytics.ui.navigator.RepositoryNode;
import com.eyeq.pivot4j.impl.PivotModelImpl;
import com.eyeq.pivot4j.mdx.MdxParser;
import com.eyeq.pivot4j.mdx.MdxStatement;
import com.eyeq.pivot4j.mdx.impl.MdxParserImpl;

@ManagedBean(name = "migrationHandler")
@ViewScoped
public class MigrationHandler {

	private Logger log = LoggerFactory.getLogger(getClass());

	@ManagedProperty(value = "#{dataSourceManager}")
	private DataSourceManager dataSourceManager;

	@ManagedProperty(value = "#{settings}")
	private Settings settings;

	@ManagedProperty(value = "#{reportRepository}")
	private ReportRepository repository;

	@ManagedProperty(value = "#{viewStateHolder}")
	private ViewStateHolder viewStateHolder;

	private TreeNode rootNode;

	private TreeNode selection;

	private String viewId;

	private List<Entry> convertedFiles = new LinkedList<Entry>();

	private Entry selectedFile;

	private boolean migrationDone = false;

	private RepositoryFileFilter fileFilter = new MigrationTargetFilter();

	public boolean isOkButtonEnabled() {
		if (migrationDone) {
			return selectedFile != null && selectedFile.getError() == null;
		} else {
			return selection != null;
		}
	}

	public String proceed() {
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(
				context, "plugin_msg");

		String navigation = null;

		try {
			if (migrationDone) {
				navigation = open(selectedFile.getResult(), false);
			} else {
				convertedFiles.clear();

				ReportFile target;

				if (selection instanceof RepositoryNode) {
					target = ((RepositoryNode) selection).getObject();
				} else {
					target = repository.getRoot();
				}

				if (target.isDirectory()) {
					convertFiles(target);
				} else {
					navigation = open(target, true);
				}

				RequestContext.getCurrentInstance().execute(
						"if (parent) parent.mantle_refreshRepository()");

				this.migrationDone = true;
			}
		} catch (Exception e) {
			String title = bundle.getString("title.migrate.error");
			String message = bundle.getString("message.migrate.error") + e;

			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_ERROR, title, message));

			if (log.isErrorEnabled()) {
				log.error(message, e);
			}
		}

		return navigation;
	}

	public String getSummary() {
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(
				context, "plugin_msg");

		int count = 0;

		for (Entry entry : convertedFiles) {
			if (entry.getError() == null) {
				count++;
			}
		}

		String message = String.format(
				bundle.getString("message.migrate.success"), count);

		return message;
	}

	/**
	 * @param selectedFile
	 * @param convert
	 * @return
	 * @throws IOException
	 * @throws ConfigurationException
	 * @throws DataSourceNotFoundException
	 * @throws InvalidConnectionInfoException
	 */
	public String open(ReportFile file, boolean convert) throws IOException,
			ConfigurationException, DataSourceNotFoundException,
			InvalidConnectionInfoException {
		FacesContext context = FacesContext.getCurrentInstance();

		ViewState state;

		if (convert) {
			state = convertFile(file);
			state.setDirty(true);
		} else {
			state = new ViewState(viewId, file.getName());
			state.setFile(file);

			ReportContent content = repository.getReportContent(file);
			content.read(state, dataSourceManager);
		}

		state.getModel().initialize();

		viewStateHolder.registerState(state);

		Flash flash = context.getExternalContext().getFlash();

		flash.put("connectionInfo", state.getConnectionInfo());
		flash.put("viewId", state.getId());

		StringBuilder builder = new StringBuilder();
		builder.append("view");
		builder.append("?faces-redirect=true");
		builder.append("&");
		builder.append(settings.getViewParameterName());
		builder.append("=");
		builder.append(state.getId());

		return builder.toString();
	}

	/**
	 * @param parent
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	protected void convertFiles(ReportFile parent) throws IOException,
			ConfigurationException {
		List<ReportFile> files = repository.getFiles(parent, fileFilter);

		for (ReportFile file : files) {
			if (file.isDirectory()) {
				convertFiles(file);
			} else {
				String error = null;

				ReportFile result = null;

				FacesContext context = FacesContext.getCurrentInstance();
				ResourceBundle bundle = context.getApplication()
						.getResourceBundle(context, "plugin_msg");

				try {
					ViewState state = convertFile(file);

					String name = file.getName().substring(
							0,
							file.getName().length()
									- file.getExtension().length())
							+ "pivot4j";

					result = repository.createFile(parent, name,
							new ReportContent(state));
				} catch (InvalidConnectionInfoException e) {
					error = bundle
							.getString("message.migrate.error.dataSource");
				} catch (Exception e) {
					error = bundle.getString("message.migrate.error") + e;

					if (log.isErrorEnabled()) {
						log.error(error, e);
					}
				}

				convertedFiles.add(new Entry(file, result, error));
			}
		}
	}

	/**
	 * @param selectedFile
	 * @return
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	protected ViewState convertFile(ReportFile file)
			throws InvalidConnectionInfoException, ConfigurationException,
			IOException {
		if (log.isDebugEnabled()) {
			log.debug("Migrating JPivot report : " + file);
		}

		InputStream in = null;

		try {
			in = repository.readContent(file);

			XMLConfiguration config = new XMLConfiguration();
			config.setRootElementName("action-sequence");
			config.setDelimiterParsingDisabled(true);
			config.load(in);

			return convertFile(config);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * @param config
	 * @return
	 * @throws InvalidConnectionInfoException
	 */
	protected ViewState convertFile(HierarchicalConfiguration config)
			throws InvalidConnectionInfoException {
		HierarchicalConfiguration report = config
				.configurationAt("actions.action-definition.component-definition");

		String title = report.getString("title");
		String query = report.getString("query");
		String cube = report.getString("cube");
		String catalog = report.getString("model");

		if (log.isDebugEnabled()) {
			log.debug("	- title : " + title);
			log.debug("	- query : " + query);
			log.debug("	- cube : " + cube);
			log.debug("	- catalog : " + catalog);
		}

		if (cube == null) {
			// NOTE : Pentaho JPivot plugin seems to have a bug with storing
			// catalog and cube names when more than one analysis data sources
			// are registered. So we need to check for such an error and try to
			// guess the correct names.

			String originalCubeName = cube;
			String originalCatalogName = catalog;

			if (log.isWarnEnabled()) {
				log.warn("Cube name is not specified. Trying to guess the correct cube and catalog names.");
			}

			MdxParser parser = new MdxParserImpl();
			MdxStatement statement = parser.parse(query);

			cube = statement.getCube().getNames().get(0).getUnquotedName();

			if (log.isInfoEnabled()) {
				log.info("Cube name specified in MDX query : " + cube);
			}

			List<CubeInfo> cubes = dataSourceManager.getCubes(catalog);

			boolean found = false;

			for (CubeInfo cubeInfo : cubes) {
				found = cubeInfo.getName().equalsIgnoreCase(cube);
				if (found) {
					cube = cubeInfo.getName();
					break;
				}
			}

			if (!found) {
				throw new InvalidConnectionInfoException(originalCubeName,
						originalCatalogName);
			}
		}

		ConnectionInfo connectionInfo = new ConnectionInfo(catalog, cube);

		OlapDataSource dataSource = dataSourceManager
				.getDataSource(connectionInfo);

		PivotModel model = new PivotModelImpl(dataSource);
		model.setMdx(query);

		FacesContext context = FacesContext.getCurrentInstance();
		PrimeFacesPivotRenderer renderer = new PrimeFacesPivotRenderer(context);

		// NOTE : Pentaho JPivot plugin does not preserve rendering states in
		// saved reports, despite they contain related
		// tags for such properties(i.e. <hide-spans/>).

		ViewState state = viewStateHolder
				.createNewState(connectionInfo, viewId);

		state.setName(title);
		state.setConnectionInfo(connectionInfo);
		state.setModel(model);
		state.setRendererState(renderer.saveState());

		return state;
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
	 * @return the repository
	 */
	public ReportRepository getRepository() {
		return repository;
	}

	/**
	 * @param repository
	 *            the repository to set
	 */
	public void setRepository(ReportRepository repository) {
		this.repository = repository;
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
	 * @return the rootNode
	 */
	public TreeNode getRootNode() {
		if (rootNode == null) {
			this.rootNode = new DefaultTreeNode();
			rootNode.setExpanded(true);

			RepositoryNode node;

			try {
				node = new RepositoryNode(repository.getRoot(), repository);
			} catch (IOException e) {
				throw new FacesException(e);
			}

			node.setExpanded(true);
			node.setFilter(fileFilter);

			rootNode.getChildren().add(node);
		}

		return rootNode;
	}

	protected RepositoryNode getRepositoryRootNode() {
		return (RepositoryNode) getRootNode().getChildren().get(0);
	}

	public TreeNode getSelection() {
		return selection;
	}

	public void setSelection(TreeNode selection) {
		this.selection = selection;
	}

	/**
	 * @return the convertedFiles
	 */
	public List<Entry> getConvertedFiles() {
		return convertedFiles;
	}

	/**
	 * @return the selectedFile
	 */
	public Entry getSelectedFile() {
		return selectedFile;
	}

	/**
	 * @param selectedFile
	 *            the selectedFile to set
	 */
	public void setSelectedFile(Entry selectedFile) {
		this.selectedFile = selectedFile;
	}

	/**
	 * @return the migrationDone
	 */
	public boolean isMigrationDone() {
		return migrationDone;
	}

	static class MigrationTargetFilter implements RepositoryFileFilter,
			Serializable {

		private static final long serialVersionUID = 4427077075329175626L;

		/**
		 * @see com.eyeq.pivot4j.analytics.repository.RepositoryFileFilter#accept(com.eyeq.pivot4j.analytics.repository.ReportFile)
		 */
		@Override
		public boolean accept(ReportFile file) {
			return file.isDirectory()
					|| "xaction".equalsIgnoreCase(file.getExtension())
					|| "xjpivot".equalsIgnoreCase(file.getExtension());
		}
	}

	static class InvalidConnectionInfoException extends Exception {

		private static final long serialVersionUID = 8066830074992740616L;

		private String cube;

		private String catalog;

		/**
		 * @param cube
		 * @param catalog
		 */
		private InvalidConnectionInfoException(String cube, String catalog) {
			this.cube = cube;
			this.catalog = catalog;
		}

		/**
		 * @return the cube
		 */
		public String getCube() {
			return cube;
		}

		/**
		 * @return the catalog
		 */
		public String getCatalog() {
			return catalog;
		}
	}

	public static class Entry {

		private ReportFile file;

		private ReportFile result;

		private String error;

		/**
		 * @param file
		 * @param result
		 * @param error
		 */
		private Entry(ReportFile file, ReportFile result, String error) {
			this.file = file;
			this.error = error;
			this.result = result;
		}

		/**
		 * @return the file
		 */
		public ReportFile getFile() {
			return file;
		}

		/**
		 * @return the result
		 */
		public ReportFile getResult() {
			return result;
		}

		/**
		 * @return the error
		 */
		public String getError() {
			return error;
		}
	}
}
