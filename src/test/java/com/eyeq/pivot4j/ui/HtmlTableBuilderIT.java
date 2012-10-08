/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

import com.eyeq.pivot4j.AbstractIntegrationTestCase;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.ui.html.HtmlTableBuilder;
import com.eyeq.pivot4j.ui.html.HtmlTableModel;

public class HtmlTableBuilderIT extends AbstractIntegrationTestCase {

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

	protected void runTestCase(String name) throws IOException {
		String mdx = readTestResource("./" + name + "-mdx.txt");

		PivotModel model = getPivotModel();
		model.setMdx(mdx);
		model.initialize();

		HtmlTableBuilder builder = new HtmlTableBuilder();
		builder.setHideSpans(false);
		builder.setShowDimensionTitle(true);
		builder.setShowParentMembers(true);

		HtmlTableModel table = builder.build(model);
		table.setBorder(1);

		StringWriter writer = new StringWriter();
		table.writeHtml(new PrintWriter(writer), 0);
		writer.flush();
		writer.close();

		String result = writer.toString().trim();
		String expected = readTestResource("./" + name + "-result.html");

		assertEquals(expected, result);
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
}
