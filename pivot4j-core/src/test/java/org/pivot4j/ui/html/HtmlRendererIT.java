/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.html;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;
import org.pivot4j.AbstractIntegrationTestCase;
import org.pivot4j.PivotModel;
import org.pivot4j.ui.collector.NonInternalPropertyCollector;
import org.pivot4j.ui.html.HtmlRenderCallback;
import org.pivot4j.ui.table.TableRenderer;

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

		TableRenderer renderer = new TableRenderer();

		renderer.setHideSpans(hideSpans);
		renderer.setShowDimensionTitle(showDimensionTitle);
		renderer.setShowParentMembers(showParentMembers);

		if (name.contains("properties")) {
			renderer.setPropertyCollector(new NonInternalPropertyCollector());
		}

		HtmlRenderCallback callback = new HtmlRenderCallback(writer);
		callback.setBorder(1);

		renderer.render(model, callback);

		writer.flush();
		writer.close();

		String result = writer.toString().trim()
				.replace(System.getProperty("line.separator"), "\n");

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

	@Test
	public void testParentChildHierarchy() throws IOException {
		runTestCase("parent-child");
	}

	@Test
	public void testSingleDimensionCrossJoin() throws IOException {
		runTestCase("single-dim-crossjoin");
	}

	@Test
	public void testRaggedHierarchy() throws IOException {
		runTestCase("ragged");
	}
}
