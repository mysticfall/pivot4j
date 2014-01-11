package org.pivot4j.analytics.exception;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

public class Pivot4JExceptionHandlerFactory extends ExceptionHandlerFactory {

	private ExceptionHandlerFactory parent;

	/**
	 * @param parent
	 */
	public Pivot4JExceptionHandlerFactory(ExceptionHandlerFactory parent) {
		this.parent = parent;
	}

	/**
	 * @see javax.faces.context.ExceptionHandlerFactory#getExceptionHandler()
	 */
	@Override
	public ExceptionHandler getExceptionHandler() {
		return new Pivot4JExceptionHandler(parent.getExceptionHandler());
	}
}
