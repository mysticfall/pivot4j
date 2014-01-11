/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.property;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.Serializable;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.ObjectUtils;
import org.junit.Test;
import org.pivot4j.ui.AbstractMockRenderTestCase;
import org.pivot4j.ui.property.RenderProperty;
import org.pivot4j.ui.property.SimpleRenderProperty;
import org.pivot4j.ui.table.TableRenderContext;

public class SimplePropertyTest extends AbstractMockRenderTestCase {

	@Test
	public void testSimpleExpression() {
		TableRenderContext context = createDummyRenderContext();
		context.setColIndex(2);
		context.setRowIndex(1);

		RenderProperty property = new SimpleRenderProperty("label",
				"(${rowIndex}, ${columnIndex})");

		String result = ObjectUtils.toString(property.getValue(context));

		assertThat("Wrong property value.", result, is(equalTo("(1, 2)")));
	}

	@Test
	public void testStateManagement() {
		SimpleRenderProperty property = new SimpleRenderProperty("bgColor",
				"red");

		Serializable state = property.saveState();

		SimpleRenderProperty property2 = new SimpleRenderProperty();

		property2.restoreState(state);

		assertThat("RenderProperty name has been changed.",
				property2.getName(), is(equalTo(property.getName())));
		assertThat("RenderProperty value has been changed.",
				property2.getValue(), is(equalTo(property.getValue())));
	}

	@Test
	public void testSettingsManagement() throws ConfigurationException {
		SimpleRenderProperty property = new SimpleRenderProperty("bgColor",
				"red");

		XMLConfiguration configuration = new XMLConfiguration();
		configuration.setRootElementName("property");

		property.saveSettings(configuration);

		SimpleRenderProperty property2 = new SimpleRenderProperty();
		property2.restoreSettings(configuration);

		assertThat("RenderProperty name has been changed.",
				property2.getName(), is(equalTo(property.getName())));
		assertThat("RenderProperty value has been changed.",
				property2.getValue(), is(equalTo(property.getValue())));

		System.out.println("Saved configuration : ");

		configuration.save(System.out);
	}
}
