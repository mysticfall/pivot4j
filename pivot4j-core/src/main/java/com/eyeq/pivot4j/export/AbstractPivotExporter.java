/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.export;

import java.io.OutputStream;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.ui.AbstractPivotRenderer;

public abstract class AbstractPivotExporter extends AbstractPivotRenderer
		implements PivotExporter {

	private OutputStream out;

	public AbstractPivotExporter() {
		setRenderSlicer(true);
	}

	/**
	 * @param out
	 */
	public AbstractPivotExporter(OutputStream out) {
		this.out = out;

		setRenderSlicer(true);
	}

	/**
	 * @return out
	 */
	protected OutputStream getOutputStream() {
		return out;
	}

	/**
	 * @see com.eyeq.pivot4j.export.PivotExporter#setOutputStream(java.io.OutputStream)
	 */
	@Override
	public void setOutputStream(OutputStream out) {
		this.out = out;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractPivotRenderer#render(com.eyeq.pivot4j.PivotModel)
	 */
	@Override
	public void render(PivotModel model) {
		if (getOutputStream() == null) {
			throw new IllegalStateException(
					"No output stream was assigned for export.");
		}

		super.render(model);
	}
}
