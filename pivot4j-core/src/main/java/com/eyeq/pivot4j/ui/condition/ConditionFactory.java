/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.condition;

public interface ConditionFactory {

	/**
	 * @param name
	 * @return
	 */
	Condition createCondition(String name);
}
