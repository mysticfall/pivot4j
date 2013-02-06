/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.el;

import com.eyeq.pivot4j.PivotException;

public class EvaluationFailedException extends PivotException {

	private static final long serialVersionUID = 5164243103322308072L;

	private String namespace;

	private String expression;

	public EvaluationFailedException() {
	}

	/**
	 * @param msg
	 * @param namespace
	 * @param expression
	 */
	public EvaluationFailedException(String msg, String namespace,
			String expression) {
		super(msg);

		this.namespace = namespace;
		this.expression = expression;
	}

	/**
	 * @param msg
	 * @param namespace
	 * @param expression
	 * @param cause
	 */
	public EvaluationFailedException(String msg, String namespace,
			String expression, Throwable cause) {
		super(msg, cause);

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
}
