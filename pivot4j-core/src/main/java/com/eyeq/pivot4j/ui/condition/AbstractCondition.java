/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.condition;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NullArgumentException;

public abstract class AbstractCondition implements Condition {

	private ConditionFactory conditionFactory;

	/**
	 * @param conditionFactory
	 */
	public AbstractCondition(ConditionFactory conditionFactory) {
		if (conditionFactory == null) {
			throw new NullArgumentException("conditionFactory");
		}

		this.conditionFactory = conditionFactory;
	}

	/**
	 * @return the conditionFactory
	 */
	public ConditionFactory getConditionFactory() {
		return conditionFactory;
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		configuration.addProperty("[@name]", getName());
	}
}
