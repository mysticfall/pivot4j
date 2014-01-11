/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.mdx;

public class ValueParameter extends ExpressionParameter {

	private static final long serialVersionUID = 4812754724151878751L;

	public ValueParameter() {
	}

	/**
	 * @param expression
	 */
	public ValueParameter(String expression) {
		super(expression);
	}

	/**
	 * @see org.pivot4j.mdx.Exp#copy()
	 */
	public ValueParameter copy() {
		ValueParameter clone = new ValueParameter(getExpression());
		clone.setEvaluated(isEvaluated());
		clone.setResult(getResult());

		return clone;
	}

	/**
	 * @see org.pivot4j.mdx.Exp#accept(org.pivot4j.mdx.ExpVisitor)
	 */
	public void accept(ExpVisitor visitor) {
		visitor.visitValueParameter(this);
	}

	/**
	 * @see org.pivot4j.mdx.ExpressionParameter#toMdx(java.lang.String)
	 */
	@Override
	protected String toMdx(String expression) {
		StringBuilder sb = new StringBuilder();

		sb.append("${");

		if (expression != null) {
			sb.append(expression.replaceAll("]", "]]"));
		}

		sb.append("}");

		return sb.toString();
	}
}
