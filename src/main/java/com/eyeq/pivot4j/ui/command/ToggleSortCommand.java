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
import com.eyeq.pivot4j.ui.PivotRenderer;
import com.eyeq.pivot4j.ui.SortMode;

public class ToggleSortCommand extends AbstractSortCommand {

	public static final String NAME = "sort";

	/**
	 * @param renderer
	 */
	public ToggleSortCommand(PivotRenderer renderer) {
		super(renderer);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#getName()
	 */
	@Override
	public String getName() {
		return NAME;
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

		SortMode mode = getRenderer().getSortMode();

		if (mode != null) {
			mode.toggleSort(model);
		}

		model.sort(otherAxis, position);
	}
}
