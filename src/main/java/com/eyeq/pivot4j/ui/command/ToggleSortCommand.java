/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.command;

import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.SortModeCycle;
import com.eyeq.pivot4j.ui.RenderContext;

public class ToggleSortCommand implements SortCommand {

	public static final String NAME = "sort";

	private SortModeCycle sortCycle;

	/**
	 * @param sortCycle
	 */
	public ToggleSortCommand(SortModeCycle sortCycle) {
		if (sortCycle == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'sortCycle'.");
		}

		this.sortCycle = sortCycle;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * @return the sortCycle
	 */
	public SortModeCycle getSortCycle() {
		return sortCycle;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#canExecute(com.eyeq.pivot4j.ui
	 *      .RenderContext)
	 */
	@Override
	public boolean canExecute(RenderContext context) {
		return context.getPosition() != null
				&& context.getModel().isSortable(context.getPosition());
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#createParameters(com.eyeq.pivot4j
	 *      .ui.RenderContext)
	 */
	@Override
	public CellParameters createParameters(RenderContext context) {
		CellParameters parameters = new CellParameters();
		parameters.setAxisOrdinal(context.getAxis().axisOrdinal());
		parameters.setPositionOrdinal(context.getPosition().getOrdinal());

		return parameters;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#execute(com.eyeq.pivot4j.PivotModel
	 *      , com.eyeq.pivot4j.ui.command.CellParameters)
	 */
	@Override
	public void execute(PivotModel model, CellParameters parameters) {
		CellSet cellSet = model.getCellSet();

		CellSetAxis axis = cellSet.getAxes().get(parameters.getAxisOrdinal());
		CellSetAxis otherAxis = cellSet.getAxes().get(
				Math.abs(parameters.getAxisOrdinal() - 1));

		Position position = axis.getPositions().get(
				parameters.getPositionOrdinal());

		sortCycle.toggleSort(model);
		model.sort(otherAxis, position);
	}
}
