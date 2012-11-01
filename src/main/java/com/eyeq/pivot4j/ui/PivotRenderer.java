/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import com.eyeq.pivot4j.ui.command.CellCommand;

public interface PivotRenderer {

	void initialize();

	CellCommand getCommand(String name);

	void addCommand(CellCommand command);

	void removeCommand(String name);

	boolean getHideSpans();

	void setHideSpans(boolean hideSpans);

	boolean getShowParentMembers();

	void setShowParentMembers(boolean showParentMembers);

	boolean getShowDimensionTitle();

	void setShowDimensionTitle(boolean showDimensionTitle);

	boolean getEnableColumnDrillDown();

	void setEnableColumnDrillDown(boolean enableColumnDrillDown);

	boolean getEnableRowDrillDown();

	void setEnableRowDrillDown(boolean enableRowDrillDown);

	boolean getEnableSort();

	void setEnableSort(boolean enableSort);

	SortMode getSortMode();

	void setSortMode(SortMode mode);

	String getDrillDownMode();

	void setDrillDownMode(String mode);

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
