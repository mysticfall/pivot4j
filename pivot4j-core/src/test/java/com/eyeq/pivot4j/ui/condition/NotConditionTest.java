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

import com.eyeq.pivot4j.ui.RenderContext;

public class NotConditionTest extends AbstractConditionTest {

	@Test(expected = IllegalStateException.class)
	public void testEmptyCondition() {
		RenderContext context = createDummyRenderContext();

		NotCondition not = new NotCondition(conditionFactory);
		not.matches(context);
	}

	@Test
	public void testCondition() {
		RenderContext context = createDummyRenderContext();

		NotCondition not = new NotCondition(conditionFactory);

		not.setSubCondition(TestCondition.FALSE);
		assertThat("'!false' should be true.", not.matches(context), is(true));

		not.setSubCondition(TestCondition.TRUE);
		assertThat("'!true' should be false.", not.matches(context), is(false));
	}
}
