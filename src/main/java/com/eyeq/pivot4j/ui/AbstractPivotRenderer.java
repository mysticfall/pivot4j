/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eyeq.pivot4j.StateHolder;
import com.eyeq.pivot4j.ui.command.CellCommand;
import com.eyeq.pivot4j.ui.command.DrillCollapseMemberCommand;
import com.eyeq.pivot4j.ui.command.DrillCollapsePositionCommand;
import com.eyeq.pivot4j.ui.command.DrillDownCommand;
import com.eyeq.pivot4j.ui.command.DrillDownReplaceCommand;
import com.eyeq.pivot4j.ui.command.DrillExpandMemberCommand;
import com.eyeq.pivot4j.ui.command.DrillExpandPositionCommand;
import com.eyeq.pivot4j.ui.command.DrillUpReplaceCommand;
import com.eyeq.pivot4j.ui.command.ToggleSortCommand;

public abstract class AbstractPivotRenderer implements PivotRenderer,
		StateHolder {

	private boolean enableColumnDrillDown;

	private boolean enableRowDrillDown;

	private boolean enableSort = true;

	private SortMode sortMode = SortMode.BASIC;

	private String drillDownMode = DrillDownCommand.MODE_POSITION;

	private Map<String, CellCommand> commands = new HashMap<String, CellCommand>();

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#initialize()
	 */
	public void initialize() {
		registerCommands();
	}

	/**
	 * @return the sortMode
	 */
	public SortMode getSortMode() {
		return sortMode;
	}

	/**
	 * @param sortMode
	 *            the sortMode to set
	 */
	public void setSortMode(SortMode sortMode) {
		this.sortMode = sortMode;
	}

	/**
	 * @return the drillDownMode
	 */
	public String getDrillDownMode() {
		return drillDownMode;
	}

	/**
	 * @param drillDownMode
	 *            the drillDownMode to set
	 */
	public void setDrillDownMode(String drillDownMode) {
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

	protected void registerCommands() {
		addCommand(new DrillExpandPositionCommand(this));
		addCommand(new DrillCollapsePositionCommand(this));
		addCommand(new DrillExpandMemberCommand(this));
		addCommand(new DrillCollapseMemberCommand(this));
		addCommand(new DrillDownReplaceCommand(this));
		addCommand(new DrillUpReplaceCommand(this));
		addCommand(new ToggleSortCommand(this));
	}

	/**
	 * @param name
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getCommand(java.lang.String)
	 */
	public CellCommand getCommand(String name) {
		return commands.get(name);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#addCommand(com.eyeq.pivot4j.ui.command
	 *      .CellCommand)
	 */
	@Override
	public void addCommand(CellCommand command) {
		commands.put(command.getName(), command);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#removeCommand(java.lang.String)
	 */
	@Override
	public void removeCommand(String name) {
		commands.remove(name);
	}

	/**
	 * @param context
	 * @return
	 */
	protected List<CellCommand> getCommands(RenderContext context) {
		if (!isInteractive()) {
			return Collections.emptyList();
		}

		List<CellCommand> availableCommands = new ArrayList<CellCommand>(
				commands.size());
		for (CellCommand command : commands.values()) {
			if (command.canExecute(context)) {
				availableCommands.add(command);
			}
		}

		return availableCommands;
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

	/**
	 * @see com.eyeq.pivot4j.StateHolder#bookmarkState()
	 */
	@Override
	public Serializable bookmarkState() {
		return new Serializable[] { drillDownMode, enableColumnDrillDown,
				enableRowDrillDown, enableSort, sortMode };
	}

	/**
	 * @see com.eyeq.pivot4j.StateHolder#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		Serializable[] states = (Serializable[]) state;

		this.drillDownMode = (String) states[0];
		this.enableColumnDrillDown = (Boolean) states[1];
		this.enableRowDrillDown = (Boolean) states[2];
		this.enableSort = (Boolean) states[3];
		this.sortMode = (SortMode) states[4];
	}
}
