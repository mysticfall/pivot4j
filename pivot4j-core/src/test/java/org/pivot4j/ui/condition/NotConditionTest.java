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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;
import org.pivot4j.ui.RenderContext;
import org.pivot4j.ui.condition.NotCondition;

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

    @Test
    public void testStateManagement() {
        RenderContext context = createDummyRenderContext();

        NotCondition not = new NotCondition(conditionFactory);
        not.setSubCondition(TestCondition.TRUE);

        Serializable state = not.saveState();

        not = new NotCondition(conditionFactory);
        not.restoreState(state);

        assertThat("Sub condition should not be null.", not.getSubCondition(),
                is(notNullValue()));
        assertThat("'!true' should be false.", not.matches(context), is(false));
    }

    @Test
    public void testSettingsManagement() throws ConfigurationException {
        RenderContext context = createDummyRenderContext();

        NotCondition not = new NotCondition(conditionFactory);
        not.setSubCondition(TestCondition.FALSE);

        XMLConfiguration configuration = new XMLConfiguration();
        configuration.setRootElementName("condition");

        not.saveSettings(configuration);

        not = new NotCondition(conditionFactory);
        not.restoreSettings(configuration);

        assertThat("Sub condition should not be null.", not.getSubCondition(),
                is(notNullValue()));
        assertThat("'!false' should be true.", not.matches(context), is(true));

        System.out.println("Saved configuration : ");

        configuration.save(System.out);
    }
}
