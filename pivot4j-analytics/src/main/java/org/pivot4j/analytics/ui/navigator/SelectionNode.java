package org.pivot4j.analytics.ui.navigator;

import java.util.ArrayList;
import java.util.List;

import org.olap4j.metadata.Member;
import org.pivot4j.analytics.component.tree.NodeData;
import org.pivot4j.util.MemberSelection;
import org.pivot4j.util.TreeNode;

public class SelectionNode extends MetadataNode<Member> {

	private MemberSelection selection;

	private TreeNode<Member> node;

	/**
	 * @param selection
	 */
	public SelectionNode(MemberSelection selection) {
		this(selection, selection);
	}

	/**
	 * @param node
	 * @param selection
	 */
	SelectionNode(TreeNode<Member> node, MemberSelection selection) {
		super(node.getReference());

		this.node = node;
		this.selection = selection;

		boolean selected = selection.isSelected(node.getReference());

		setSelectable(true);
		setExpanded(true);

		NodeData data = getData();
		if (data != null) {
			data.setSelected(selected);
		}
	}

	/**
	 * @see org.primefaces.model.TreeNode#getType()
	 */
	@Override
	public String getType() {
		return "member";
	}

	/**
	 * @see org.primefaces.model.TreeNode#getChildCount()
	 */
	@Override
	public int getChildCount() {
		return node.getChildCount();
	}

	/**
	 * @see org.primefaces.model.TreeNode#isLeaf()
	 */
	@Override
	public boolean isLeaf() {
		return getChildCount() == 0;
	}

	/**
	 * @param child
	 */
	public void moveUp(SelectionNode child) {
		int index = getChildren().indexOf(child);

		if (index < 0) {
			throw new IllegalArgumentException(
					"The specified node is not a child of this node.");
		}

		SelectionNode other = (SelectionNode) getChildren().get(index - 1);

		getChildren().set(index, other);
		getChildren().set(index - 1, child);
	}

	/**
	 * @param child
	 */
	public void moveDown(SelectionNode child) {
		int index = getChildren().indexOf(child);

		if (index < 0) {
			throw new IllegalArgumentException(
					"The specified node is not a child of this node.");
		}

		SelectionNode other = (SelectionNode) getChildren().get(index + 1);

		getChildren().set(index, other);
		getChildren().set(index + 1, child);
	}

	/**
	 * @see org.pivot4j.analytics.ui.navigator.MetadataNode#createChildren()
	 */
	@Override
	protected List<org.primefaces.model.TreeNode> createChildren() {
		List<TreeNode<Member>> nodes = node.getChildren();

		List<org.primefaces.model.TreeNode> children = new ArrayList<org.primefaces.model.TreeNode>(
				nodes.size());

		for (TreeNode<Member> memberNode : nodes) {
			SelectionNode child = new SelectionNode(memberNode, selection);
			child.setParent(this);

			children.add(child);
		}

		return children;
	}
}