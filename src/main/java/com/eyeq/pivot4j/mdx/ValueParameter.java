/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;

public class ValueParameter extends ExpressionParameter {

	private static final long serialVersionUID = 4812754724151878751L;

	public ValueParameter() {
	}

	/**
	 * @param namespace
	 * @param function
	 */
	public ValueParameter(String namespace, String expression) {
		super(namespace, expression);
	}

	/**
	 * 
	 * @see java.lang.Object#clone()
	 */
	public ValueParameter clone() {
		ValueParameter clone = new ValueParameter(getNamespace(),
				getExpression());
		clone.setEvaluated(isEvaluated());
		clone.setResult(getResult());

		return clone;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#accept
	 */
	public void accept(ExpVisitor visitor) {
		visitor.visitValueParameter(this);
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.ExpressionParameter#toMdx(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	protected String toMdx(String namespace, String expression) {
		StringBuilder sb = new StringBuilder();

		sb.append("${");

		if (namespace != null) {
			sb.append(namespace);
			sb.append(":");
		}

		if (expression != null) {
			sb.append(expression.replaceAll("]", "]]"));
		}

		sb.append("}");

		return sb.toString();
	}
}
