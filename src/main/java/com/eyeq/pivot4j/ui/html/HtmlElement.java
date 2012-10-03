/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.html;

import java.io.PrintWriter;

public interface HtmlElement {

	String getId();
	
	void setId(String id);

	String getStyle();

	/**
	 * @param style
	 */
	void setStyle(String style);

	String getStyleClass();

	/**
	 * @param style
	 */
	void setStyleClass(String styleClass);

	/**
	 * @param writer
	 * @param indent
	 */
	void writeHtml(PrintWriter writer, int indent);
}
