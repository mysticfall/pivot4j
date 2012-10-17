/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;


/**
 * Visitor for MDX parse expressions
 */
public interface ExpVisitor {

	void visitCompoundId(CompoundId visio);

	void visitFormula(Formula visio);

	void visitFunCall(FunCall visio);

	void visitLiteral(Literal visio);

	void visitMemberProperty(MemberProperty visio);

	void visitParsedQuery(ParsedQuery visio);

	void visitQueryAxis(QueryAxis visio);

	void visitDimension(DimensionExp visio);

	void visitHierarchy(HierarchyExp visio);

	void visitLevel(LevelExp visio);

	void visitMember(MemberExp visio);
}
