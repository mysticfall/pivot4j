/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.condition;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.Serializable;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;
import org.olap4j.Axis;
import org.pivot4j.ui.RenderContext;
import org.pivot4j.ui.condition.ExpressionCondition;
import org.pivot4j.ui.table.TableRenderContext;

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
        TableRenderContext context = createDummyRenderContext();

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
        TableRenderContext context = createDummyRenderContext();
        context.setColIndex(2);
        context.setRowIndex(1);
        context.setAxis(Axis.ROWS);

        ExpressionCondition expression = new ExpressionCondition(
                conditionFactory);

        expression.setExpression("<#if columnIndex = 2>true</#if>");
        assertThat("Expression '" + expression.getExpression()
                + "' should be true.", expression.matches(context), is(true));

        expression.setExpression("<#if columnIndex != 2>true</#if>");
        assertThat("Expression '" + expression.getExpression()
                + "' should be false.", expression.matches(context), is(false));

        expression
                .setExpression("<#if columnIndex = 2 && rowIndex = 1>true</#if>");
        assertThat("Expression '" + expression.getExpression()
                + "' should be true.", expression.matches(context), is(true));

        expression.setExpression("<#if axis = \"ROWS\">true</#if>");
        assertThat("Expression '" + expression.getExpression()
                + "' should be true.", expression.matches(context), is(true));

        expression.setExpression("<#if axis = \"COLUMNS\">true</#if>");
        assertThat("Expression '" + expression.getExpression()
                + "' should be false.", expression.matches(context), is(false));
    }

    @Test
    public void testStateManagement() {
        TableRenderContext context = createDummyRenderContext();
        context.setColIndex(2);
        context.setRowIndex(1);
        context.setAxis(Axis.ROWS);

        String expression = "<#if columnIndex = 2 && rowIndex = 1>true</#if>";

        ExpressionCondition condition = new ExpressionCondition(
                conditionFactory);
        condition.setExpression(expression);

        Serializable state = condition.saveState();

        condition = new ExpressionCondition(conditionFactory);
        condition.restoreState(state);

        assertThat("Expression has been changed.", condition.getExpression(),
                is(equalTo(expression)));
        assertThat("Expression '" + expression + "' should be true.",
                condition.matches(context), is(true));
    }

    @Test
    public void testSettingsManagement() throws ConfigurationException {
        TableRenderContext context = createDummyRenderContext();
        context.setColIndex(2);
        context.setRowIndex(1);
        context.setAxis(Axis.ROWS);

        String expression = "<#if columnIndex = 2 && rowIndex = 1>true</#if>";

        ExpressionCondition condition = new ExpressionCondition(
                conditionFactory);
        condition.setExpression(expression);

        XMLConfiguration configuration = new XMLConfiguration();
        configuration.setRootElementName("condition");

        condition.saveSettings(configuration);

        condition = new ExpressionCondition(conditionFactory);
        condition.restoreSettings(configuration);

        assertThat("Expression has been changed.", condition.getExpression(),
                is(equalTo(expression)));
        assertThat("Expression '" + expression + "' should be true.",
                condition.matches(context), is(true));

        System.out.println("Saved configuration : ");

        configuration.save(System.out);
    }
}
