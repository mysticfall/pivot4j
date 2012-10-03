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
import java.io.Serializable;
import java.io.StringWriter;

public abstract class AbstractHtmlElement implements HtmlElement, Serializable {

	private static final long serialVersionUID = 4689947586988026274L;

	private String id;

	private String style;

	private String styleClass;

	/**
	 * @see com.eyeq.pivot4j.ui.html.HtmlElement#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.html.HtmlElement#setId(java.lang.String)
	 */
	@Override
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.html.HtmlElement#getStyle()
	 */
	@Override
	public String getStyle() {
		return style;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.html.HtmlElement#setStyle(java.lang.String)
	 */
	@Override
	public void setStyle(String style) {
		this.style = style;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.html.HtmlElement#getStyleClass()
	 */
	@Override
	public String getStyleClass() {
		return styleClass;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.html.HtmlElement#setStyleClass(java.lang.String)
	 */
	@Override
	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringWriter writer = new StringWriter();
		writeHtml(new PrintWriter(writer), 0);
		return writer.toString();
	}
}
