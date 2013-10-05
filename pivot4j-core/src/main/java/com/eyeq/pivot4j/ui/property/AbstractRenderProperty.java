/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.property;

import java.io.Serializable;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.builder.ToStringBuilder;

public abstract class AbstractRenderProperty implements RenderProperty {

	private String name;

	/**
	 * Constructor used for internal state restoration.
	 */
	AbstractRenderProperty() {
	}

	/**
	 * Default public constructor.
	 * 
	 * @param name
	 * @param value
	 */
	public AbstractRenderProperty(String name) {
		if (name == null) {
			throw new NullArgumentException("name");
		}

		this.name = name;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.property.RenderProperty#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#saveState()
	 */
	@Override
	public Serializable saveState() {
		return name;
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		this.name = (String) state;
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#saveSettings(org.apache.commons.
	 *      configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		configuration.setProperty("[@name]", name);
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#restoreSettings(org.apache.commons
	 *      .configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		this.name = configuration.getString("[@name]");
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RenderProperty property) {
		if (property == null) {
			return -1;
		}

		return name.compareTo(property.getName());
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", name).toString();
	}
}
