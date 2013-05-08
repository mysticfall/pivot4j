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
import static org.junit.Assert.assertThat;

import java.io.Serializable;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;

import com.eyeq.pivot4j.ui.AbstractRenderTest;
import com.eyeq.pivot4j.ui.RenderContext;

public class SimplePropertyTest extends AbstractRenderTest {

	@Test
	public void testSimpleExpression() {
		RenderContext context = createDummyRenderContext();
		context.setColIndex(2);
		context.setRowIndex(1);

		Property property = new SimpleProperty("label",
				"(${rowIndex}, ${colIndex})");

		String result = property.getValue(context);

		assertThat("Wrong property value.", result, is(equalTo("(1, 2)")));
	}

	@Test
	public void testStateManagement() {
		SimpleProperty property = new SimpleProperty("bgColor", "red");

		Serializable state = property.saveState();

		SimpleProperty property2 = new SimpleProperty();

		property2.restoreState(state);

		assertThat("Property name has been changed.", property2.getName(),
				is(equalTo(property.getName())));
		assertThat("Property value has been changed.", property2.getValue(),
				is(equalTo(property.getValue())));
	}

	@Test
	public void testSettingsManagement() throws ConfigurationException {
		SimpleProperty property = new SimpleProperty("bgColor", "red");

		XMLConfiguration configuration = new XMLConfiguration();
		configuration.setRootElementName("property");

		property.saveSettings(configuration);

		SimpleProperty property2 = new SimpleProperty();
		property2.restoreSettings(configuration);

		assertThat("Property name has been changed.", property2.getName(),
				is(equalTo(property.getName())));
		assertThat("Property value has been changed.", property2.getValue(),
				is(equalTo(property.getValue())));

		System.out.println("Saved configuration : ");

		configuration.save(System.out);
	}
}
