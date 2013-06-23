package com.eyeq.pivot4j.analytics.ui.navigator;

import java.util.List;

import org.olap4j.metadata.MetadataElement;
import org.primefaces.model.TreeNode;

public abstract class NavigatorNode<T> implements TreeNode {

	private TreeNode parent;

	private T object;

	private NodeData data;

	private boolean expanded = false;

	private boolean selectable = false;

	private boolean selected = false;

	private List<TreeNode> children;

	private NodeFilter nodeFilter;

	public NavigatorNode() {
	}

	/**
	 * @param object
	 */
	public NavigatorNode(T object) {
		setObject(object);
	}

	/**
	 * @param object
	 * @return
	 */
	protected abstract NodeData createData(T object);

	/**
	 * @see org.primefaces.model.TreeNode#getParent()
	 */
	@Override
	public TreeNode getParent() {
		return parent;
	}

	public T getObject() {
		return object;
	}

	/**
	 * @param object
	 */
	public void setObject(T object) {
		this.object = object;

		if (object == null) {
			this.data = null;
		} else {
			this.data = createData(object);
		}
	}

	/**
	 * @see org.primefaces.model.TreeNode#setParent(org.primefaces.model.TreeNode)
	 */
	@Override
	public void setParent(TreeNode parent) {
		this.parent = parent;
	}

	/**
	 * @see org.primefaces.model.TreeNode#isExpanded()
	 */
	@Override
	public boolean isExpanded() {
		return expanded;
	}

	/**
	 * @see org.primefaces.model.TreeNode#setExpanded(boolean)
	 */
	@Override
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	/**
	 * @see org.primefaces.model.TreeNode#isSelectable()
	 */
	@Override
	public boolean isSelectable() {
		return selectable;
	}

	/**
	 * @see org.primefaces.model.TreeNode#setSelectable(boolean)
	 */
	@Override
	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	/**
	 * @see org.primefaces.model.TreeNode#isSelected()
	 */
	@Override
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @see org.primefaces.model.TreeNode#setSelected(boolean)
	 */
	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * @see org.primefaces.model.TreeNode#getData()
	 */
	@Override
	public NodeData getData() {
		return data;
	}

	/**
	 * @return the nodeFilter
	 */
	public NodeFilter getNodeFilter() {
		return nodeFilter;
	}

	/**
	 * @param nodeFilter
	 *            the nodeFilter to set
	 */
	public void setNodeFilter(NodeFilter nodeFilter) {
		this.nodeFilter = nodeFilter;
	}

	/**
	 * @see org.primefaces.model.TreeNode#getChildCount()
	 */
	@Override
	public int getChildCount() {
		return getChildren().size();
	}

	/**
	 * @see org.primefaces.model.TreeNode#getChildren()
	 */
	@Override
	public List<TreeNode> getChildren() {
		if (!isLoaded()) {
			this.children = createChildren();
		}

		return children;
	}

	public void refresh() {
		this.children = null;
	}

	protected boolean isLoaded() {
		return children != null;
	}

	public void clearSelection() {
		setSelected(false);

		if (children != null) {
			for (TreeNode child : children) {
				if (child instanceof NavigatorNode) {
					((NavigatorNode<?>) child).clearSelection();
				}
			}
		}
	}

	/**
	 * @param element
	 * @param child
	 * @return
	 */
	protected <C extends MetadataElement> boolean configureChildNode(C element,
			NavigatorNode<?> child) {
		child.setParent(this);

		if (nodeFilter != null) {
			if (nodeFilter.isVisible(element)) {
				child.setNodeFilter(nodeFilter);
				child.setExpanded(nodeFilter.isExpanded(element));
				child.setSelectable(nodeFilter.isSelectable(element));
				child.setSelected(nodeFilter.isSelected(element));

				NodeData nodeData = child.getData();
				nodeData.setSelected(nodeFilter.isActive(element));
			} else {
				return false;
			}
		}

		return true;
	}

	protected abstract List<TreeNode> createChildren();
}
