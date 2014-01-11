/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.condition;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;
import org.pivot4j.ui.RenderContext;
import org.pivot4j.ui.condition.OrCondition;

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

	@Test
	public void testStateManagement() {
		RenderContext context = createDummyRenderContext();

		OrCondition or = new OrCondition(conditionFactory);
		or.setSubConditions(Arrays.asList(TestCondition.TRUE,
				TestCondition.FALSE));

		Serializable state = or.saveState();

		or = new OrCondition(conditionFactory);
		or.restoreState(state);

		assertThat("Sub conditions should not be null.", or.getSubConditions(),
				is(notNullValue()));
		assertThat("Sub condition count should be 2.", or.getSubConditions()
				.size(), is(2));
		assertThat("'true || false' should be true.", or.matches(context),
				is(true));
	}

	@Test
	public void testSettingsManagement() throws ConfigurationException {
		RenderContext context = createDummyRenderContext();

		OrCondition or = new OrCondition(conditionFactory);
		or.setSubConditions(Arrays.asList(TestCondition.TRUE,
				TestCondition.FALSE));

		XMLConfiguration configuration = new XMLConfiguration();
		configuration.setRootElementName("condition");

		or.saveSettings(configuration);

		or = new OrCondition(conditionFactory);
		or.restoreSettings(configuration);

		assertThat("Sub conditions should not be null.", or.getSubConditions(),
				is(notNullValue()));
		assertThat("Sub condition count should be 2.", or.getSubConditions()
				.size(), is(2));
		assertThat("'true || false' should be true.", or.matches(context),
				is(true));

		System.out.println("Saved configuration : ");

		configuration.save(System.out);
	}
}
