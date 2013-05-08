package com.eyeq.pivot4j.analytics.ui;

import java.io.IOException;
import java.io.Serializable;
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
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.analytics.datasource.ConnectionMetadata;
import com.eyeq.pivot4j.analytics.datasource.DataSourceManager;
import com.eyeq.pivot4j.analytics.repository.ReportContent;
import com.eyeq.pivot4j.analytics.repository.ReportRepository;
import com.eyeq.pivot4j.analytics.repository.RepositoryFile;
import com.eyeq.pivot4j.analytics.repository.RepositoryFileComparator;
import com.eyeq.pivot4j.analytics.state.ViewState;
import com.eyeq.pivot4j.analytics.state.ViewStateEvent;
import com.eyeq.pivot4j.analytics.state.ViewStateHolder;
import com.eyeq.pivot4j.analytics.state.ViewStateListener;
import com.eyeq.pivot4j.analytics.ui.navigator.RepositoryNode;

@ManagedBean(name = "repositoryHandler")
@SessionScoped
public class RepositoryHandler implements ViewStateListener {

	protected Logger log = LoggerFactory.getLogger(getClass());

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

	@PostConstruct
	protected void initialize() {
		viewStateHolder.addViewStateListener(this);

		ViewState state = viewStateHolder
				.createNewState(new ConnectionMetadata());

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
			context.addCallbackParam(state.getId(), new PageInfo(state));
		}
	}

	public void create() {
		ViewState state = viewStateHolder
				.createNewState(new ConnectionMetadata());
		viewStateHolder.registerState(state);

		this.activeViewId = state.getId();

		if (log.isInfoEnabled()) {
			log.info("Created a new view state : " + state.getId());
		}

		RequestContext requestContext = RequestContext.getCurrentInstance();
		requestContext.addCallbackParam("report", new PageInfo(state));
	}

	public void save() {
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(
				context, "msg");

		ViewState state = viewStateHolder.getState(activeViewId);

		ReportContent content = new ReportContent(state);

		RepositoryFile file = state.getFile();

		try {
			repository.setContent(file, content);
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
					FacesMessage.SEVERITY_INFO, title, message));

			if (log.isErrorEnabled()) {
				log.error(title, e);
			}

			return;
		}

		RepositoryNode node = getRepositoryRootNode().selectNode(file);
		if (node != null) {
			node.setViewId(state.getId());
		}

		state.setDirty(false);

		String title = bundle.getString("message.save.report.title");
		String message = bundle.getString("message.save.report.message");

		context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
				title, message));
	}

	public void saveAs() {
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(
				context, "msg");

		RepositoryFile parent = getDirectoryToSave();

		RepositoryNode rootNode = getRepositoryRootNode();

		ViewState state = viewStateHolder.getState(activeViewId);

		if (state.getFile() != null) {
			RepositoryNode node = rootNode.findNode(state.getFile());
			if (node != null) {
				node.setViewId(null);
			}
		}

		ReportContent content = new ReportContent(state);

		RepositoryFile file;

		try {
			file = repository.createFile(parent, reportName, content);
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
					FacesMessage.SEVERITY_INFO, title, message));

			if (log.isErrorEnabled()) {
				log.error(title, e);
			}

			return;
		}

		state.setName(reportName);
		state.setFile(file);

		RepositoryNode parentNode = rootNode.findNode(parent);

		RepositoryNode selection = parentNode.selectNode(file);
		if (selection == null) {
			selection = new RepositoryNode(file, repository);
			selection.setParent(parentNode);

			parentNode.getChildren().add(selection);

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

		selection.setViewId(activeViewId);
		this.selection = selection;

		RequestContext requestContext = RequestContext.getCurrentInstance();
		requestContext.addCallbackParam("name", state.getName());
		requestContext.addCallbackParam("path", file.getPath());

		String title = bundle.getString("message.save.report.title");
		String message = bundle.getString("message.save_as.report.message")
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
		RepositoryFile file = node.getObject();

		String viewId = UUID.randomUUID().toString();

		ViewState state = new ViewState(viewId, file.getName());
		state.setFile(file);

		try {
			ReportContent content = repository.getContent(file);
			content.read(state, dataSourceManager);
		} catch (ConfigurationException e) {
			String title = bundle.getString("error.open.report.title");
			String message = bundle.getString("error.open.report.format") + e;

			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_INFO, title, message));

			if (log.isErrorEnabled()) {
				log.error(title, e);
			}

			return;
		} catch (IOException e) {
			String title = bundle.getString("error.open.report.title");
			String message = bundle.getString("error.open.report.io") + e;

			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_INFO, title, message));

			if (log.isErrorEnabled()) {
				log.error(title, e);
			}

			return;
		}

		viewStateHolder.registerState(state);

		if (log.isInfoEnabled()) {
			log.info("Created a new view state : " + viewId);
		}

		this.activeViewId = viewId;

		RequestContext requestContext = RequestContext.getCurrentInstance();
		requestContext.addCallbackParam("report", new PageInfo(state));
	}

	public void delete() {
		ViewState state = viewStateHolder.getState(activeViewId);
		if (state == null) {
			return;
		}

		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(
				context, "msg");

		RepositoryFile file = state.getFile();

		try {
			repository.deleteFile(file);
		} catch (IOException e) {
			String title = bundle.getString("error.delete.report.title");
			String message = bundle.getString("error.delete.report.message")
					+ e;

			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_INFO, title, message));

			if (log.isErrorEnabled()) {
				log.error(title, e);
			}

			return;
		}

		viewStateHolder.unregisterState(activeViewId);

		this.activeViewId = null;

		if (selection != null && selection instanceof RepositoryNode) {
			RepositoryNode node = (RepositoryNode) selection;
			if (node.getObject().equals(file)) {
				selection.getParent().getChildren().remove(selection);

				this.selection = null;
			}
		}

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

		String title = bundle.getString("message.delete.report.title");
		String message = bundle.getString("message.delete.report.message");

		context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
				title, message));
	}

	public void close() {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, String> parameters = context.getExternalContext()
				.getRequestParameterMap();
		String viewId = parameters.get("viewId");

		viewStateHolder.unregisterState(viewId);
	}

	public void onTabChange() {
		FacesContext context = FacesContext.getCurrentInstance();

		Map<String, String> parameters = context.getExternalContext()
				.getRequestParameterMap();

		select(parameters.get("viewId"));
	}

	public void onSelectionChange() {
		RepositoryNode rootNode = getRepositoryRootNode();
		rootNode.clearSelection();

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

		RepositoryNode rootNode = getRepositoryRootNode();
		rootNode.clearSelection();

		if (state != null && state.getFile() != null) {
			this.selection = rootNode.selectNode(state.getFile());
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

		RepositoryFile parent = getDirectoryToSave();

		Set<String> names;

		try {
			List<RepositoryFile> children = repository.getFiles(parent);

			names = new HashSet<String>(children.size());

			for (RepositoryFile child : children) {
				names.add(child.getName());
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

	protected RepositoryFile getDirectoryToSave() {
		RepositoryFile parent = null;

		if (selection != null) {
			RepositoryNode node = (RepositoryNode) selection;

			RepositoryFile selectedFile = node.getObject();
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
			parent = repository.getRoot();
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

			RepositoryNode node = new RepositoryNode(repository.getRoot(),
					repository);
			node.setExpanded(true);

			rootNode.getChildren().add(node);
		}

		return rootNode;
	}

	protected RepositoryNode getRepositoryRootNode() {
		return (RepositoryNode) getRootNode().getChildren().get(0);
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.state.ViewStateListener#viewRegistered(com.eyeq.pivot4j.analytics.state.ViewStateEvent)
	 */
	@Override
	public void viewRegistered(ViewStateEvent e) {
		final String viewId = e.getState().getId();
		final RepositoryFile file = e.getState().getFile();

		if (file == null) {
			return;
		}

		RepositoryNode rootNode = getRepositoryRootNode();
		RepositoryNode node = rootNode.findNode(file);

		if (node != null) {
			node.setViewId(viewId);
		}
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.state.ViewStateListener#viewUnregistered(com.eyeq.pivot4j.analytics.state.ViewStateEvent)
	 */
	@Override
	public void viewUnregistered(ViewStateEvent e) {
		String viewId = e.getState().getId();

		RepositoryNode rootNode = getRepositoryRootNode();
		RepositoryNode node = rootNode.findNode(viewId);

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

	public static class PageInfo implements Serializable {

		private static final long serialVersionUID = 862747643432896517L;

		private String id;

		private String name;

		private String path;

		private boolean dirty;

		/**
		 * @param state
		 */
		PageInfo(ViewState state) {
			this.id = state.getId();
			this.name = state.getName();
			this.dirty = state.isDirty();

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
	}
}
