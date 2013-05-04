/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.condition;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.olap4j.Axis;

import com.eyeq.pivot4j.ui.RenderContext;

public class ExpressionConditionTest extends AbstractConditionTest {

	@Test(expected = IllegalStateException.class)
	public void testEmptyCondition() {
		RenderContext context = createDummyRenderContext();

		ExpressionCondition expression = new ExpressionCondition(
				conditionFactory);
		expression.matches(context);
	}

	@Test
	public void testSimpleExpression() {
		RenderContext context = createDummyRenderContext();

		ExpressionCondition expression = new ExpressionCondition(
				conditionFactory);

		expression.setExpression("true");
		assertThat("Expression '" + expression.getExpression()
				+ "' should be true.", expression.matches(context), is(true));

		expression.setExpression("  True ");
		assertThat("Expression '" + expression.getExpression()
				+ "' should be true.", expression.matches(context), is(true));

		expression.setExpression("abctrue");
		assertThat("Expression '" + expression.getExpression()
				+ "' should be false.", expression.matches(context), is(false));

		context.setColIndex(2);
	}

	@Test
	public void testConditionalExpression() {
		RenderContext context = createDummyRenderContext();
		context.setColIndex(2);
		context.setRowIndex(1);
		context.setAxis(Axis.ROWS);

		ExpressionCondition expression = new ExpressionCondition(
				conditionFactory);

		expression.setExpression("<#if colIndex = 2>true</#if>");
		assertThat("Expression '" + expression.getExpression()
				+ "' should be true.", expression.matches(context), is(true));

		expression.setExpression("<#if colIndex != 2>true</#if>");
		assertThat("Expression '" + expression.getExpression()
				+ "' should be false.", expression.matches(context), is(false));

		expression
				.setExpression("<#if colIndex = 2 && rowIndex = 1>true</#if>");
		assertThat("Expression '" + expression.getExpression()
				+ "' should be true.", expression.matches(context), is(true));

		expression.setExpression("<#if axis = \"ROWS\">true</#if>");
		assertThat("Expression '" + expression.getExpression()
				+ "' should be true.", expression.matches(context), is(true));

		expression.setExpression("<#if axis = \"COLUMNS\">true</#if>");
		assertThat("Expression '" + expression.getExpression()
				+ "' should be false.", expression.matches(context), is(false));
	}
}
