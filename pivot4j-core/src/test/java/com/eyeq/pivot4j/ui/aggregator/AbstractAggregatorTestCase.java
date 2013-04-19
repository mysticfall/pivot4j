/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.aggregator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.eyeq.pivot4j.AbstractIntegrationTestCase;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.ui.PivotRenderer;
import com.eyeq.pivot4j.ui.html.HtmlRenderer;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

public abstract class AbstractAggregatorTestCase extends
		AbstractIntegrationTestCase {

	private boolean deleteTestFile = true;

	private HtmlTable table;

	/**
	 * @return the table
	 */
	protected HtmlTable getTable() {
		return table;
	}

	/**
	 * @return the deleteTestFile
	 */
	protected boolean getDeleteTestFile() {
		return deleteTestFile;
	}

	/**
	 * @param deleteTestFile
	 *            the deleteTestFile to set
	 */
	protected void setDeleteTestFile(boolean deleteTestFile) {
		this.deleteTestFile = deleteTestFile;
	}

	protected abstract String getQueryName();

	/**
	 * @see com.eyeq.pivot4j.AbstractIntegrationTestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();

		PivotModel model = getPivotModel();
		model.setMdx(readTestResource(getQueryName() + ".txt"));
		model.initialize();

		Writer writer = null;

		File file = File.createTempFile("pivot4j-", ".html");

		if (deleteTestFile) {
			file.deleteOnExit();
		}

		try {
			writer = new FileWriter(file);

			HtmlRenderer renderer = new HtmlRenderer(writer);
			renderer.initialize();
			renderer.setTableId("pivot");
			renderer.setBorder(1);

			configureRenderer(renderer);
			configureAggregators(renderer);

			renderer.render(model);
		} finally {
			writer.flush();
			IOUtils.closeQuietly(writer);
		}

		WebClient webClient = new WebClient();
		HtmlPage page = webClient.getPage(file.toURI().toURL());

		this.table = page.getHtmlElementById("pivot");

		assertThat("Table element is not found.", table, is(notNullValue()));
	}

	/**
	 * @see com.eyeq.pivot4j.AbstractIntegrationTestCase#tearDown()
	 */
	@Override
	public void tearDown() throws Exception {
		super.tearDown();

		this.table = null;
	}

	/**
	 * @param renderer
	 */
	protected void configureRenderer(PivotRenderer renderer) {
		renderer.setHideSpans(false);
		renderer.setShowDimensionTitle(true);
		renderer.setShowParentMembers(true);
	}

	/**
	 * @param renderer
	 */
	protected abstract void configureAggregators(PivotRenderer renderer);

	/**
	 * @param rows
	 * @param rowIndex
	 * @param colIndex
	 * @param rowSpan
	 * @param colSpan
	 * @param label
	 */
	protected void assertCell(List<HtmlTableRow> rows, int rowIndex,
			int colIndex, int rowSpan, int colSpan, String label) {
		String coords = String.format("(%s, %s).", rowIndex, colIndex);

		assertThat("Insufficient row count" + coords, rows.size(),
				is(greaterThan(rowIndex)));

		HtmlTableRow row = rows.get(rowIndex);

		assertThat("Insufficient column count" + coords, row.getCells().size(),
				is(greaterThan(colIndex)));

		HtmlTableCell cell = row.getCell(colIndex);

		assertThat("Wrong row span of header cell" + coords, cell.getRowSpan(),
				is(equalTo(rowSpan)));

		assertThat("Wrong column span of header cell" + coords,
				cell.getColumnSpan(), is(equalTo(colSpan)));

		if (label != null) {
			assertThat("Unexpected cell content" + coords,
					cell.getTextContent(), is(equalToIgnoringWhiteSpace(label)));
		}
	}
}
