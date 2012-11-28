/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olap4j.metadata.Member;

public class MemberSelection extends TreeNode<Member> {

	public MemberSelection() {
		super(null);
	}

	/**
	 * @param members
	 */
	public MemberSelection(List<Member> members) {
		super(null);
		addMembers(members);
	}

	/**
	 * @param members
	 */
	public void addMembers(List<Member> members) {
		for (Member member : members) {
			addMember(member);
		}
	}

	/**
	 * @param member
	 */
	public void addMember(Member member) {
		List<Member> ancestors = new ArrayList<Member>(
				member.getAncestorMembers());

		Collections.reverse(ancestors);

		TreeNode<Member> parent = this;

		for (Member ancestor : ancestors) {
			TreeNode<Member> node = findChild(ancestor);

			if (node == null) {
				node = new SelectionNode(ancestor, false);
				parent.addChild(node);
			}

			parent = node;
		}

		TreeNode<Member> node = findChild(member);

		if (node == null) {
			node = new SelectionNode(member, true);
			parent.addChild(node);
		} else {
			((SelectionNode) node).setSelected(true);
		}
	}

	/**
	 * @param member
	 */
	public void removeMember(Member member) {
		SelectionNode node = (SelectionNode) findChild(member);

		if (node == null) {
			throw new IllegalArgumentException(
					"The specified member does not belong to the selection tree.");
		}

		node.getParent().removeChild(node);
	}

	public List<Member> getMembers() {
		final List<Member> members = new ArrayList<Member>();

		TreeNodeCallback<Member> callback = new TreeNodeCallback<Member>() {

			@Override
			public int handleTreeNode(TreeNode<Member> node) {
				if (((SelectionNode) node).isSelected()) {
					members.add(node.getReference());
				}

				return CONTINUE;
			}
		};

		walkChildren(callback);

		return members;
	}

	/**
	 * @param member
	 * @return
	 */
	public boolean canMoveUp(Member member) {
		SelectionNode node = (SelectionNode) findChild(member);

		if (node == null) {
			throw new IllegalArgumentException(
					"The specified member does not belong to the selection tree.");
		}

		int index = node.getParent().getChildren().indexOf(node);
		return index > 0;
	}

	/**
	 * @param member
	 * @return
	 */
	public boolean canMoveDown(Member member) {
		SelectionNode node = (SelectionNode) findChild(member);

		if (node == null) {
			throw new IllegalArgumentException(
					"The specified member does not belong to the selection tree.");
		}

		int index = node.getParent().getChildren().indexOf(node);
		return index < node.getParent().getChildCount() - 1;
	}

	/**
	 * @param member
	 */
	public void moveUp(Member member) {
		SelectionNode node = (SelectionNode) findChild(member);

		if (node == null) {
			throw new IllegalArgumentException(
					"The specified member does not belong to the selection tree.");
		}

		TreeNode<Member> parentNode = node.getParent();
		List<TreeNode<Member>> siblings = parentNode.getChildren();

		int index = siblings.indexOf(node);

		TreeNode<Member> targetNode = siblings.get(index - 1);

		parentNode.removeChild(node);
		parentNode.removeChild(targetNode);

		parentNode.addChild(index - 1, node);
		parentNode.addChild(index, targetNode);
	}

	/**
	 * @param member
	 */
	public void moveDown(Member member) {
		SelectionNode node = (SelectionNode) findChild(member);

		if (node == null) {
			throw new IllegalArgumentException(
					"The specified member does not belong to the selection tree.");
		}

		TreeNode<Member> parentNode = node.getParent();
		List<TreeNode<Member>> siblings = parentNode.getChildren();

		int index = siblings.indexOf(node);

		TreeNode<Member> targetNode = siblings.get(index + 1);

		parentNode.removeChild(node);
		parentNode.removeChild(targetNode);

		parentNode.addChild(index, targetNode);
		parentNode.addChild(index + 1, node);
	}

	static class SelectionNode extends TreeNode<Member> {

		private boolean selected;

		/**
		 * @param member
		 * @param selected
		 */
		SelectionNode(Member member, boolean selected) {
			super(member);
			this.selected = selected;
		}

		boolean isSelected() {
			return selected;
		}

		/**
		 * @param selected
		 */
		void setSelected(boolean selected) {
			this.selected = selected;
		}
	}
}
