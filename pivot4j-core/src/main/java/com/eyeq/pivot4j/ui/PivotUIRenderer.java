/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import com.eyeq.pivot4j.sort.SortMode;
import com.eyeq.pivot4j.ui.command.CellCommand;

public interface PivotUIRenderer extends PivotRenderer {

	CellCommand<?> getCommand(String name);

	void addCommand(CellCommand<?> command);

	void removeCommand(String name);

	boolean getEnableColumnDrillDown();

	void setEnableColumnDrillDown(boolean enableColumnDrillDown);

	boolean getEnableRowDrillDown();

	void setEnableRowDrillDown(boolean enableRowDrillDown);

	boolean getEnableSort();

	void setEnableSort(boolean enableSort);

	boolean getEnableDrillThrough();

	void setEnableDrillThrough(boolean enableDrillThrough);

	SortMode getSortMode();

	void setSortMode(SortMode mode);

	String getDrillDownMode();

	void setDrillDownMode(String mode);
}
