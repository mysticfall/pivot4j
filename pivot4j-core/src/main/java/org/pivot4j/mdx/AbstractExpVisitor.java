/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.mdx;

import org.pivot4j.mdx.Formula.Property;
import org.pivot4j.mdx.SapVariable.Value;

public abstract class AbstractExpVisitor implements ExpVisitor {

    /**
     * @see org.pivot4j.mdx.ExpVisitor#visitCompoundId(org.pivot4j.mdx.
     * CompoundId)
     */
    @Override
    public void visitCompoundId(CompoundId exp) {
    }

    /**
     * @see org.pivot4j.mdx.ExpVisitor#visitLiteral(org.pivot4j.mdx.Literal )
     */
    @Override
    public void visitLiteral(Literal exp) {
    }

    /**
     * @see org.pivot4j.mdx.ExpVisitor#visitFunCall(org.pivot4j.mdx.FunCall )
     */
    @Override
    public void visitFunCall(FunCall exp) {
    }

    /**
     * @see org.pivot4j.mdx.ExpVisitor#visitStatement(org.pivot4j.mdx.
     * MdxStatement)
     */
    @Override
    public void visitStatement(MdxStatement exp) {
    }

    /**
     * @see org.pivot4j.mdx.ExpVisitor#visitQueryAxis(org.pivot4j.mdx.QueryAxis
     * )
     */
    @Override
    public void visitQueryAxis(QueryAxis exp) {
    }

    /**
     * @see org.pivot4j.mdx.ExpVisitor#visitFormula(org.pivot4j.mdx.Formula )
     */
    @Override
    public void visitFormula(Formula exp) {
    }

    /**
     * @see
     * org.pivot4j.mdx.ExpVisitor#visitFormulaProperty(org.pivot4j.mdx.Formula.Property)
     */
    @Override
    public void visitFormulaProperty(Property exp) {
    }

    /**
     * @see org.pivot4j.mdx.ExpVisitor#visitSapVariable(org.pivot4j.mdx
     * .SapVariable)
     */
    @Override
    public void visitSapVariable(SapVariable exp) {
    }

    /**
     * @see
     * org.pivot4j.mdx.ExpVisitor#visitSapVariableValue(org.pivot4j.mdx.SapVariable.Value)
     */
    @Override
    public void visitSapVariableValue(Value exp) {
    }

    /**
     * @see
     * org.pivot4j.mdx.ExpVisitor#visitMemberParameter(org.pivot4j.mdx.MemberParameter)
     */
    @Override
    public void visitMemberParameter(MemberParameter exp) {
    }

    /**
     * @see
     * org.pivot4j.mdx.ExpVisitor#visitValueParameter(org.pivot4j.mdx.ValueParameter)
     */
    @Override
    public void visitValueParameter(ValueParameter exp) {
    }
}
