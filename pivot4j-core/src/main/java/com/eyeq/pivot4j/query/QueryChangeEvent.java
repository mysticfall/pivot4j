/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.query;

import java.util.EventObject;

/**
 * Informs a listener that the query model has changed.
 */
public class QueryChangeEvent extends EventObject {

	private static final long serialVersionUID = -4579513913541469377L;

	/**
	 * Constructor for QueryChangeEvent.
	 * 
	 * @param source
	 */
	public QueryChangeEvent(QueryAdapter source) {
		super(source);
	}

	public QueryAdapter getQueryAdapter() {
		return (QueryAdapter) getSource();
	}
}
