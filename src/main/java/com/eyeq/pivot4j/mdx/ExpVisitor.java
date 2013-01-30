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

	void visitCompoundId(CompoundId exp);

	void visitFormula(Formula exp);

	void visitFunCall(FunCall exp);

	void visitLiteral(Literal exp);

	void visitMdxQuery(MdxQuery exp);

	void visitQueryAxis(QueryAxis exp);

	void visitDimension(DimensionExp exp);

	void visitHierarchy(HierarchyExp exp);

	void visitLevel(LevelExp exp);

	void visitMember(MemberExp exp);

	void visitSapVariable(SapVariable exp);

	void visitMemberProperty(MemberProperty exp);
}
