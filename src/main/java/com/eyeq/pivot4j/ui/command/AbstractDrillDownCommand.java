/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.command;

import org.apache.commons.lang.ObjectUtils;
import org.olap4j.Axis;

import com.eyeq.pivot4j.ui.PivotRenderer;
import com.eyeq.pivot4j.ui.RenderContext;

public abstract class AbstractDrillDownCommand extends AbstractCellCommand
		implements DrillDownCommand {

	/**
	 * @param renderer
	 */
	public AbstractDrillDownCommand(PivotRenderer renderer) {
		super(renderer);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#canExecute(com.eyeq.pivot4j.ui
	 *      .RenderContext)
	 */
	@Override
	public boolean canExecute(RenderContext context) {
		boolean enabled = ObjectUtils.equals(getMode(context), getRenderer()
				.getDrillDownMode())
				&& context.getAxis() != null;

		if (enabled) {
			enabled = (getRenderer().getEnableColumnDrillDown() && context
					.getAxis().equals(Axis.COLUMNS))
					|| (getRenderer().getEnableRowDrillDown() && context
							.getAxis().equals(Axis.ROWS));
		}

		return enabled;
	}
}
