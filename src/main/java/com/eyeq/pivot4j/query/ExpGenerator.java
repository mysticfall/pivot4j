/*
 * ====================================================================
 * This software is subject to the terms of the Common Publilc License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.query;

import java.util.ArrayList;
import java.util.List;

import com.eyeq.pivot4j.mdx.Exp;
import com.eyeq.pivot4j.mdx.ExpNode;
import com.eyeq.pivot4j.mdx.Syntax;
import com.eyeq.pivot4j.util.TreeNode;

/**
 * Generate MDX expression for TreeNode
 */
public class ExpGenerator {

	private QuaxUtil quaxUtil;

	private ExpNode rootNode;

	private int nDimension;

	/**
	 * @param rootNode
	 * @param nDimension
	 * @param quaxUtil
	 */
	public void init(ExpNode rootNode, int nDimension, QuaxUtil quaxUtil) {
		this.rootNode = rootNode;
		this.nDimension = nDimension;
		this.quaxUtil = quaxUtil;
	}

	/**
	 * Generate MDX Expression
	 * 
	 * @param genHierarchize
	 * @return
	 */
	public Exp generate() {
		Exp exp = null;

		List<TreeNode<Exp>> nodes = rootNode.getChildren();

		// single members (nDimension = 1) are enclosed in set brackets
		// collect single members
		List<Exp> openSet = new ArrayList<Exp>();

		// loop over top level nodes
		for (TreeNode<Exp> node : nodes) {
			Exp expForNode = genExpForNode(node, nDimension);

			boolean closeOpenSet = false;
			if (nDimension == 1) {
				if (quaxUtil.isMember(expForNode)) {
					openSet.add(expForNode);
					continue;
				} else
					closeOpenSet = true;
			} else {
				if (quaxUtil.isFunCallTo(expForNode, "()")) {
					openSet.add(expForNode);
					continue;
				} else
					closeOpenSet = true;
			}

			if (closeOpenSet && !openSet.isEmpty()) {
				// close open set
				Exp[] expArray = openSet.toArray(new Exp[openSet.size()]);

				Exp set = quaxUtil.createFunCall("{}", expArray, Syntax.Braces);

				if (exp == null) {
					exp = set;
				} else {
					// generate Union
					exp = quaxUtil.createFunCall("Union",
							new Exp[] { exp, set }, Syntax.Function);
				}

				openSet.clear();
			}

			if (exp == null) {
				exp = expForNode;
			} else {
				// generate Union of Exp and expForNode
				exp = quaxUtil.createFunCall("Union", new Exp[] { exp,
						expForNode }, Syntax.Function);
			}
		}

		if (!openSet.isEmpty()) {
			// close open set
			Exp[] expArray = openSet.toArray(new Exp[openSet.size()]);
			Exp set = quaxUtil.createFunCall("{}", expArray, Syntax.Braces);

			if (exp == null) {
				exp = set;
			} else {
				// generate Union
				exp = quaxUtil.createFunCall("Union", new Exp[] { exp, set },
						Syntax.Function);
			}

			openSet.clear();
		}

		return exp;
	}

	/**
	 * Recursively generate Exp for a node
	 * 
	 * @param node
	 * @return
	 */
	private Exp genExpForNode(TreeNode<Exp> node, int untilIndex) {
		Exp eNode = node.getReference();
		if (node.getLevel() == untilIndex) {
			return eNode; // last dimension
		}

		// use tuple representation if possible
		Exp[] tuple = genTuple(node);
		if (tuple != null) {
			if (tuple.length == 1) {
				return tuple[0];
			} else {
				return quaxUtil.createFunCall("()", tuple, Syntax.Parentheses);
			}
		}

		// generate CrossJoin
		Exp exp = null;

		List<TreeNode<Exp>> childNodes = node.getChildren();
		for (TreeNode<Exp> childNode : childNodes) {
			Exp childExp = genExpForNode(childNode, untilIndex);

			Exp eSet;
			if (!quaxUtil.isMember(eNode)) {
				// FunCall
				eSet = eNode;
			} else {
				// member
				eSet = quaxUtil.createFunCall("{}", new Exp[] { eNode },
						Syntax.Braces);
			}

			if (childExp == null) {
				exp = eSet;
			} else {
				Exp childSet = bracesAround(childExp);

				Exp cj = quaxUtil.createFunCall("CrossJoin", new Exp[] { eSet,
						childSet }, Syntax.Function);
				if (exp == null) {
					exp = cj;
				} else {
					exp = quaxUtil.createFunCall("Union",
							new Exp[] { exp, cj }, Syntax.Function);
				}
			}
		}
		return exp;
	}

	/**
	 * put braces around single member or single tuple
	 * 
	 * @param oExp
	 * @return set exp
	 */
	private Exp bracesAround(Exp oExp) {
		Exp oSet;

		if (quaxUtil.isMember(oExp) || quaxUtil.isFunCallTo(oExp, "()")) {
			oSet = quaxUtil.createFunCall("{}", new Exp[] { oExp },
					Syntax.Braces);
		} else {
			oSet = oExp;
		}

		return oSet;
	}

	/**
	 * generate Tuple Exp
	 * 
	 * @param node
	 * @return
	 */
	private Exp[] genTuple(TreeNode<Exp> node) {
		if (!quaxUtil.isMember(node.getReference())) {
			return null;
		}

		int size = nDimension - node.getLevel() + 1;
		if (size == 1) {
			return new Exp[] { node.getReference() }; // single member
		}

		List<TreeNode<Exp>> childNodes = node.getChildren();
		if (childNodes.size() != 1) {
			return null;
		}

		Exp[] nextTuple = genTuple(childNodes.get(0));
		if (nextTuple == null) {
			return null;
		}

		Exp[] tupleMembers = new Exp[size];
		tupleMembers[0] = node.getReference();
		for (int i = 1; i < tupleMembers.length; i++) {
			tupleMembers[i] = nextTuple[i - 1];
		}

		return tupleMembers;
	}
}
