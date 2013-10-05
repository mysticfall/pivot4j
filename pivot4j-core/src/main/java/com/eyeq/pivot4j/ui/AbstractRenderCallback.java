/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import java.io.Serializable;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.eyeq.pivot4j.util.RenderPropertyUtils;

public abstract class AbstractRenderCallback<T extends RenderContext>
		implements RenderCallback<T> {

	private RenderPropertyUtils renderPropertyUtils;

	/**
	 * @see com.eyeq.pivot4j.ui.RenderCallback#getContentType()
	 */
	@Override
	public String getContentType() {
		return null;
	}

	/**
	 * @return the renderPropertyUtils
	 */
	protected RenderPropertyUtils getRenderPropertyUtils() {
		return renderPropertyUtils;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.RenderCallback#startRender(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void startRender(T context) {
		this.renderPropertyUtils = new RenderPropertyUtils(context);
	}

	/**
	 * @see com.eyeq.pivot4j.ui.RenderCallback#endRender(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public void endRender(T context) {
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#saveSettings(org.apache.commons.
	 *      configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#restoreSettings(org.apache.commons
	 *      .configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#saveState()
	 */
	@Override
	public Serializable saveState() {
		return null;
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
	}
}
