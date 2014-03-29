package org.pivot4j.analytics.component.tree;

import org.primefaces.model.TreeNode;

public abstract class AbstractTreeNode<T> implements TreeNode {

	private static final String ROOT_ROW_KEY = "root";

	private TreeNode parent;

	private boolean expanded = false;

	private boolean selectable = false;

	private boolean selected = false;

	private boolean partialSelected = false;

	/**
	 * @see org.primefaces.model.TreeNode#getParent()
	 */
	@Override
	public TreeNode getParent() {
		return parent;
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
	 * @return the rowKey
	 */
	public String getRowKey() {
		if (getParent() == null) {
			return ROOT_ROW_KEY;
		}

		int index = getParent().getChildren().indexOf(this);

		String parentKey = getParent().getRowKey();

		if (parentKey == null || parentKey.equals(ROOT_ROW_KEY)) {
			return Integer.toString(index);
		}

		return parentKey + "_" + index;
	}

	/**
	 * @param rowKey
	 *            the rowKey to set
	 */
	public void setRowKey(String rowKey) {
		// No-op.
	}

	/**
	 * @return the partialSelected
	 */
	public boolean isPartialSelected() {
		return partialSelected;
	}

	/**
	 * @param partialSelected
	 *            the partialSelected to set
	 */
	public void setPartialSelected(boolean partialSelected) {
		this.partialSelected = partialSelected;
	}
}
