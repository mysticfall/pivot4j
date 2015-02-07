package org.pivot4j.analytics.ui;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.pivot4j.analytics.component.tree.DefaultTreeNode;
import org.pivot4j.analytics.config.Settings;
import org.pivot4j.analytics.datasource.DataSourceManager;
import org.pivot4j.analytics.repository.DataSourceNotFoundException;
import org.pivot4j.analytics.repository.ReportContent;
import org.pivot4j.analytics.repository.ReportFile;
import org.pivot4j.analytics.repository.ReportRepository;
import org.pivot4j.analytics.repository.RepositoryFileComparator;
import org.pivot4j.analytics.repository.RepositoryFileFilter;
import org.pivot4j.analytics.state.ViewState;
import org.pivot4j.analytics.state.ViewStateEvent;
import org.pivot4j.analytics.state.ViewStateHolder;
import org.pivot4j.analytics.state.ViewStateListener;
import org.pivot4j.analytics.ui.navigator.RepositoryNode;
import org.primefaces.context.RequestContext;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name = "repositoryHandler")
@SessionScoped
public class RepositoryHandler implements ViewStateListener, Serializable {

	private static final long serialVersionUID = -860723075484210684L;

	private Logger log = LoggerFactory.getLogger(getClass());

	@ManagedProperty(value = "#{settings}")
	private Settings settings;

	@ManagedProperty(value = "#{dataSourceManager}")
	private DataSourceManager dataSourceManager;

	@ManagedProperty(value = "#{reportRepository}")
	private ReportRepository repository;

	@ManagedProperty(value = "#{viewStateHolder}")
	private ViewStateHolder viewStateHolder;

	private TreeNode rootNode;

	private TreeNode selection;

	private String activeViewId;

	private String reportName;

	private String folderName;

	@PostConstruct
	protected void initialize() {
		viewStateHolder.addViewStateListener(this);

		ViewState state = viewStateHolder.createNewState();

		if (state != null) {
			viewStateHolder.registerState(state);

			this.activeViewId = state.getId();
		}
	}

	@PreDestroy
	protected void destroy() {
		viewStateHolder.removeViewStateListener(this);
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

	public void loadReports() {
		RequestContext context = RequestContext.getCurrentInstance();

		List<ViewState> states = viewStateHolder.getStates();

		for (ViewState state : states) {
			context.addCallbackParam(state.getId(), new ViewInfo(state));
		}
	}

	public void create() {
		ViewState state = viewStateHolder.createNewState();
		viewStateHolder.registerState(state);

		this.activeViewId = state.getId();

		if (log.isInfoEnabled()) {
			log.info("Created a new view state : {}", state.getId());
		}

		RequestContext requestContext = RequestContext.getCurrentInstance();
		requestContext.addCallbackParam("report", new ViewInfo(state));
	}

	public void createDirectory() {
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(
				context, "msg");

		ReportFile parent = getTargetDirectory();

		ReportFile newFile;

		StringBuilder builder = new StringBuilder();
		builder.append(parent.getPath());

		if (!parent.getPath().endsWith(ReportFile.SEPARATOR)) {
			builder.append(ReportFile.SEPARATOR);
		}

		builder.append(folderName);

		String path = builder.toString();

		if (log.isInfoEnabled()) {
			log.info("Creating a new folder : {}", path);
		}

		try {
			if (repository.exists(path)) {
				this.folderName = null;

				String title = bundle.getString("error.create.folder.title");
				String message = bundle.getString("warn.folder.exists");

				context.addMessage("new-folder-form:name", new FacesMessage(
						FacesMessage.SEVERITY_ERROR, title, message));

				return;
			}

			newFile = repository.createDirectory(parent, folderName);
		} catch (IOException e) {
			String title = bundle.getString("error.create.folder.title");
			String message = bundle.getString("error.create.folder.io") + e;

			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_ERROR, title, message));

			if (log.isErrorEnabled()) {
				log.error(title, e);
			}

			return;
		}

		RepositoryNode parentNode = getRepositoryRootNode().findNode(parent);
		parentNode.setExpanded(true);
		parentNode.setSelected(false);
		parentNode.refresh();

		RepositoryNode newFileNode = getRepositoryRootNode().findNode(newFile);
		newFileNode.setSelected(true);

		this.selection = newFileNode;
		this.folderName = null;

		RequestContext requestContext = RequestContext.getCurrentInstance();
		requestContext.execute("PF('newFolderDialog').hide()");
	}

	public void save() {
		FacesContext context = FacesContext.getCurrentInstance();
		RequestContext requestContext = RequestContext.getCurrentInstance();

		ResourceBundle bundle = context.getApplication().getResourceBundle(
				context, "msg");

		Map<String, String> parameters = context.getExternalContext()
				.getRequestParameterMap();
		String param = parameters.get("close");

		String viewId = parameters.get("viewId");

		if (viewId == null) {
			viewId = activeViewId;
		}

		boolean saveAndClose = "true".equals(param);

		ViewState state = viewStateHolder.getState(viewId);

		ReportFile file = state.getFile();

		if (file == null) {
			suggestNewName();

			requestContext.update("new-form");
			requestContext.execute("PF('newReportDialog').show()");

			return;
		}

		requestContext.update(Arrays.asList(new String[] {
				"toolbar-form:toolbar", "repository-form:repository-panel",
				"growl" }));

		try {
			repository.setReportContent(file, new ReportContent(state));
		} catch (ConfigurationException e) {
			String title = bundle.getString("error.save.report.title");
			String message = bundle.getString("error.save.report.format") + e;

			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_INFO, title, message));

			if (log.isErrorEnabled()) {
				log.error(title, e);
			}

			return;
		} catch (IOException e) {
			String title = bundle.getString("error.save.report.title");
			String message = bundle.getString("error.save.report.io") + e;

			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_ERROR, title, message));

			if (log.isErrorEnabled()) {
				log.error(title, e);
			}

			return;
		}

		if (saveAndClose) {
			requestContext.update("close-form");

			close(viewId);
		} else {
			if (this.selection != null) {
				this.selection.setSelected(false);
			}

			RepositoryNode node = getRepositoryRootNode().selectNode(file);
			node.setViewId(state.getId());
			node.setSelected(true);

			this.selection = node;
		}

		state.setDirty(false);

		String title = bundle.getString("message.save.report.title");
		String message = bundle.getString("message.save.report.message");

		context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
				title, message));

		requestContext.execute("enableSave(false);");
	}

	public void saveAs() {
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(
				context, "msg");

		ReportFile parent = getTargetDirectory();

		RepositoryNode root = getRepositoryRootNode();

		ViewState state = viewStateHolder.getState(activeViewId);

		if (state.getFile() != null) {
			RepositoryNode node = root.findNode(state.getFile());
			if (node != null) {
				node.setViewId(null);
			}
		}

		ReportContent content = new ReportContent(state);

		if (reportName.toLowerCase().endsWith(".pivot4j")) {
			reportName = reportName.substring(0, reportName.length() - 8);
		}

		String fileName = reportName + ".pivot4j";

		ReportFile file;

		try {
			file = repository.createFile(parent, fileName, content);
		} catch (ConfigurationException e) {
			String title = bundle.getString("error.save.report.title");
			String message = bundle.getString("error.save.report.format") + e;

			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_ERROR, title, message));

			if (log.isErrorEnabled()) {
				log.error(title, e);
			}

			return;
		} catch (IOException e) {
			String title = bundle.getString("error.save.report.title");
			String message = bundle.getString("error.save.report.io") + e;

			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_ERROR, title, message));

			if (log.isErrorEnabled()) {
				log.error(title, e);
			}

			return;
		}

		state.setName(reportName);
		state.setFile(file);
		state.setDirty(false);

		RepositoryNode parentNode = root.findNode(parent);
		parentNode.setSelected(false);
		parentNode.setExpanded(true);

		if (this.selection != null) {
			this.selection.setSelected(false);
		}

		RepositoryNode node = parentNode.selectNode(file);
		if (node == null) {
			node = new RepositoryNode(file, repository);
			node.setParent(parentNode);

			parentNode.getChildren().add(node);

			final RepositoryFileComparator comparator = new RepositoryFileComparator();

			Collections.sort(parentNode.getChildren(),
					new Comparator<TreeNode>() {

						@Override
						public int compare(TreeNode t1, TreeNode t2) {
							RepositoryNode r1 = (RepositoryNode) t1;
							RepositoryNode r2 = (RepositoryNode) t2;

							return comparator.compare(r1.getObject(),
									r2.getObject());
						}
					});
		}

		node.setViewId(activeViewId);
		node.setSelected(true);

		this.selection = node;
		this.reportName = null;

		RequestContext requestContext = RequestContext.getCurrentInstance();
		requestContext.addCallbackParam("name", state.getName());
		requestContext.addCallbackParam("path", file.getPath());

		String title = bundle.getString("message.save.report.title");
		String message = bundle.getString("message.saveAs.report.message")
				+ file.getPath();

		context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
				title, message));
	}

	public void open() {
		if (selection == null) {
			if (log.isWarnEnabled()) {
				log.warn("Unable to load report from empty or multiple selection.");
			}

			return;
		}

		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(
				context, "msg");

		RepositoryNode node = (RepositoryNode) selection;
		ReportFile file = node.getObject();

		String viewId = UUID.randomUUID().toString();

		String name = file.getName();

		if (name.toLowerCase().endsWith(".pivot4j")) {
			name = name.substring(0, name.length() - 8);
		}

		ViewState state = new ViewState(viewId, name);
		state.setFile(file);

		String errorMessage = null;
		Exception exception = null;

		try {
			ReportContent content = repository.getReportContent(file);
			content.read(state, dataSourceManager, settings.getConfiguration());
		} catch (ConfigurationException e) {
			exception = e;
			errorMessage = bundle.getString("error.open.report.format") + e;
		} catch (DataSourceNotFoundException e) {
			exception = e;
			errorMessage = bundle.getString("error.open.report.dataSource")
					+ e.getConnectionInfo().getCatalogName();
		} catch (IOException e) {
			exception = e;
			errorMessage = bundle.getString("error.open.report.io") + e;
		}

		if (exception != null) {
			String title = bundle.getString("error.open.report.title");

			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_ERROR, title, errorMessage));

			if (log.isErrorEnabled()) {
				log.error(title, exception);
			}

			return;
		}

		viewStateHolder.registerState(state);

		if (log.isInfoEnabled()) {
			log.info("Created a new view state : {}", viewId);
		}

		this.activeViewId = viewId;

		RequestContext requestContext = RequestContext.getCurrentInstance();
		requestContext.addCallbackParam("report", new ViewInfo(state));
	}

	public void refresh() {
		RepositoryNode node = (RepositoryNode) selection;
		node.refresh();
	}

	public void delete() {
		ViewState state = getActiveView();
		ReportFile file = state.getFile();

		delete(state);

		if (file != null) {
			delete(file);
		}

		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(
				context, "msg");

		String title = bundle.getString("message.delete.report.title");
		String message = bundle.getString("message.delete.report.message");

		context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
				title, message));
	}

	public void deleteFile() {
		RepositoryNode node = (RepositoryNode) selection;

		if (node.getViewId() != null) {
			delete(viewStateHolder.getState(node.getViewId()));
		}

		delete(node.getObject());

		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(
				context, "msg");

		String title = bundle.getString("message.delete.report.title");
		String message = bundle.getString("message.delete.report.message");

		context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
				title, message));
	}

	public void deleteDirectory() {
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(
				context, "msg");

		RepositoryNode node = (RepositoryNode) selection;
		ReportFile directory = node.getObject();

		try {
			List<ViewState> states = viewStateHolder.getStates();

			for (ViewState state : states) {
				if (state.getFile() == null) {
					continue;
				}

				ReportFile file = state.getFile();

				List<ReportFile> ancestors = file.getAncestors();

				if (ancestors.contains(directory)) {
					String title = bundle.getString("warn.folder.delete.title");
					String message = bundle
							.getString("warn.folder.delete.openReport.message");

					context.addMessage(null, new FacesMessage(
							FacesMessage.SEVERITY_WARN, title, message));

					return;
				}
			}

			repository.deleteFile(directory);

			selection.getParent().getChildren().remove(selection);

			this.selection = null;

			String title = bundle.getString("message.delete.folder.title");
			String message = bundle.getString("message.delete.folder.message");

			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_INFO, title, message));
		} catch (IOException e) {
			String title = bundle.getString("error.delete.folder.title");
			String message = bundle.getString("error.delete.folder.message")
					+ e;

			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_ERROR, title, message));

			if (log.isErrorEnabled()) {
				log.error(title, e);
			}
		}
	}

	/**
	 * @param state
	 */
	protected void delete(ViewState state) {
		String viewId = state.getId();

		viewStateHolder.unregisterState(viewId);

		if (viewId.equals(activeViewId)) {
			this.activeViewId = null;

			synchronized (viewStateHolder) {
				List<ViewState> states = viewStateHolder.getStates();

				int index = states.indexOf(state);
				if (index >= states.size() - 1) {
					index--;
				} else {
					index++;
				}

				if (index > -1 && index < states.size()) {
					this.activeViewId = states.get(index).getId();
				}
			}
		}

		RequestContext.getCurrentInstance().execute(
				String.format("closeTab(getTabIndex('%s'))", viewId));
	}

	/**
	 * @param file
	 */
	protected void delete(ReportFile file) {
		try {
			repository.deleteFile(file);
		} catch (IOException e) {
			FacesContext context = FacesContext.getCurrentInstance();
			ResourceBundle bundle = context.getApplication().getResourceBundle(
					context, "msg");

			String title = bundle.getString("error.delete.report.title");
			String message = bundle.getString("error.delete.report.message")
					+ e;

			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_ERROR, title, message));

			if (log.isErrorEnabled()) {
				log.error(title, e);
			}

			return;
		}

		if (selection instanceof RepositoryNode) {
			RepositoryNode node = (RepositoryNode) selection;
			if (node.getObject().equals(file)) {
				selection.getParent().getChildren().remove(selection);

				this.selection = null;
			}
		}
	}

	public void close() {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, String> parameters = context.getExternalContext()
				.getRequestParameterMap();
		String viewId = parameters.get("viewId");

		close(viewId);
	}

	/**
	 * @param viewId
	 */
	public void close(String viewId) {
		String viewToClose;

		if (viewId == null) {
			viewToClose = this.activeViewId;
			this.activeViewId = null;
		} else {
			viewToClose = viewId;
		}

		ViewState view = viewStateHolder.getState(viewToClose);
		int index = viewStateHolder.getStates().indexOf(view);

		viewStateHolder.unregisterState(viewToClose);

		RequestContext.getCurrentInstance().execute(
				String.format("closeTab(%s)", index));
	}

	public boolean isOpenEnabled() {
		if (selection != null) {
			RepositoryNode node = (RepositoryNode) selection;
			ReportFile file = node.getObject();

			if (!file.isDirectory()) {
				List<ViewState> states = viewStateHolder.getStates();
				for (ViewState state : states) {
					if (file.equals(state.getFile())) {
						return false;
					}
				}

				return true;
			}
		}

		return false;
	}

	public boolean isDeleteEnabled() {
		if (selection != null) {
			RepositoryNode node = (RepositoryNode) selection;
			ReportFile file = node.getObject();

			return !file.isRoot();
		}

		return false;
	}

	public void onTabChange() {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, String> parameters = context.getExternalContext()
				.getRequestParameterMap();

		select(parameters.get("viewId"));
	}

	public void onSelectionChange() {
		RepositoryNode root = getRepositoryRootNode();
		root.clearSelection();

		if (selection != null) {
			selection.setSelected(true);
		}
	}

	/**
	 * @param viewId
	 */
	protected void select(String viewId) {
		this.activeViewId = viewId;

		ViewState state = viewStateHolder.getState(activeViewId);

		RepositoryNode root = getRepositoryRootNode();
		root.clearSelection();

		if (state != null && state.getFile() != null) {
			this.selection = root.selectNode(state.getFile());
		}
	}

	public void onChange() {
		ViewState state = getActiveView();
		if (state != null) {
			state.setDirty(true);
		}
	}

	public void suggestNewName() {
		String name = null;

		ViewState state = getActiveView();
		if (state != null) {
			name = state.getName();
		}

		if (name == null) {
			this.reportName = null;
			return;
		}

		if (name.toLowerCase().endsWith(".pivot4j")) {
			name = name.substring(0, name.length() - 8);
		}

		ReportFile parent = getTargetDirectory();

		Set<String> names;

		try {
			List<ReportFile> children = repository.getFiles(parent);

			names = new HashSet<String>(children.size());

			for (ReportFile child : children) {
				String childName = child.getName();

				if (childName.toLowerCase().endsWith(".pivot4j")) {
					childName = childName.substring(0, childName.length() - 8);
				}

				names.add(childName);
			}
		} catch (IOException e) {
			throw new FacesException(e);
		}

		Pattern pattern = Pattern.compile("([^\\(]+)\\(([0-9]+)\\)");

		while (names.contains(name)) {
			Matcher matcher = pattern.matcher(name);

			if (matcher.matches()) {
				String prefix = matcher.group(1);
				int suffix = Integer.parseInt(matcher.group(2)) + 1;

				StringBuilder builder = new StringBuilder();
				builder.append(prefix);
				builder.append("(");
				builder.append(Integer.toString(suffix));
				builder.append(")");

				name = builder.toString();
			} else {
				name += "(2)";
			}
		}

		this.reportName = name;
	}

	protected ReportFile getTargetDirectory() {
		ReportFile parent = null;

		if (selection != null) {
			RepositoryNode node = (RepositoryNode) selection;

			ReportFile selectedFile = node.getObject();
			if (selectedFile.isDirectory()) {
				parent = selectedFile;
			} else {
				try {
					parent = selectedFile.getParent();
				} catch (IOException e) {
					throw new FacesException(e);
				}
			}
		}

		if (parent == null) {
			try {
				parent = repository.getRoot();
			} catch (IOException e) {
				throw new FacesException(e);
			}
		}

		return parent;
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
			node.setFilter(new DefaultExtensionFilter(settings.getExtension()));

			rootNode.getChildren().add(node);
		}

		return rootNode;
	}

	protected RepositoryNode getRepositoryRootNode() {
		return (RepositoryNode) getRootNode().getChildren().get(0);
	}

	/**
	 * @see org.pivot4j.analytics.state.ViewStateListener#viewRegistered(org.pivot4j.analytics.state.ViewStateEvent)
	 */
	@Override
	public void viewRegistered(ViewStateEvent e) {
		final String viewId = e.getState().getId();
		final ReportFile file = e.getState().getFile();

		if (file == null) {
			return;
		}

		RepositoryNode root = getRepositoryRootNode();
		RepositoryNode node = root.findNode(file);

		if (node != null) {
			node.setViewId(viewId);
		}
	}

	/**
	 * @see org.pivot4j.analytics.state.ViewStateListener#viewUnregistered(org.pivot4j.analytics.state.ViewStateEvent)
	 */
	@Override
	public void viewUnregistered(ViewStateEvent e) {
		String viewId = e.getState().getId();

		RepositoryNode root = getRepositoryRootNode();
		RepositoryNode node = root.findNode(viewId);

		if (node != null) {
			node.setViewId(null);
		}
	}

	/**
	 * @return the activeViewId
	 */
	public ViewState getActiveView() {
		if (activeViewId == null) {
			return null;
		}

		return viewStateHolder.getState(activeViewId);
	}

	/**
	 * @return the activeViewId
	 */
	public String getActiveViewId() {
		return activeViewId;
	}

	/**
	 * @param activeViewId
	 *            the activeViewId to set
	 */
	public void setActiveViewId(String activeViewId) {
		this.activeViewId = activeViewId;
	}

	/**
	 * @return the reportName
	 */
	public String getReportName() {
		return reportName;
	}

	/**
	 * @param reportName
	 *            the reportName to set
	 */
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	/**
	 * @return the folderName
	 */
	public String getFolderName() {
		return folderName;
	}

	/**
	 * @param folderName
	 *            the folderName to set
	 */
	public void setFolderName(String folderName) {
		this.folderName = folderName;
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
	 * @return the selection
	 */
	public TreeNode getSelection() {
		return selection;
	}

	/**
	 * @param selection
	 *            the selection to set
	 */
	public void setSelection(TreeNode selection) {
		this.selection = selection;
	}

	public static class ViewInfo implements Serializable {

		private static final long serialVersionUID = 862747643432896517L;

		private String id;

		private String name;

		private String path;

		private boolean dirty;

		private boolean initialized;

		/**
		 * @param state
		 */
		ViewInfo(ViewState state) {
			this.id = state.getId();
			this.name = state.getName();
			this.dirty = state.isDirty();
			this.initialized = state.getConnectionInfo() != null;

			if (state.getFile() != null) {
				this.path = state.getFile().getPath();
			}
		}

		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the path
		 */
		public String getPath() {
			return path;
		}

		/**
		 * @return the dirty
		 */
		public boolean isDirty() {
			return dirty;
		}

		/**
		 * @return the initialized
		 */
		public boolean isInitialized() {
			return initialized;
		}
	}

	static class DefaultExtensionFilter implements RepositoryFileFilter {

		private String extension;

		DefaultExtensionFilter(String extension) {
			this.extension = StringUtils.trimToNull(extension);
		}

		/**
		 * @see org.pivot4j.analytics.repository.RepositoryFileFilter#accept(org.pivot4j.analytics.repository.ReportFile)
		 */
		@Override
		public boolean accept(ReportFile file) {
			if (file.isDirectory() || extension == null) {
				return true;
			} else {
				String value = file.getExtension();

				return value != null && value.endsWith(extension);
			}
		}
	}
}
