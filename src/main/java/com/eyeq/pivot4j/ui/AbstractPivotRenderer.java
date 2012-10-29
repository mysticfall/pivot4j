/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olap4j.Axis;

import com.eyeq.pivot4j.SortModeCycle;
import com.eyeq.pivot4j.ui.command.CellCommand;
import com.eyeq.pivot4j.ui.command.DrillDownCommand;
import com.eyeq.pivot4j.ui.command.DrillDownMode;
import com.eyeq.pivot4j.ui.command.SortCommand;
import com.eyeq.pivot4j.ui.command.ToggleSortCommand;

public abstract class AbstractPivotRenderer implements PivotRenderer {

	private boolean enableColumnDrillDown = true;

	private boolean enableRowDrillDown = true;

	private boolean enableSort = true;

	private SortModeCycle sortCycle = SortModeCycle.BASIC;

	private DrillDownMode drillDownMode = DrillDownMode.POSITION;

	/**
	 * @return the sortCycle
	 */
	public SortModeCycle getSortCycle() {
		return sortCycle;
	}

	/**
	 * @param sortCycle
	 *            the sortCycle to set
	 */
	public void setSortCycle(SortModeCycle sortCycle) {
		this.sortCycle = sortCycle;
	}

	/**
	 * @return the drillDownMode
	 */
	public DrillDownMode getDrillDownMode() {
		return drillDownMode;
	}

	/**
	 * @param drillDownMode
	 *            the drillDownMode to set
	 */
	public void setDrillDownMode(DrillDownMode drillDownMode) {
		this.drillDownMode = drillDownMode;
	}

	protected boolean isInteractive() {
		return true;
	}

	/**
	 * @return the enableColumnDrillDown
	 */
	public boolean getEnableColumnDrillDown() {
		return enableColumnDrillDown;
	}

	/**
	 * @param enableColumnDrillDown
	 *            the enableColumnDrillDown to set
	 */
	public void setEnableColumnDrillDown(boolean enableColumnDrillDown) {
		this.enableColumnDrillDown = enableColumnDrillDown;
	}

	/**
	 * @return the enableRowDrillDown
	 */
	public boolean getEnableRowDrillDown() {
		return enableRowDrillDown;
	}

	/**
	 * @param enableRowDrillDown
	 *            the enableRowDrillDown to set
	 */
	public void setEnableRowDrillDown(boolean enableRowDrillDown) {
		this.enableRowDrillDown = enableRowDrillDown;
	}

	/**
	 * @return the enableSort
	 */
	public boolean getEnableSort() {
		return enableSort;
	}

	/**
	 * @param enableSort
	 *            the enableSort to set
	 */
	public void setEnableSort(boolean enableSort) {
		this.enableSort = enableSort;
	}

	/**
	 * @param name
	 * @return
	 */
	public CellCommand getCommand(String name) {
		for (CellCommand command : getCommands()) {
			if (name.equals(command.getName())) {
				return command;
			}
		}

		return null;
	}

	/**
	 * @param context
	 * @return
	 */
	protected List<CellCommand> getCommands(RenderContext context) {
		if (!isInteractive()) {
			return Collections.emptyList();
		}

		List<CellCommand> commands = new ArrayList<CellCommand>();
		for (CellCommand command : getCommands()) {
			if (command instanceof DrillDownCommand) {
				if (Axis.COLUMNS.equals(context.getAxis())
						&& enableColumnDrillDown
						|| Axis.ROWS.equals(context.getAxis())
						&& enableRowDrillDown) {
					if (command.canExecute(context)) {
						commands.add(command);
					}
				}
			} else if (command instanceof SortCommand) {
				if (enableSort && command.canExecute(context)) {
					commands.add(command);
				}
			}
		}

		return commands;
	}

	/**
	 * @param context
	 * @return
	 */
	protected List<CellCommand> getCommands() {
		List<CellCommand> commands = new ArrayList<CellCommand>();

		if (drillDownMode != null) {
			for (CellCommand command : drillDownMode.getCommands()) {
				commands.add(command);
			}
		}

		if (sortCycle != null) {
			commands.add(new ToggleSortCommand(sortCycle));
		}

		return commands;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#startCell(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void startCell(RenderContext context) {
		List<CellCommand> commands = getCommands(context);
		startCell(context, commands);
	}

	/**
	 * @param context
	 * @param commands
	 */
	public abstract void startCell(RenderContext context,
			List<CellCommand> commands);

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#cellContent(com.eyeq.pivot4j.ui.RenderContext)
	 */
	public void cellContent(RenderContext context) {
		cellContent(context, getCellLabel(context));
	}

	public abstract void cellContent(RenderContext context, String label);

	/**
	 * @param context
	 * @return
	 */
	protected String getCellLabel(RenderContext context) {
		String label;

		switch (context.getCellType()) {
		case ColumnHeader:
		case RowHeader:
			label = context.getMember().getCaption();
			break;
		case ColumnTitle:
		case RowTitle:
			if (context.getLevel() != null) {
				label = context.getLevel().getCaption();
			} else if (context.getHierarchy() != null) {
				label = context.getHierarchy().getCaption();
			} else {
				label = null;
			}
			break;
		case Value:
			label = context.getCell().getFormattedValue();
			break;
		case None:
		default:
			label = null;
			break;
		}

		return label;
	}
}
