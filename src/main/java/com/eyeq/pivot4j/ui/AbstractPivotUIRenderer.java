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

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.sort.SortMode;
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

	private SortMode sortMode = SortMode.BASIC;

	private boolean enableDrillThrough = false;

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
	 * @see com.eyeq.pivot4j.ui.PivotUIRenderer#setSortMode(com.eyeq.pivot4j.sort.SortMode)
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
	 * @see com.eyeq.pivot4j.state.Bookmarkable#saveState()
	 */
	@Override
	public Serializable saveState() {
		return new Serializable[] { super.saveState(), drillDownMode,
				enableColumnDrillDown, enableRowDrillDown, enableSort,
				sortMode, enableDrillThrough };
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		Serializable[] states = (Serializable[]) state;

		super.restoreState(states[0]);

		this.drillDownMode = (String) states[1];
		this.enableColumnDrillDown = (Boolean) states[2];
		this.enableRowDrillDown = (Boolean) states[3];
		this.enableSort = (Boolean) states[4];
		this.sortMode = (SortMode) states[5];
		this.enableDrillThrough = (Boolean) states[6];
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		super.saveSettings(configuration);

		if (configuration.getLogger() == null) {
			configuration.setLogger(LogFactory.getLog(getClass()));
		}

		configuration.addProperty("render.drillDown[@mode]", drillDownMode);
		configuration.addProperty("render.drillDown.columnAxis[@enabled]",
				enableColumnDrillDown);
		configuration.addProperty("render.drillDown.rowAxis[@enabled]",
				enableRowDrillDown);
		configuration.addProperty("render.sort[@enabled]", enableSort);

		if (sortMode != null) {
			configuration.addProperty("render.sort[@mode]", sortMode.getName());
		}

		configuration.addProperty("render.drillThrough[@enabled]",
				enableDrillThrough);

		// TODO Need to store registered commands here.
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		super.restoreSettings(configuration);

		this.drillDownMode = configuration.getString("render.drillDown[@mode]",
				DrillDownCommand.MODE_POSITION);
		this.enableColumnDrillDown = configuration.getBoolean(
				"render.drillDown.columnAxis[@enabled]", false);
		this.enableRowDrillDown = configuration.getBoolean(
				"render.drillDown.rowAxis[@enabled]", false);
		this.enableSort = configuration.getBoolean("render.sort[@enabled]",
				true);

		// TODO Need to support a custom implementation.
		String sortModeName = configuration.getString("render.sort[@mode]",
				SortMode.BASIC.getName());

		this.sortMode = SortMode.fromName(sortModeName);

		if (sortMode == null) {
			Logger logger = LoggerFactory.getLogger(getClass());
			if (logger.isWarnEnabled()) {
				logger.warn("Ignoring unknown sort mode name : " + sortModeName);
			}

			this.sortMode = SortMode.BASIC;
		}

		this.enableDrillThrough = configuration.getBoolean(
				"render.drillThrough[@enabled]", false);
	}
}
