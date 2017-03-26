/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.command;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.olap4j.Cell;
import org.olap4j.OlapException;
import org.pivot4j.PivotException;
import org.pivot4j.PivotModel;
import org.pivot4j.ui.PivotRenderer;
import org.pivot4j.ui.RenderContext;

public class BasicDrillThroughCommand extends AbstractUICommand<ResultSet>
		implements DrillThroughCommand {

	public static final String NAME = "drillThrough";

	/**
	 * @param renderer
	 */
	public BasicDrillThroughCommand(PivotRenderer<?> renderer) {
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
	 * @see org.pivot4j.ui.command.UICommand#canExecute(org.pivot4j.ui
	 *      .RenderContext)
	 */
	@Override
	public boolean canExecute(RenderContext context) {
		// See: http://jira.pentaho.com/browse/MONDRIAN-1036
		PivotModel model = context.getModel();
		boolean scenarioEnabled = model.isScenarioSupported()
				&& model.getScenario() != null;

		return !scenarioEnabled && getRenderer().getEnableDrillThrough()
				&& model.getCube().isDrillThroughEnabled()
				&& context.getCell() != null;
	}

	/**
	 * @see org.pivot4j.ui.command.UICommand#createParameters(org.pivot4j.ui.RenderContext)
	 */
	@Override
	public UICommandParameters createParameters(RenderContext context) {
		UICommandParameters parameters = new UICommandParameters();

		List<Integer> list = context.getCell().getCoordinateList();
		int[] coords = ArrayUtils.toPrimitive(list.toArray(new Integer[list.size()]));

		parameters.setCellCoordinate(coords);

		return parameters;
	}

	/**
	 * @see org.pivot4j.ui.command.UICommand#execute(org.pivot4j.PivotModel ,
	 *      org.pivot4j.ui.command.UICommandParameters)
	 */
	@Override
	public ResultSet execute(PivotModel model, UICommandParameters parameters) {
		int[] array = parameters.getCellCoordinate();
		List<Integer> coords = Arrays.asList(ArrayUtils.toObject(array));

		Cell cell = model.getCellSet().getCell(coords);

		try {
			return cell.drillThrough();
		} catch (OlapException e) {
			throw new PivotException(e);
		}
	}
}