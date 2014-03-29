package org.pivot4j.analytics.component.tree;

import java.util.List;

import org.primefaces.model.TreeNode;

/**
 * Temporary workaround for #144.
 * 
 * See the related issue report <a
 * href="https://github.com/mysticfall/pivot4j/issues/144">here</a>.
 */
public class Tree extends org.primefaces.component.tree.Tree {

	/**
	 * @see org.primefaces.component.api.UITree#populateRowKeys(org.primefaces.model.TreeNode,
	 *      java.util.List)
	 */
	@Override
	public void populateRowKeys(TreeNode node, List<String> keys) {
		// Overriden to skip recursive row key population, which not only causes
		// the issue described in #144, but also defeats purpose of lazy loading
		// the children.
		//
		// super.populateRowKeys(node, keys);
	}
}
