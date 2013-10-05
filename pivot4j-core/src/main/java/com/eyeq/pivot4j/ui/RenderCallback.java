/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import java.util.List;

import com.eyeq.pivot4j.state.Bookmarkable;
import com.eyeq.pivot4j.state.Configurable;
import com.eyeq.pivot4j.ui.command.UICommand;

public interface RenderCallback<T extends RenderContext> extends Configurable,
		Bookmarkable {

	public String getContentType();

	/**
	 * @param context
	 */
	void startRender(T context);

	/**
	 * @param context
	 * @param commands
	 */
	void renderCommands(T context, List<UICommand<?>> commands);

	/**
	 * @param context
	 * @param label
	 */
	void renderContent(T context, String label);

	/**
	 * @param context
	 */
	void endRender(T context);
}
