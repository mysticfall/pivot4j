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

import org.olap4j.OlapException;

import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.PivotModel;
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
import com.eyeq.pivot4j.ui.impl.RenderStrategyImpl;

public abstract class AbstractPivotRenderer implements PivotRenderer,
		PivotLayoutCallback {

	private boolean enableColumnDrillDown;

	private boolean enableRowDrillDown;

	private boolean enableSort = true;

	private boolean enableDrillThrough = false;

	private boolean hideSpans = false;

	private boolean showParentMembers = false;

	private boolean showDimensionTitle = true;

	private SortMode sortMode = SortMode.BASIC;

	private String drillDownMode = DrillDownCommand.MODE_POSITION;

	private PropertyCollector propertyCollector;

	private Map<String, CellCommand<?>> commands = new HashMap<String, CellCommand<?>>();

	private RenderStrategy renderStrategy;

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#initialize()
	 */
	public void initialize() {
		registerCommands();
		this.renderStrategy = createRenderStrategy();
	}

	/**
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#render(com.eyeq.pivot4j.PivotModel)
	 */
	@Override
	public void render(PivotModel model) {
		if (model == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'model'.");
		}

		if (renderStrategy == null) {
			throw new IllegalStateException("Renderer was not initialized yet.");
		}

		renderStrategy.render(model, this, this);
	}

	/**
	 * @return the sortMode
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getSortMode()
	 */
	public SortMode getSortMode() {
		return sortMode;
	}

	/**
	 * @param sortMode
	 *            the sortMode to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setSortMode(com.eyeq.pivot4j.ui.SortMode)
	 */
	public void setSortMode(SortMode sortMode) {
		this.sortMode = sortMode;
	}

	/**
	 * @return the drillDownMode
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getDrillDownMode()
	 */
	public String getDrillDownMode() {
		return drillDownMode;
	}

	/**
	 * @param drillDownMode
	 *            the drillDownMode to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setDrillDownMode(java.lang.String)
	 */
	public void setDrillDownMode(String drillDownMode) {
		this.drillDownMode = drillDownMode;
	}

	/**
	 * @return the enableColumnDrillDown
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getEnableColumnDrillDown()
	 */
	public boolean getEnableColumnDrillDown() {
		return enableColumnDrillDown;
	}

	/**
	 * @param enableColumnDrillDown
	 *            the enableColumnDrillDown to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setEnableColumnDrillDown(boolean)
	 */
	public void setEnableColumnDrillDown(boolean enableColumnDrillDown) {
		this.enableColumnDrillDown = enableColumnDrillDown;
	}

	/**
	 * @return the enableRowDrillDown
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getEnableRowDrillDown()
	 */
	public boolean getEnableRowDrillDown() {
		return enableRowDrillDown;
	}

	/**
	 * @param enableRowDrillDown
	 *            the enableRowDrillDown to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setEnableRowDrillDown(boolean)
	 */
	public void setEnableRowDrillDown(boolean enableRowDrillDown) {
		this.enableRowDrillDown = enableRowDrillDown;
	}

	/**
	 * @return the enableSort
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getEnableSort()
	 */
	public boolean getEnableSort() {
		return enableSort;
	}

	/**
	 * @param enableSort
	 *            the enableSort to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setEnableSort(boolean)
	 */
	public void setEnableSort(boolean enableSort) {
		this.enableSort = enableSort;
	}

	/**
	 * @return the enableDrillThrough
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getEnableDrillThrough()
	 */
	public boolean getEnableDrillThrough() {
		return enableDrillThrough;
	}

	/**
	 * @param enableDrillThrough
	 *            the enableDrillThrough to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setEnableDrillThrough(boolean)
	 */
	public void setEnableDrillThrough(boolean enableDrillThrough) {
		this.enableDrillThrough = enableDrillThrough;
	}

	/**
	 * @return the hideSpans
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getHideSpans()
	 */
	public boolean getHideSpans() {
		return hideSpans;
	}

	/**
	 * @param hideSpans
	 *            the hideSpans to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setHideSpans(boolean)
	 */
	public void setHideSpans(boolean hideSpans) {
		this.hideSpans = hideSpans;
	}

	/**
	 * @return the showParentMembers
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getShowParentMembers()
	 */
	public boolean getShowParentMembers() {
		return showParentMembers;
	}

	/**
	 * @param showParentMembers
	 *            the showParentMembers to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setShowParentMembers(boolean)
	 */
	public void setShowParentMembers(boolean showParentMembers) {
		this.showParentMembers = showParentMembers;
	}

	/**
	 * @return the showDimensionTitle
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#getShowDimensionTitle()
	 */
	public boolean getShowDimensionTitle() {
		return showDimensionTitle;
	}

	/**
	 * @param showDimensionTitle
	 *            the showDimensionTitle to set
	 * @see com.eyeq.pivot4j.ui.PivotRenderer#setShowDimensionTitle(boolean)
	 */
	public void setShowDimensionTitle(boolean showDimensionTitle) {
		this.showDimensionTitle = showDimensionTitle;
	}

	/**
	 * @return the propertyCollector
	 */
	public PropertyCollector getPropertyCollector() {
		return propertyCollector;
	}

	/**
	 * @param propertyCollector
	 *            the propertyCollector to set
	 */
	public void setPropertyCollector(PropertyCollector propertyCollector) {
		this.propertyCollector = propertyCollector;
	}

	protected boolean isInteractive() {
		return true;
	}

	/**
	 * @return renderStrategy
	 */
	protected RenderStrategy getRenderStrategy() {
		return renderStrategy;
	}

	/**
	 * @return
	 */
	protected RenderStrategy createRenderStrategy() {
		return new RenderStrategyImpl();
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
		if (!isInteractive()) {
			return Collections.emptyList();
		}

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
			if (context.getProperty() == null) {
				label = context.getMember().getCaption();
			} else {
				try {
					label = context.getMember().getPropertyFormattedValue(
							context.getProperty());
				} catch (OlapException e) {
					throw new PivotException(e);
				}
			}
			break;
		case ColumnTitle:
		case RowTitle:
			if (context.getProperty() != null) {
				label = context.getProperty().getCaption();
			} else if (context.getLevel() != null) {
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
				enableRowDrillDown, enableSort, enableDrillThrough, sortMode,
				showDimensionTitle, showParentMembers, hideSpans };
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
		this.enableDrillThrough = (Boolean) states[4];
		this.sortMode = (SortMode) states[5];
		this.showDimensionTitle = (Boolean) states[6];
		this.showParentMembers = (Boolean) states[7];
		this.hideSpans = (Boolean) states[8];
	}
}
