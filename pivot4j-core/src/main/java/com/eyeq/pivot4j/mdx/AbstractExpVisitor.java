/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;

import com.eyeq.pivot4j.mdx.Formula.Property;
import com.eyeq.pivot4j.mdx.SapVariable.Value;

public abstract class AbstractExpVisitor implements ExpVisitor {

	/**
	 * @see com.eyeq.pivot4j.mdx.ExpVisitor#visitCompoundId(com.eyeq.pivot4j.mdx.
	 *      CompoundId)
	 */
	@Override
	public void visitCompoundId(CompoundId exp) {
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.ExpVisitor#visitLiteral(com.eyeq.pivot4j.mdx.Literal
	 *      )
	 */
	@Override
	public void visitLiteral(Literal exp) {
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.ExpVisitor#visitFunCall(com.eyeq.pivot4j.mdx.FunCall
	 *      )
	 */
	@Override
	public void visitFunCall(FunCall exp) {
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.ExpVisitor#visitStatement(com.eyeq.pivot4j.mdx.
	 *      MdxStatement)
	 */
	@Override
	public void visitStatement(MdxStatement exp) {
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.ExpVisitor#visitQueryAxis(com.eyeq.pivot4j.mdx.QueryAxis
	 *      )
	 */
	@Override
	public void visitQueryAxis(QueryAxis exp) {
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.ExpVisitor#visitFormula(com.eyeq.pivot4j.mdx.Formula
	 *      )
	 */
	@Override
	public void visitFormula(Formula exp) {
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.ExpVisitor#visitFormulaProperty(com.eyeq.pivot4j
	 *      .mdx.Formula.Property)
	 */
	@Override
	public void visitFormulaProperty(Property exp) {
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.ExpVisitor#visitSapVariable(com.eyeq.pivot4j.mdx
	 *      .SapVariable)
	 */
	@Override
	public void visitSapVariable(SapVariable exp) {
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.ExpVisitor#visitSapVariableValue(com.eyeq.pivot4j
	 *      .mdx.SapVariable.Value)
	 */
	@Override
	public void visitSapVariableValue(Value exp) {
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.ExpVisitor#visitMemberParameter(com.eyeq.pivot4j
	 *      .mdx.MemberParameter)
	 */
	@Override
	public void visitMemberParameter(MemberParameter exp) {
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.ExpVisitor#visitValueParameter(com.eyeq.pivot4j
	 *      .mdx.ValueParameter)
	 */
	@Override
	public void visitValueParameter(ValueParameter exp) {
	}
}
