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

public class OrConditionTest extends AbstractConditionTest {

	@Test(expected = IllegalStateException.class)
	public void testEmptyCondition() {
		RenderContext context = createDummyRenderContext();

		OrCondition or = new OrCondition(conditionFactory);
		or.matches(context);
	}

	@Test
	public void testCondition() {
		RenderContext context = createDummyRenderContext();

		OrCondition or = new OrCondition(conditionFactory);

		or.setSubConditions(Arrays.asList(TestCondition.TRUE,
				TestCondition.TRUE));
		assertThat("'true || true' should be true.", or.matches(context),
				is(true));

		or.setSubConditions(Arrays.asList(TestCondition.TRUE,
				TestCondition.FALSE));
		assertThat("'true || false' should be true.", or.matches(context),
				is(true));

		or.setSubConditions(Arrays.asList(TestCondition.TRUE));
		assertThat("'true' should be true.", or.matches(context), is(true));

		or.setSubConditions(Arrays.asList(TestCondition.FALSE,
				TestCondition.FALSE, TestCondition.TRUE));
		assertThat("'false || false || true' should be true.",
				or.matches(context), is(true));
	}
}
