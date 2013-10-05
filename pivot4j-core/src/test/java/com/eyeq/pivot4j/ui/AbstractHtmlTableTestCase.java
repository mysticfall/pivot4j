/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.eyeq.pivot4j.AbstractIntegrationTestCase;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.ui.html.HtmlRenderCallback;
import com.eyeq.pivot4j.ui.table.TableRenderer;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

public abstract class AbstractHtmlTableTestCase extends
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
			TableRenderer renderer = new TableRenderer();
			configureRenderer(renderer);

			writer = new FileWriter(file);

			HtmlRenderCallback callback = new HtmlRenderCallback(writer);
			callback.setTableId("pivot");
			callback.setBorder(1);

			renderer.render(model, callback);
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
	protected void configureRenderer(TableRenderer renderer) {
		renderer.setHideSpans(false);
		renderer.setShowDimensionTitle(true);
		renderer.setShowParentMembers(true);
	}

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

	/**
	 * @param rows
	 * @param rowIndex
	 * @param colIndex
	 * @return
	 */
	protected Map<String, String> getCellStyles(List<HtmlTableRow> rows,
			int rowIndex, int colIndex) {
		Map<String, String> styles = new HashMap<String, String>();

		HtmlTableRow row = rows.get(rowIndex);

		if (row != null) {
			HtmlTableCell cell = row.getCell(colIndex);

			if (cell != null) {
				String style = cell.getAttribute("style");

				if (style != null) {
					String[] pairs = style.split(";");

					for (String pair : pairs) {
						String[] values = pair.split(":");

						if (values.length == 2) {
							styles.put(values[0].trim(), values[1].trim());
						}
					}
				}
			}
		}

		return styles;
	}
}
