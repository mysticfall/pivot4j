/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui;

import java.io.OutputStream;

import org.apache.commons.lang.NullArgumentException;

public abstract class AbstractContentRenderCallback<T extends RenderContext>
		extends AbstractRenderCallback<T> {

	private OutputStream stream;

	/**
	 * @param stream
	 */
	public AbstractContentRenderCallback(OutputStream stream) {
		if (stream == null) {
			throw new NullArgumentException("out");
		}

		this.stream = stream;
	}

	/**
	 * @return the stream
	 */
	protected OutputStream getOutputStream() {
		return stream;
	}
}
