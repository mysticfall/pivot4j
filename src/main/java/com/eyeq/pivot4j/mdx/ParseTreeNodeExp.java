/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;

import org.olap4j.mdx.ParseTreeNode;
import org.olap4j.type.DimensionType;
import org.olap4j.type.HierarchyType;
import org.olap4j.type.LevelType;
import org.olap4j.type.MemberType;
import org.olap4j.type.Type;

public class ParseTreeNodeExp implements Exp {

	private ParseTreeNode node;

	/**
	 * @param node
	 */
	public ParseTreeNodeExp(ParseTreeNode node) {
		this.node = node;
	}

	public ParseTreeNode getNode() {
		return node;
	}

	public Type getType() {
		return node.getType();
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.Exp#toMdx()
	 */
	public String toMdx() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ParseTreeNodeExp clone() {
		return new ParseTreeNodeExp(node.deepCopy());
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.Exp#accept(com.eyeq.pivot4j.mdx.ExpVisitor)
	 */
	public void accept(ExpVisitor visitor) {
		Type type = getType();

		if (type instanceof MemberType) {
			visitor.visitMember(((MemberType) type).getMember());
		} else if (type instanceof LevelType) {
			visitor.visitLevel(((LevelType) type).getLevel());
		} else if (type instanceof DimensionType) {
			visitor.visitDimension(((DimensionType) type).getDimension());
		} else if (type instanceof HierarchyType) {
			visitor.visitHierarchy(((HierarchyType) type).getHierarchy());
		}
	}
}
