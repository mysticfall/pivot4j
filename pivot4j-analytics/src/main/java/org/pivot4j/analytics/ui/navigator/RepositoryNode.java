package org.pivot4j.analytics.ui.navigator;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.component.StateHolder;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.pivot4j.analytics.component.tree.LazyTreeNode;
import org.pivot4j.analytics.component.tree.NodeData;
import org.pivot4j.analytics.repository.ReportFile;
import org.pivot4j.analytics.repository.ReportRepository;
import org.pivot4j.analytics.repository.RepositoryFileFilter;
import org.primefaces.model.TreeNode;

public class RepositoryNode extends LazyTreeNode<ReportFile> implements
		StateHolder {

	private ReportRepository repository;

	private RepositoryFileFilter filter;

	private boolean transientState = false;

	private String viewId;

	public RepositoryNode() {
		ExternalContext externalContext = FacesContext.getCurrentInstance()
				.getExternalContext();
		Map<String, Object> applicationMap = externalContext
				.getApplicationMap();

		this.repository = (ReportRepository) applicationMap
				.get("reportRepository");
	}

	/**
	 * @param file
	 * @param repository
	 */
	public RepositoryNode(ReportFile file, ReportRepository repository) {
		super(file);

		if (repository == null) {
			throw new NullArgumentException("repository");
		}

		this.repository = repository;

		setSelectable(true);
	}

	/**
	 * @return the repository
	 */
	public ReportRepository getRepository() {
		return repository;
	}

	/**
	 * @see org.primefaces.model.TreeNode#getType()
	 */
	@Override
	public String getType() {
		String type;

		if (getObject().isRoot()) {
			type = "root";
		} else if (getObject().isDirectory()) {
			type = "directory";
		} else {
			type = "file";
		}

		return type;
	}

	/**
	 * @see org.primefaces.model.TreeNode#isLeaf()
	 */
	@Override
	public boolean isLeaf() {
		return !getObject().isDirectory() || getChildCount() == 0;
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

		getData().setSelected(viewId != null);
	}

	/**
	 * @return the filter
	 */
	public RepositoryFileFilter getFilter() {
		return filter;
	}

	/**
	 * @param filter
	 *            the filter to set
	 */
	public void setFilter(RepositoryFileFilter filter) {
		this.filter = filter;
	}

	/**
	 * @see org.pivot4j.analytics.component.tree.LazyTreeNode#createData(java.lang.Object)
	 */
	@Override
	protected NodeData createData(ReportFile object) {
		return new NodeData(object.getPath(), object.getName());
	}

	/**
	 * @see org.pivot4j.analytics.component.tree.LazyTreeNode#createChildren()
	 */
	@Override
	protected List<TreeNode> createChildren() {
		List<TreeNode> children;

		try {
			List<ReportFile> files = repository.getFiles(getObject());

			children = new ArrayList<TreeNode>(files.size());

			for (ReportFile file : files) {
				if (filter == null || filter.accept(file)) {
					RepositoryNode child = new RepositoryNode(file, repository);
					child.setParent(this);
					child.setFilter(filter);

					children.add(child);
				}
			}
		} catch (IOException e) {
			throw new FacesException(e);
		}

		return children;
	}

	/**
	 * @param file
	 */
	public RepositoryNode selectNode(ReportFile file) {
		RepositoryNode node = findNode(file);

		if (node != null) {
			node.setSelected(true);

			TreeNode parent = node;

			while ((parent = parent.getParent()) != null) {
				parent.setExpanded(true);
			}
		}

		return node;
	}

	/**
	 * @param file
	 */
	public RepositoryNode findNode(ReportFile file) {
		if (file == null) {
			throw new NullArgumentException("file");
		}

		RepositoryNode selectedNode = null;

		List<ReportFile> ancestors;

		try {
			ancestors = file.getAncestors();
		} catch (IOException e) {
			throw new FacesException(e);
		}

		ReportFile thisFile = getObject();

		if (file.equals(thisFile)) {
			selectedNode = this;
		} else if (ancestors.contains(thisFile)) {
			for (TreeNode node : getChildren()) {
				RepositoryNode fileNode = (RepositoryNode) node;

				selectedNode = fileNode.findNode(file);

				if (selectedNode != null) {
					break;
				}
			}
		}

		return selectedNode;
	}

	/**
	 * @param viewId
	 */
	public RepositoryNode findNode(String viewId) {
		if (viewId == null) {
			throw new NullArgumentException("viewId");
		}

		RepositoryNode selectedNode = null;

		if (viewId.equals(this.viewId)) {
			selectedNode = this;
		} else if (isLoaded()) {
			for (TreeNode node : getChildren()) {
				RepositoryNode fileNode = (RepositoryNode) node;

				selectedNode = fileNode.findNode(viewId);

				if (selectedNode != null) {
					break;
				}
			}
		}

		return selectedNode;
	}

	/**
	 * @see javax.faces.component.StateHolder#isTransient()
	 */
	@Override
	public boolean isTransient() {
		return transientState;
	}

	/**
	 * @see javax.faces.component.StateHolder#setTransient(boolean)
	 */
	@Override
	public void setTransient(boolean newTransientValue) {
		this.transientState = newTransientValue;
	}

	/**
	 * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
	 */
	@Override
	public Object saveState(FacesContext context) {
		List<Object> states = new LinkedList<Object>();

		states.add(isSelectable());
		states.add(isSelected());
		states.add(isExpanded());
		states.add(viewId);
		states.add(getObject().getPath());

		if (filter instanceof Serializable) {
			states.add(filter);
		}

		return states.toArray(new Object[states.size()]);
	}

	/**
	 * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext,
	 *      java.lang.Object)
	 */
	@Override
	public void restoreState(FacesContext context, Object state) {
		Object[] states = (Object[]) state;

		if (repository == null) {
			Application application = context.getApplication();
			this.repository = application.evaluateExpressionGet(context,
					"#{reportRepository}", ReportRepository.class);
		}

		try {
			setObject(repository.getFile((String) states[4]));
		} catch (IOException e) {
			throw new FacesException(e);
		}

		setSelectable((Boolean) states[0]);
		setSelected((Boolean) states[1]);
		setExpanded((Boolean) states[2]);
		setViewId((String) states[3]);

		if (states.length > 5) {
			this.filter = (RepositoryFileFilter) states[5];
		}
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(getObject()).append(repository)
				.toHashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		RepositoryNode other = (RepositoryNode) obj;

		return new EqualsBuilder().append(getObject(), other.getObject())
				.append(repository, other.repository).isEquals();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getObject().toString();
	}
}
