/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.property;

import java.io.Serializable;

import org.apache.commons.lang.NullArgumentException;
import org.pivot4j.ui.condition.Condition;

public class ConditionalValue implements Serializable {

	private static final long serialVersionUID = 1329852127269673680L;

	private Condition condition;

	private String value;

	/**
	 * @param condition
	 * @param value
	 */
	public ConditionalValue(Condition condition, String value) {
		if (condition == null) {
			throw new NullArgumentException("condition");
		}

		this.condition = condition;
		this.value = value;
	}

	/**
	 * @return the condition
	 */
	public Condition getCondition() {
		return condition;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
