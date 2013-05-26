/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.condition;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.PivotException;

public class DefaultConditionFactory implements ConditionFactory {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private Map<String, Class<? extends Condition>> types = new TreeMap<String, Class<? extends Condition>>();

	public DefaultConditionFactory() {
		types.put(AndCondition.NAME, AndCondition.class);
		types.put(OrCondition.NAME, OrCondition.class);
		types.put(NotCondition.NAME, NotCondition.class);
		types.put(ExpressionCondition.NAME, ExpressionCondition.class);

		types.put(AxisCondition.NAME, AxisCondition.class);
		types.put(HierarchyCondition.NAME, HierarchyCondition.class);
		types.put(LevelCondition.NAME, LevelCondition.class);
		types.put(MemberCondition.NAME, MemberCondition.class);

		types.put(CellTypeCondition.NAME, CellTypeCondition.class);
		types.put(CellValueCondition.NAME, CellValueCondition.class);
	}

	/**
	 * @return the logger
	 */
	protected Logger getLogger() {
		return logger;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.condition.ConditionFactory#getAvailableConditions()
	 */
	@Override
	public List<String> getAvailableConditions() {
		return new LinkedList<String>(types.keySet());
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

		Class<? extends Condition> type = types.get(name);

		if (type != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Instantiating a new condition : " + type);
			}

			try {
				Constructor<? extends Condition> constructor = type
						.getConstructor(ConditionFactory.class);
				condition = constructor.newInstance(this);
			} catch (Exception e) {
				throw new PivotException(e);
			}
		}

		if (condition == null && logger.isWarnEnabled()) {
			logger.warn("Unknown condition name : " + name);
		}

		return condition;
	}
}
