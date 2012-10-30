/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.command;

import com.eyeq.pivot4j.ui.PivotRenderer;
import com.eyeq.pivot4j.ui.RenderContext;
import com.eyeq.pivot4j.ui.SortMode;

public abstract class AbstractSortCommand extends AbstractCellCommand implements
		SortCommand {

	/**
	 * @param renderer
	 */
	public AbstractSortCommand(PivotRenderer renderer) {
		super(renderer);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.AbstractCellCommand#getMode(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public String getMode(RenderContext context) {
		StringBuilder builder = new StringBuilder();

		SortMode mode = getRenderer().getSortMode();
		if (mode != null) {
			builder.append(mode.getName());
			builder.append('-');
		}

		switch (context.getModel().getSortCriteria()) {
		case ASC:
		case BASC:
		case TOPCOUNT:
			builder.append("up");
			break;
		case DESC:
		case BDESC:
		case BOTTOMCOUNT:
			builder.append("down");
			break;
		default:
			assert false;
		}

		return builder.toString();
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
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#canExecute(com.eyeq.pivot4j.ui
	 *      .RenderContext)
	 */
	@Override
	public boolean canExecute(RenderContext context) {
		return getRenderer().getEnableSort() && context.getPosition() != null
				&& context.getModel().isSortable(context.getPosition());
	}
}
