/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.condition;

import java.io.Serializable;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.pivot4j.el.ExpressionEvaluator;
import org.pivot4j.ui.RenderContext;

public class ExpressionCondition extends AbstractCondition {

	public static final String NAME = "expression";

	private String expression;

	/**
	 * @param conditionFactory
	 */
	public ExpressionCondition(ConditionFactory conditionFactory) {
		super(conditionFactory);
	}

	/**
	 * @param conditionFactory
	 * @param expression
	 */
	public ExpressionCondition(ConditionFactory conditionFactory,
			String expression) {
		super(conditionFactory);

		this.expression = expression;
	}

	/**
	 * @see org.pivot4j.ui.condition.Condition#getName()
	 */
	public String getName() {
		return NAME;
	}

	/**
	 * @return the expression
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * @param expression
	 *            the expression to set
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}

	/**
	 * @see org.pivot4j.ui.condition.Condition#matches(org.pivot4j.ui.RenderContext)
	 */
	@Override
	public boolean matches(RenderContext context) {
		if (expression == null) {
			throw new IllegalStateException("Missing expression statement.");
		}

		ExpressionEvaluator evaluator = context.getExpressionEvaluator();

		Object result = evaluator.evaluate(expression,
				context.getExpressionContext());

		if (result != null) {
			return "true".equalsIgnoreCase(result.toString().trim());
		} else {
			return false;
		}
	}

	/**
	 * @see org.pivot4j.state.Bookmarkable#saveState()
	 */
	@Override
	public Serializable saveState() {
		return expression;
	}

	/**
	 * @see org.pivot4j.state.Bookmarkable#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		if (state == null) {
			this.expression = null;
		} else {
			this.expression = (String) state;
		}
	}

	/**
	 * @see org.pivot4j.ui.condition.AbstractCondition#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		super.saveSettings(configuration);

		if (expression == null) {
			return;
		}

		configuration.addProperty("expression", expression);
	}

	/**
	 * @see org.pivot4j.state.Configurable#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		this.expression = configuration.getString("expression");
	}

	/**
	 * @see org.pivot4j.ui.condition.AbstractCondition#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("eval(");

		if (expression != null) {
			builder.append(expression);
		}

		builder.append(")");

		return builder.toString();
	}
}
