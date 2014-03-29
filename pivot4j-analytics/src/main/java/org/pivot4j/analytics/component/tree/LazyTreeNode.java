package org.pivot4j.analytics.component.tree;

import java.util.List;

import org.olap4j.metadata.MetadataElement;
import org.primefaces.model.TreeNode;

public abstract class LazyTreeNode<T> extends AbstractTreeNode<T> {

	private T object;

	private NodeData data;

	private List<TreeNode> children;

	private NodeFilter nodeFilter;

	public LazyTreeNode() {
	}

	/**
	 * @param object
	 */
	public LazyTreeNode(T object) {
		setObject(object);
	}

	/**
	 * @param object
	 * @return
	 */
	protected abstract NodeData createData(T object);

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
		synchronized (this) {
			if (!isLoaded()) {
				this.children = createChildren();
			}
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
				if (child instanceof LazyTreeNode) {
					((LazyTreeNode<?>) child).clearSelection();
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
			LazyTreeNode<?> child) {
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
