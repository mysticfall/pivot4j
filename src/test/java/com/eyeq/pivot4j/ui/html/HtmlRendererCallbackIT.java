/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.html;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.junit.Test;

import com.eyeq.pivot4j.AbstractIntegrationTestCase;
import com.eyeq.pivot4j.PivotModel;

public class HtmlRendererCallbackIT extends AbstractIntegrationTestCase {

	private static final String RESOURCE_PREFIX = "/com/eyeq/pivot4j/ui/html/";

	/**
	 * @param name
	 * @return
	 * @throws IOException
	 */
	protected String readTestResource(String name) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream(name)));

		StringWriter writer = new StringWriter();

		String line = null;
		while ((line = reader.readLine()) != null) {
			writer.append(line);
			writer.append('\n');
		}

		reader.close();
		writer.flush();
		writer.close();

		return writer.toString().trim();
	}

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
		String mdx = readTestResource(RESOURCE_PREFIX + name + "-mdx.txt");

		PivotModel model = getPivotModel();
		model.setMdx(mdx);
		model.setHideSpans(hideSpans);
		model.setShowDimensionTitle(showDimensionTitle);
		model.setShowParentMembers(showParentMembers);

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
		renderer.setBorder(1);

		model.render(renderer);

		writer.flush();
		writer.close();

		String result = writer.toString().trim();
		String expected = readTestResource(RESOURCE_PREFIX + fileName);

		String message = String
				.format("Unexpected result : %s, hideSpans=%s, showDimensionTitle=%s, showParentMembers=%s",
						name, hideSpans, showDimensionTitle, showParentMembers);
		assertEquals(message, expected, result);
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
}
