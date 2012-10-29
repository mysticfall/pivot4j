/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.eyeq.pivot4j.SortModeCycle;
import com.eyeq.pivot4j.ui.command.CellCommand;
import com.eyeq.pivot4j.ui.command.DrillDownMode;
import com.eyeq.pivot4j.ui.command.ToggleSortCommand;

public abstract class AbstractPivotRenderer implements PivotRenderer {

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
	 * @param context
	 * @return
	 */
	protected Set<CellCommand> getCommands(RenderContext context) {
		if (!isInteractive()) {
			return Collections.emptySet();
		}

		Set<CellCommand> commands = new HashSet<CellCommand>();
		if (drillDownMode != null) {
			for (CellCommand command : drillDownMode.getCommands()) {
				if (command.canExecute(context)) {
					commands.add(command);
				}
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
		Set<CellCommand> commands = getCommands(context);
		startCell(context, commands);
	}

	/**
	 * @param context
	 * @param commands
	 */
	public abstract void startCell(RenderContext context,
			Set<CellCommand> commands);

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
