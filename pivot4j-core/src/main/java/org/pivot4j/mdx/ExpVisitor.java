/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.mdx;

/**
 * Visitor for MDX parse expressions
 */
public interface ExpVisitor {

	void visitCompoundId(CompoundId exp);

	void visitLiteral(Literal exp);

	void visitFunCall(FunCall exp);

	void visitStatement(MdxStatement exp);

	void visitQueryAxis(QueryAxis exp);

	void visitFormula(Formula exp);

	void visitFormulaProperty(Formula.Property exp);

	void visitSapVariable(SapVariable exp);

	void visitSapVariableValue(SapVariable.Value exp);

	void visitMemberParameter(MemberParameter exp);

	void visitValueParameter(ValueParameter exp);
}
