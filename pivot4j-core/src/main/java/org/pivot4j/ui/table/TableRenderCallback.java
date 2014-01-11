/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.table;

import org.pivot4j.ui.RenderCallback;

public interface TableRenderCallback extends RenderCallback<TableRenderContext> {

	/**
	 * @param context
	 */
	void startTable(TableRenderContext context);

	/**
	 * @param context
	 */
	void startHeader(TableRenderContext context);

	/**
	 * @param context
	 */
	void endHeader(TableRenderContext context);

	/**
	 * @param context
	 */
	void startBody(TableRenderContext context);

	/**
	 * @param context
	 */
	void startRow(TableRenderContext context);

	/**
	 * @param context
	 */
	void startCell(TableRenderContext context);

	/**
	 * @param context
	 */
	void endCell(TableRenderContext context);

	/**
	 * @param context
	 */
	void endRow(TableRenderContext context);

	/**
	 * @param context
	 */
	void endBody(TableRenderContext context);

	/**
	 * @param context
	 */
	void endTable(TableRenderContext context);
}
