/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.html;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;

import com.eyeq.pivot4j.AbstractIntegrationTestCase;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.ui.impl.NonInternalPropertyCollector;

public class HtmlRendererIT extends AbstractIntegrationTestCase {

	/**
	 * @param name
	 * @throws IOException
	 */
	protected void runTestCase(String name) throws IOException {
		runTestCase(name, true, true, true);
		runTestCase(name, true, true, false);
		runTestCase(name, true, false, false);
		runTestCase(name, true, false, true);
		runTestCase(name, false, true, true);
		runTestCase(name, false, true, false);
		runTestCase(name, false, false, true);
		runTestCase(name, false, false, false);
	}

	/**
	 * @param name
	 * @param hideSpans
	 * @param showDimensionTitle
	 * @param showParentMembers
	 * @throws IOException
	 */
	protected void runTestCase(String name, boolean hideSpans,
			boolean showDimensionTitle, boolean showParentMembers)
			throws IOException {
		String mdx = readTestResource(name + "-mdx.txt");

		PivotModel model = getPivotModel();
		model.setMdx(mdx);
		model.initialize();

		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append("-");
		sb.append(Boolean.toString(hideSpans));
		sb.append("-");
		sb.append(Boolean.toString(showDimensionTitle));
		sb.append("-");
		sb.append(Boolean.toString(showParentMembers));
		sb.append("-result.html");

		String fileName = sb.toString();

		StringWriter writer = new StringWriter();

		HtmlRenderer renderer = new HtmlRenderer(writer);
		renderer.initialize();

		renderer.setBorder(1);
		renderer.setHideSpans(hideSpans);
		renderer.setShowDimensionTitle(showDimensionTitle);
		renderer.setShowParentMembers(showParentMembers);
		renderer.setPropertyCollector(new NonInternalPropertyCollector());

		renderer.render(model);

		writer.flush();
		writer.close();

		String result = writer.toString().trim();

		String expected = readTestResource(fileName);

		String message = String
				.format("Unexpected result : %s, hideSpans=%s, showDimensionTitle=%s, showParentMembers=%s",
						name, hideSpans, showDimensionTitle, showParentMembers);
		assertThat(message, result, is(equalTo(expected)));
	}

	@Test
	public void testBasicGrid() throws IOException {
		runTestCase("basic");
	}

	@Test
	public void testComplexColumns() throws IOException {
		runTestCase("complex-columns");
	}

	@Test
	public void testMoreComplexColumns() throws IOException {
		runTestCase("more-complex-columns");
	}

	@Test
	public void testInsaneRows() throws IOException {
		runTestCase("insane-rows");
	}

	@Test
	public void testSkippingLevel() throws IOException {
		runTestCase("skipping-level");
	}

	@Test
	public void testMemberProperties() throws IOException {
		runTestCase("member-properties");
	}

	@Test
	public void testComplexMemberProperties() throws IOException {
		runTestCase("complex-member-properties");
	}
}
