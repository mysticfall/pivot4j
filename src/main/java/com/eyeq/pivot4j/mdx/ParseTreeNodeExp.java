/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;

import java.io.StringWriter;

import org.olap4j.mdx.ParseTreeNode;
import org.olap4j.mdx.ParseTreeWriter;
import org.olap4j.mdx.parser.MdxParser;
import org.olap4j.type.DimensionType;
import org.olap4j.type.HierarchyType;
import org.olap4j.type.LevelType;
import org.olap4j.type.MemberType;
import org.olap4j.type.Type;

public class ParseTreeNodeExp implements Exp {

	private static final long serialVersionUID = -1850996813470282590L;

	private transient ParseTreeNode node;

	private String mdx;

	/**
	 * @param node
	 */
	public ParseTreeNodeExp(ParseTreeNode node) {
		if (node == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'node'.");
		}

		this.node = node;

		StringWriter writer = new StringWriter();
		node.unparse(new ParseTreeWriter(writer));
		this.mdx = writer.toString();
	}

	public ParseTreeNode getNode() {
		return node;
	}

	public Type getType() {
		checkState();
		return node.getType();
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.Exp#toMdx()
	 */
	public String toMdx() {
		return mdx;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ParseTreeNodeExp clone() {
		checkState();
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

	private void checkState() {
		if (node == null) {
			throw new IllegalStateException(
					"Nested parse tree node has not been restored yet.");
		}
	}

	/**
	 * @param parser
	 */
	public void restore(MdxParser parser) {
		this.node = parser.parseExpression(mdx);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toMdx();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 31 + mdx.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		// ParseTreeNode does not override equals()!
		ParseTreeNodeExp other = (ParseTreeNodeExp) obj;
		return toMdx().equals(other.toMdx());
	}
}
