/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.command;

import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.pivot4j.PivotModel;
import org.pivot4j.sort.SortMode;
import org.pivot4j.transform.SwapAxes;
import org.pivot4j.ui.PivotRenderer;

public class ToggleSortCommand extends AbstractSortCommand {

	public static final String NAME = "sort";

	/**
	 * @param renderer
	 */
	public ToggleSortCommand(PivotRenderer<?> renderer) {
		super(renderer);
	}

	/**
	 * @see org.pivot4j.ui.command.UICommand#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * @see org.pivot4j.ui.command.UICommand#execute(org.pivot4j.PivotModel ,
	 *      org.pivot4j.ui.command.UICommandParameters)
	 */
	@Override
	public Void execute(PivotModel model, UICommandParameters parameters) {
		CellSet cellSet = model.getCellSet();

		CellSetAxis axis = cellSet.getAxes().get(parameters.getAxisOrdinal());
		CellSetAxis otherAxis = cellSet.getAxes().get(
				Math.abs(parameters.getAxisOrdinal() - 1));

		SwapAxes transform = model.getTransform(SwapAxes.class);

		if (transform.isSwapAxes()) {
			CellSetAxis temp = axis;
			axis = otherAxis;
			otherAxis = temp;
		}

		Position position = axis.getPositions().get(
				parameters.getPositionOrdinal());

		SortMode mode = getRenderer().getSortMode();

		if (mode != null) {
			mode.toggleSort(model);
		}

		model.sort(otherAxis, position);

		return null;
	}
}
