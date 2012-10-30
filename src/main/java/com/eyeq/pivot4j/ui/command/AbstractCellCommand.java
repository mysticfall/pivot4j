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

public abstract class AbstractCellCommand implements CellCommand {

	private PivotRenderer renderer;

	/**
	 * @param renderer
	 */
	public AbstractCellCommand(PivotRenderer renderer) {
		if (renderer == null) {
			throw new IllegalArgumentException(
					"Missing required argument 'renderer'.");
		}

		this.renderer = renderer;
	}

	/**
	 * @return the renderer
	 */
	protected PivotRenderer getRenderer() {
		return renderer;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#getDescription()
	 */
	@Override
	public String getDescription() {
		// TODO Do i18n here
		return null;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#getModeName()
	 */
	@Override
	public String getMode() {
		return null;
	}
}
