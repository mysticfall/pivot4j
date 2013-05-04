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

import java.util.Arrays;

import org.junit.Test;

import com.eyeq.pivot4j.ui.RenderContext;

public class AndConditionTest extends AbstractConditionTest {

	@Test(expected = IllegalStateException.class)
	public void testEmptyCondition() {
		RenderContext context = createDummyRenderContext();

		AndCondition and = new AndCondition(conditionFactory);
		and.matches(context);
	}

	@Test
	public void testCondition() {
		RenderContext context = createDummyRenderContext();

		AndCondition and = new AndCondition(conditionFactory);

		and.setSubConditions(Arrays.asList(TestCondition.TRUE,
				TestCondition.TRUE));
		assertThat("'true && true' should be true.", and.matches(context),
				is(true));

		and.setSubConditions(Arrays.asList(TestCondition.TRUE,
				TestCondition.FALSE));
		assertThat("'true && false' should be false.", and.matches(context),
				is(false));

		and.setSubConditions(Arrays.asList(TestCondition.TRUE));
		assertThat("'true' should be true.", and.matches(context), is(true));

		and.setSubConditions(Arrays.asList(TestCondition.FALSE,
				TestCondition.FALSE, TestCondition.FALSE));
		assertThat("'false && false && false' should be false.",
				and.matches(context), is(false));
	}
}
