/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

public interface PivotLayoutCallback {

	void startTable(RenderContext context);

	void startHeader(RenderContext context);

	void endHeader(RenderContext context);

	void startBody(RenderContext context);

	void endBody(RenderContext context);

	void endTable(RenderContext context);

	void startRow(RenderContext context);

	void endRow(RenderContext context);

	void startCell(RenderContext context);

	void cellContent(RenderContext context);

	void endCell(RenderContext context);
}
