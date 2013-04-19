/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;

public abstract class ExpressionParameter extends AbstractExp {

	private static final long serialVersionUID = -8720548361608653946L;

	private String namespace;

	private String expression;

	private String result;

	private boolean evaluated = false;

	public ExpressionParameter() {
	}

	/**
	 * @param namespace
	 * @param expression
	 */
	public ExpressionParameter(String namespace, String expression) {
		this.namespace = namespace;
		this.expression = expression;
	}

	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * @param namespace
	 *            the namespace to set
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * @return the expression
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * @param expression
	 *            the expression to set
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}

	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * @return the evaluated
	 */
	public boolean isEvaluated() {
		return evaluated;
	}

	/**
	 * @param evaluated
	 *            the evaluated to set
	 */
	public void setEvaluated(boolean evaluated) {
		this.evaluated = evaluated;
	}

	/**
	 * @see com.eyeq.pivot4j.mdx.Exp#toMdx()
	 */
	@Override
	public String toMdx() {
		if (evaluated) {
			return result == null ? "" : result;
		} else {
			return toMdx(namespace, expression);
		}
	}

	/**
	 * @param namespace
	 * @param expression
	 * @return
	 */
	protected abstract String toMdx(String namespace, String expression);
}
