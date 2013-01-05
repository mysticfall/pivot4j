/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.command;

import java.sql.ResultSet;

import org.olap4j.Cell;
import org.olap4j.OlapException;

import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.ui.PivotRenderer;
import com.eyeq.pivot4j.ui.RenderContext;

public class BasicDrillThroughCommand extends AbstractCellCommand<ResultSet>
		implements DrillThroughCommand {

	public static final String NAME = "drillThrough";

	/**
	 * @param renderer
	 */
	public BasicDrillThroughCommand(PivotRenderer renderer) {
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
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#canExecute(com.eyeq.pivot4j.ui
	 *      .RenderContext)
	 */
	@Override
	public boolean canExecute(RenderContext context) {
		return getRenderer().getEnableDrillThrough()
				&& context.getModel().getCube().isDrillThroughEnabled()
				&& context.getCell() != null;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#createParameters(com.eyeq.pivot4j
	 *      .ui.RenderContext)
	 */
	@Override
	public CellParameters createParameters(RenderContext context) {
		CellParameters parameters = new CellParameters();
		parameters.setCellOrdinal(context.getCell().getOrdinal());

		return parameters;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#execute(com.eyeq.pivot4j.PivotModel
	 *      , com.eyeq.pivot4j.ui.command.CellParameters)
	 */
	@Override
	public ResultSet execute(PivotModel model, CellParameters parameters) {
		Cell cell = model.getCellSet().getCell(parameters.getCellOrdinal());

		try {
			return cell.drillThrough();
		} catch (OlapException e) {
			throw new PivotException(e);
		}
	}
}