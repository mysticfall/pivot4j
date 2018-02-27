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
import org.pivot4j.ui.condition.AndCondition;

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

    @Test
    public void testStateManagement() {
        RenderContext context = createDummyRenderContext();

        AndCondition and = new AndCondition(conditionFactory);
        and.setSubConditions(Arrays.asList(TestCondition.TRUE,
                TestCondition.TRUE));

        Serializable state = and.saveState();

        and = new AndCondition(conditionFactory);
        and.restoreState(state);

        assertThat("Sub conditions should not be null.",
                and.getSubConditions(), is(notNullValue()));
        assertThat("Sub condition count should be 2.", and.getSubConditions()
                .size(), is(2));
        assertThat("'true && true' should be true.", and.matches(context),
                is(true));
    }

    @Test
    public void testSettingsManagement() throws ConfigurationException {
        RenderContext context = createDummyRenderContext();

        AndCondition and = new AndCondition(conditionFactory);
        and.setSubConditions(Arrays.asList(TestCondition.TRUE,
                TestCondition.TRUE));

        XMLConfiguration configuration = new XMLConfiguration();
        configuration.setRootElementName("condition");

        and.saveSettings(configuration);

        and = new AndCondition(conditionFactory);
        and.restoreSettings(configuration);

        assertThat("Sub conditions should not be null.",
                and.getSubConditions(), is(notNullValue()));
        assertThat("Sub condition count should be 2.", and.getSubConditions()
                .size(), is(2));
        assertThat("'true && true' should be true.", and.matches(context),
                is(true));

        System.out.println("Saved configuration : ");

        configuration.save(System.out);
    }
}
