/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.condition;

import java.io.Serializable;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.olap4j.Axis;
import org.olap4j.Position;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.ui.RenderContext;

public abstract class AbstractMetadataCondition extends AbstractCondition {

	private String uniqueName;

	/**
	 * @param conditionFactory
	 */
	public AbstractMetadataCondition(ConditionFactory conditionFactory) {
		super(conditionFactory);
	}

	/**
	 * @param conditionFactory
	 * @param uniqueName
	 */
	public AbstractMetadataCondition(ConditionFactory conditionFactory,
			String uniqueName) {
		super(conditionFactory);

		this.uniqueName = uniqueName;
	}

	/**
	 * @return the uniqueName
	 */
	public String getUniqueName() {
		return uniqueName;
	}

	/**
	 * @param uniqueName
	 *            the uniqueName to set
	 */
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.condition.Condition#matches(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public boolean matches(RenderContext context) {
		if (uniqueName == null) {
			throw new IllegalStateException(
					"Unique name of the metadata is not specified.");
		}

		if (matches(context.getMember())) {
			return true;
		}

		for (Axis axis : context.getAxes()) {
			Position pos = context.getPosition(axis);

			if (pos != null && matches(pos)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param position
	 * @return
	 */
	protected boolean matches(Position position) {
		if (position == null) {
			return false;
		}

		boolean matches = false;

		for (Member member : position.getMembers()) {
			if (matches(member)) {
				matches = true;
				break;
			}
		}

		return matches;
	}

	/**
	 * @param member
	 * @return
	 */
	protected abstract boolean matches(Member member);

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#saveState()
	 */
	@Override
	public Serializable saveState() {
		return uniqueName;
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		if (state != null) {
			this.uniqueName = (String) state;
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.condition.AbstractCondition#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		super.saveSettings(configuration);

		if (uniqueName == null) {
			return;
		}

		configuration.addProperty(getName(), uniqueName);
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		this.uniqueName = configuration.getString(getName());
	}

	/**
	 * @see com.eyeq.pivot4j.ui.condition.AbstractCondition#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getName() + " = '");

		if (uniqueName != null) {
			builder.append(uniqueName);
		}

		builder.append("'");

		return builder.toString();
	}
}
