/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.condition;

import org.apache.commons.lang.NullArgumentException;

public class DefaultConditionFactory implements ConditionFactory {

	/**
	 * @see com.eyeq.pivot4j.ui.condition.ConditionFactory#createCondition(java.lang.String)
	 */
	@Override
	public Condition createCondition(String name) {
		if (name == null) {
			throw new NullArgumentException("name");
		}

		return null;
	}
}
