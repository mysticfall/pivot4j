/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.condition;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConditionFactory implements ConditionFactory {

	/**
	 * @see com.eyeq.pivot4j.ui.condition.ConditionFactory#getAvailableConditions()
	 */
	@Override
	public List<String> getAvailableConditions() {
		List<String> names = new LinkedList<String>();

		names.add(AndCondition.NAME);
		names.add(OrCondition.NAME);
		names.add(NotCondition.NAME);
		names.add(CellTypeCondition.NAME);
		names.add(ExpressionCondition.NAME);

		return names;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.condition.ConditionFactory#createCondition(java.lang.String)
	 */
	@Override
	public Condition createCondition(String name) {
		if (name == null) {
			throw new NullArgumentException("name");
		}

		Condition condition = null;

		// TODO Replace this abomination with something more sensible.
		if (AndCondition.NAME.equals(name)) {
			condition = new AndCondition(this);
		} else if (OrCondition.NAME.equals(name)) {
			condition = new OrCondition(this);
		} else if (NotCondition.NAME.equals(name)) {
			condition = new NotCondition(this);
		} else if (CellTypeCondition.NAME.equals(name)) {
			condition = new CellTypeCondition(this);
		} else if (ExpressionCondition.NAME.equals(name)) {
			condition = new ExpressionCondition(this);
		}

		if (condition == null) {
			Logger logger = LoggerFactory.getLogger(getClass());
			if (logger.isWarnEnabled()) {
				logger.warn("Unknown condition name : " + name);
			}
		}

		return condition;
	}
}
