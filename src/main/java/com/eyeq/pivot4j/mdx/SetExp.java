/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;

import org.olap4j.metadata.Hierarchy;

import com.eyeq.pivot4j.query.CalcSetMode;

/**
 * Wrapper for Set Object to be placed on a query axis
 */
public class SetExp implements Exp {

	private CalcSetMode mode;

	private Exp expression;

	private Hierarchy hierarchy;

	/**
	 * @param mode
	 * @param expression
	 * @param hierarchy
	 */
	public SetExp(CalcSetMode mode, Exp expression, Hierarchy hierarchy) {
		this.mode = mode;
		this.expression = expression;
		this.hierarchy = hierarchy;
	}

	/**
	 * @return
	 */
	public Hierarchy getHierarchy() {
		return hierarchy;
	}

	/**
	 * @return
	 */
	public CalcSetMode getMode() {
		return mode;
	}

	/**
	 * @return
	 */
	public Exp getExpression() {
		return expression;
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.Exp#toMdx()
	 */
	public String toMdx() {
		return expression.toMdx();
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.Exp#accept(com.eyeq.pivot4j.mdx.ExpVisitor)
	 */
	public void accept(ExpVisitor visitor) {
		expression.accept(visitor);
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public SetExp clone() {
		return new SetExp(mode, expression.clone(), hierarchy);
	}
}
