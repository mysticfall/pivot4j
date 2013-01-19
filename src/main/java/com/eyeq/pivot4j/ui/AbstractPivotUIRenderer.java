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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eyeq.pivot4j.ui.command.BasicDrillThroughCommand;
import com.eyeq.pivot4j.ui.command.CellCommand;
import com.eyeq.pivot4j.ui.command.DrillCollapseMemberCommand;
import com.eyeq.pivot4j.ui.command.DrillCollapsePositionCommand;
import com.eyeq.pivot4j.ui.command.DrillDownCommand;
import com.eyeq.pivot4j.ui.command.DrillDownReplaceCommand;
import com.eyeq.pivot4j.ui.command.DrillExpandMemberCommand;
import com.eyeq.pivot4j.ui.command.DrillExpandPositionCommand;
import com.eyeq.pivot4j.ui.command.DrillUpReplaceCommand;
import com.eyeq.pivot4j.ui.command.ToggleSortCommand;

public abstract class AbstractPivotUIRenderer extends AbstractPivotRenderer
		implements PivotUIRenderer {

	private boolean enableColumnDrillDown;

	private boolean enableRowDrillDown;

	private boolean enableSort = true;

	private boolean enableDrillThrough = false;

	private SortMode sortMode = SortMode.BASIC;

	private String drillDownMode = DrillDownCommand.MODE_POSITION;

	private Map<String, CellCommand<?>> commands = new HashMap<String, CellCommand<?>>();

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#initialize()
	 */
	@Override
	public void initialize() {
		super.initialize();

		registerCommands();
	}

	/**
	 * @return
	 */
	public SortMode getSortMode() {
		return sortMode;
	}

	/**
	 * @param sortMode
	 *            the sortMode to set
	 * @see com.eyeq.pivot4j.ui.PivotUIRenderer#setSortMode(com.eyeq.pivot4j.ui.SortMode)
	 */
	public void setSortMode(SortMode sortMode) {
		this.sortMode = sortMode;
	}

	/**
	 * @return the drillDownMode
	 * @see com.eyeq.pivot4j.ui.PivotUIRenderer#getDrillDownMode()
	 */
	public String getDrillDownMode() {
		return drillDownMode;
	}

	/**
	 * @param drillDownMode
	 *            the drillDownMode to set
	 * @see com.eyeq.pivot4j.ui.PivotUIRenderer#setDrillDownMode(java.lang.String)
	 */
	public void setDrillDownMode(String drillDownMode) {
		this.drillDownMode = drillDownMode;
	}

	/**
	 * @return the enableColumnDrillDown
	 * @see com.eyeq.pivot4j.ui.PivotUIRenderer#getEnableColumnDrillDown()
	 */
	public boolean getEnableColumnDrillDown() {
		return enableColumnDrillDown;
	}

	/**
	 * @param enableColumnDrillDown
	 *            the enableColumnDrillDown to set
	 * @see com.eyeq.pivot4j.ui.PivotUIRenderer#setEnableColumnDrillDown(boolean)
	 */
	public void setEnableColumnDrillDown(boolean enableColumnDrillDown) {
		this.enableColumnDrillDown = enableColumnDrillDown;
	}

	/**
	 * @return the enableRowDrillDown
	 * @see com.eyeq.pivot4j.ui.PivotUIRenderer#getEnableRowDrillDown()
	 */
	public boolean getEnableRowDrillDown() {
		return enableRowDrillDown;
	}

	/**
	 * @param enableRowDrillDown
	 *            the enableRowDrillDown to set
	 * @see com.eyeq.pivot4j.ui.PivotUIRenderer#setEnableRowDrillDown(boolean)
	 */
	public void setEnableRowDrillDown(boolean enableRowDrillDown) {
		this.enableRowDrillDown = enableRowDrillDown;
	}

	/**
	 * @return the enableSort
	 * @see com.eyeq.pivot4j.ui.PivotUIRenderer#getEnableSort()
	 */
	public boolean getEnableSort() {
		return enableSort;
	}

	/**
	 * @param enableSort
	 *            the enableSort to set
	 * @see com.eyeq.pivot4j.ui.PivotUIRenderer#setEnableSort(boolean)
	 */
	public void setEnableSort(boolean enableSort) {
		this.enableSort = enableSort;
	}

	/**
	 * @return the enableDrillThrough
	 * @see com.eyeq.pivot4j.ui.PivotUIRenderer#getEnableDrillThrough()
	 */
	public boolean getEnableDrillThrough() {
		return enableDrillThrough;
	}

	/**
	 * @param enableDrillThrough
	 *            the enableDrillThrough to set
	 * @see com.eyeq.pivot4j.ui.PivotUIRenderer#setEnableDrillThrough(boolean)
	 */
	public void setEnableDrillThrough(boolean enableDrillThrough) {
		this.enableDrillThrough = enableDrillThrough;
	}

	protected void registerCommands() {
		addCommand(new DrillExpandPositionCommand(this));
		addCommand(new DrillCollapsePositionCommand(this));
		addCommand(new DrillExpandMemberCommand(this));
		addCommand(new DrillCollapseMemberCommand(this));
		addCommand(new DrillDownReplaceCommand(this));
		addCommand(new DrillUpReplaceCommand(this));
		addCommand(new ToggleSortCommand(this));
		addCommand(new BasicDrillThroughCommand(this));
	}

	/**
	 * @param name
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getCommand(java.lang.String)
	 */
	public CellCommand<?> getCommand(String name) {
		return commands.get(name);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#addCommand(com.eyeq.pivot4j.ui.command
	 *      .CellCommand)
	 */
	@Override
	public void addCommand(CellCommand<?> command) {
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
	protected List<CellCommand<?>> getCommands(RenderContext context) {
		List<CellCommand<?>> availableCommands = new ArrayList<CellCommand<?>>(
				commands.size());
		for (CellCommand<?> command : commands.values()) {
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
		List<CellCommand<?>> commands = getCommands(context);

		startCell(context, commands);
	}

	/**
	 * @param context
	 * @param commands
	 */
	public abstract void startCell(RenderContext context,
			List<CellCommand<?>> commands);

	/**
	 * @see com.eyeq.pivot4j.StateHolder#bookmarkState()
	 */
	@Override
	public Serializable bookmarkState() {
		return new Serializable[] { super.bookmarkState(), drillDownMode,
				enableColumnDrillDown, enableRowDrillDown, enableSort,
				enableDrillThrough, sortMode };
	}

	/**
	 * @see com.eyeq.pivot4j.StateHolder#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		Serializable[] states = (Serializable[]) state;

		super.restoreState(states[0]);

		this.drillDownMode = (String) states[1];
		this.enableColumnDrillDown = (Boolean) states[2];
		this.enableRowDrillDown = (Boolean) states[3];
		this.enableSort = (Boolean) states[4];
		this.enableDrillThrough = (Boolean) states[5];
		this.sortMode = (SortMode) states[6];
	}
}
