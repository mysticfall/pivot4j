/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.export;

import java.io.OutputStream;

import com.eyeq.pivot4j.ui.PivotRenderer;

public interface PivotExporter extends PivotRenderer {

	String getContentType();

	void setOutputStream(OutputStream out);
}
