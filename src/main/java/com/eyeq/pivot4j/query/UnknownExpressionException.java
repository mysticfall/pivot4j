/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.query;

public class UnknownExpressionException extends Exception {

	private static final long serialVersionUID = 7929488196577893455L;

	private String expression;

	/**
	 * Constructor for UnknownExpressionException.
	 * 
	 * @param expression
	 */
	public UnknownExpressionException(String expression) {
		this("Unknown query expression : " + expression, expression);
	}

	/**
	 * @param msg
	 * @param expression
	 */
	public UnknownExpressionException(String msg, String expression) {
		super(msg);
		this.expression = expression;
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
