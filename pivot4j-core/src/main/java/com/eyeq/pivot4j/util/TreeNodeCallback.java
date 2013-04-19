/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.util;

/**
 * handle call back for position tree
 */
public interface TreeNodeCallback<T> {

	int CONTINUE = 0;

	int CONTINUE_SIBLING = 1;

	int CONTINUE_PARENT = 2;

	int BREAK = 3;

	/**
	 * @param node
	 *            the current node to handle
	 * @return 0 continue tree walk 1 break this node (continue sibling) 2 break
	 *         this level (continue parent level) 3 break tree walk
	 */
	int handleTreeNode(TreeNode<T> node);
}
