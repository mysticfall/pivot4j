/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.command;

import org.apache.commons.lang.NullArgumentException;

import com.eyeq.pivot4j.ui.PivotUIRenderer;
import com.eyeq.pivot4j.ui.RenderContext;

public abstract class AbstractCellCommand<T> implements CellCommand<T> {

	private PivotUIRenderer renderer;

	/**
	 * @param renderer
	 */
	public AbstractCellCommand(PivotUIRenderer renderer) {
		if (renderer == null) {
			throw new NullArgumentException("renderer");
		}

		this.renderer = renderer;
	}

	/**
	 * @return the renderer
	 */
	protected PivotUIRenderer getRenderer() {
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
	 * @see com.eyeq.pivot4j.ui.command.CellCommand#getMode(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public String getMode(RenderContext context) {
		return null;
	}
}
