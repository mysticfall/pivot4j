/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.property;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.eyeq.pivot4j.ui.AbstractMockRenderTestCase;
import com.eyeq.pivot4j.ui.RenderContext;
import com.eyeq.pivot4j.ui.condition.ConditionFactory;
import com.eyeq.pivot4j.ui.condition.DefaultConditionFactory;
import com.eyeq.pivot4j.ui.condition.ExpressionCondition;
import com.eyeq.pivot4j.ui.condition.OrCondition;
import com.eyeq.pivot4j.ui.table.TableRenderContext;

public class ConditionalPropertyTest extends AbstractMockRenderTestCase {

	private ConditionalRenderProperty property;

	@Before
	public void setUp() throws Exception {
		ConditionFactory factory = new DefaultConditionFactory();

		this.property = new ConditionalRenderProperty("bgColor", factory);

		ExpressionCondition expression1 = new ExpressionCondition(factory,
				"<#if rowIndex = 3>true</#if>");
		ExpressionCondition expression2 = new ExpressionCondition(factory,
				"<#if rowIndex = 4>true</#if>");

		OrCondition or = new OrCondition(factory, expression1, expression2);

		List<ConditionalValue> values = new LinkedList<ConditionalValue>();

		values.add(new ConditionalValue(new ExpressionCondition(factory,
				"<#if columnIndex = 1>true</#if>"), "red"));
		values.add(new ConditionalValue(or, "blue"));

		property.setDefaultValue("black");
		property.setValues(values);
	}

	@After
	public void tearDown() throws Exception {
		this.property = null;
	}

	@Test
	public void testNullValue() {
		RenderContext context = createDummyRenderContext();

		property.setValues(null);

		String result = property.getValue(context);

		assertThat("Wrong property value.", result, is(nullValue()));
	}

	@Test
	public void testDefaultValue() {
		TableRenderContext context = createDummyRenderContext();
		context.setColIndex(2);
		context.setRowIndex(1);

		String result = property.getValue(context);

		assertThat("Wrong property value.", result, is(equalTo("black")));
	}

	@Test
	public void testSimpleValue() {
		TableRenderContext context = createDummyRenderContext();
		context.setColIndex(1);
		context.setRowIndex(2);

		String result = property.getValue(context);

		assertThat("Wrong property value.", result, is(equalTo("red")));
	}

	@Test
	public void testConditionValue() {
		TableRenderContext context = createDummyRenderContext();
		context.setColIndex(2);
		context.setRowIndex(4);

		String result = property.getValue(context);

		assertThat("Wrong property value.", result, is(equalTo("blue")));
	}

	@Test
	public void testStateManagement() {
		Serializable state = property.saveState();

		ConditionalRenderProperty property2 = new ConditionalRenderProperty(
				new DefaultConditionFactory());

		property2.restoreState(state);

		assertThat("RenderProperty name has been changed.",
				property2.getName(), is(equalTo(property.getName())));
		assertThat("Default value has been changed.",
				property2.getDefaultValue(),
				is(equalTo(property.getDefaultValue())));

		TableRenderContext context = createDummyRenderContext();
		context.setColIndex(2);
		context.setRowIndex(4);

		String result = property2.getValue(context);

		assertThat("Wrong property value.", result, is(equalTo("blue")));
	}

	@Test
	public void testSettingsManagement() throws ConfigurationException {
		XMLConfiguration configuration = new XMLConfiguration();
		configuration.setRootElementName("property");

		property.saveSettings(configuration);
		System.out.println("Saved configuration : ");
		configuration.save(System.out);
		ConditionalRenderProperty property2 = new ConditionalRenderProperty(
				new DefaultConditionFactory());
		property2.restoreSettings(configuration);

		assertThat("RenderProperty name has been changed.",
				property2.getName(), is(equalTo(property.getName())));
		assertThat("Default value has been changed.",
				property2.getDefaultValue(),
				is(equalTo(property.getDefaultValue())));

		TableRenderContext context = createDummyRenderContext();
		context.setColIndex(2);
		context.setRowIndex(4);

		String result = property2.getValue(context);

		assertThat("Wrong property value.", result, is(equalTo("blue")));

		System.out.println("Saved configuration : ");
		configuration.save(System.out);
	}
}
