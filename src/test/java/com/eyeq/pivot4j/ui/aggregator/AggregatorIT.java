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
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.olap4j.Axis;

import com.eyeq.pivot4j.AbstractIntegrationTestCase;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.ui.html.HtmlRenderer;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableHeader;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

public class AggregatorIT extends AbstractIntegrationTestCase {

	private boolean deleteTestFile = true;

	private HtmlTable table;

	/**
	 * @return the table
	 */
	protected HtmlTable getTable() {
		return table;
	}

	/**
	 * @see com.eyeq.pivot4j.AbstractIntegrationTestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();

		PivotModel model = getPivotModel();
		model.setMdx(readTestResource("mdx.txt"));
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
			renderer.setHideSpans(false);
			renderer.setShowDimensionTitle(true);
			renderer.setShowParentMembers(true);

			renderer.setAggregatorName(Axis.ROWS, AggregatorPosition.Grand,
					MinimumAggregator.NAME);
			renderer.setAggregatorName(Axis.ROWS, AggregatorPosition.Hierarchy,
					TotalAggregator.NAME);
			renderer.setAggregatorName(Axis.ROWS, AggregatorPosition.Member,
					AverageAggregator.NAME);

			renderer.setAggregatorName(Axis.COLUMNS, AggregatorPosition.Grand,
					MinimumAggregator.NAME);
			renderer.setAggregatorName(Axis.COLUMNS,
					AggregatorPosition.Hierarchy, TotalAggregator.NAME);
			renderer.setAggregatorName(Axis.COLUMNS, AggregatorPosition.Member,
					AverageAggregator.NAME);

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

	@Test
	public void testAggregation() throws IOException {
		HtmlTableHeader header = table.getHeader();

		List<HtmlTableRow> rows = header.getRows();

		assertThat("Table header is missing.", rows, is(notNullValue()));
		assertThat("Not enough column header rows.", rows.size(),
				is(equalTo(7)));

		// Column headers
		assertCell(rows, 0, 0, 5, 4, null);
		assertCell(rows, 0, 1, 1, 39, "Time");

		assertCell(rows, 1, 0, 2, 9, "Q1");
		assertCell(rows, 1, 1, 2, 9, "Q2");
		assertCell(rows, 1, 2, 1, 18, "Q2");
		assertCell(rows, 1, 3, 4, 3, "Minimum");

		assertCell(rows, 2, 0, 1, 9, "5");
		assertCell(rows, 2, 1, 1, 9, "6");

		for (int i = 0; i < 3; i++) {
			assertCell(rows, 3, i, 1, 9, "Gender");
		}

		for (int i = 0; i < 4; i++) {
			assertCell(rows, 4, i * 3, 1, 3, "F");
			assertCell(rows, 4, (i * 3) + 1, 1, 3, "M");
			assertCell(rows, 4, (i * 3) + 2, 1, 3, "Total");

		}

		assertCell(rows, 5, 0, 1, 1, "Promotion Media");
		assertCell(rows, 5, 1, 1, 1, "Marital Status");
		assertCell(rows, 5, 2, 1, 2, "Product");

		for (int i = 3; i < 16; i++) {
			assertCell(rows, 5, i, 1, 3, "Measures");
		}

		assertCell(rows, 6, 0, 1, 1, "Media Type");
		assertCell(rows, 6, 1, 1, 1, "Marital Status");
		assertCell(rows, 6, 2, 1, 1, "Product Family");
		assertCell(rows, 6, 3, 1, 1, "Product Department");

		for (int i = 0; i < 13; i++) {
			assertCell(rows, 6, (i * 3) + 4, 1, 1, "Store Sales");
			assertCell(rows, 6, (i * 3) + 5, 1, 1, "Store Cost");
			assertCell(rows, 6, (i * 3) + 6, 1, 1, "Unit Sales");
		}

		// Table content
		List<HtmlTableBody> bodies = table.getBodies();

		assertThat("Table body is missing.", bodies, is(notNullValue()));
		assertThat("Table body is missing.", bodies.size(), is(equalTo(1)));

		rows = bodies.get(0).getRows();

		assertThat("Table content is missing.", rows, is(notNullValue()));
		assertThat("Not enough content rows.", rows.size(), is(equalTo(47)));

		assertCell(rows, 0, 0, 23, 1, "Bulk Mail");
		assertCell(rows, 0, 1, 11, 1, "M");
		assertCell(rows, 0, 2, 4, 1, "Drink");
		assertCell(rows, 0, 3, 1, 1, "Alcoholic Beverages");

		assertCell(rows, 1, 0, 1, 1, "Beverages");
		assertCell(rows, 2, 0, 1, 1, "Dairy");
		assertCell(rows, 3, 0, 1, 1, "Average");
		assertCell(rows, 4, 0, 1, 2, "Food");

		assertCell(rows, 5, 0, 4, 1, "Food");
		assertCell(rows, 5, 1, 1, 1, "Dairy");
		assertCell(rows, 6, 0, 1, 1, "Eggs");
		assertCell(rows, 7, 0, 1, 1, "Frozen Foods");
		assertCell(rows, 8, 0, 1, 1, "Average");
		assertCell(rows, 9, 0, 1, 2, "Non-Consumable");

		// Member total title
		assertCell(rows, 10, 0, 1, 2, "Total");

		// Hierarchy total title
		assertCell(rows, 22, 0, 1, 3, "Total");

		// Axis total title
		assertCell(rows, 46, 0, 1, 4, "Minimum");

		// Row axis member aggregation
		// (Bulk Mail, M, Food, Average / Q1, F, Store Sales)
		assertCell(rows, 8, 7, 1, 1, "26.00");

		// Row axis hierarchy aggregation
		// (Daily Paper, S, Total / Q1, F, Store Sales)
		assertCell(rows, 44, 1, 1, 1, "887.15");

		// Row axis hierarchy aggregation
		// (Daily Paper, Total / Q1, F, Store Sales)
		assertCell(rows, 45, 1, 1, 1, "2,052.52");

		// Row axis aggregation
		// (Minimum / Q1, F, Store Sales)
		assertCell(rows, 46, 1, 1, 1, "2.26");

		// Column axis hierarchy aggregation
		// (Daily Paper, S, Drink, Average / Q2, Total, Store Cost)
		assertCell(rows, 37, 17, 1, 1, "7.94");

		// Column axis hierarchy aggregation
		// (Bulk Mail, M, Drink, Alcoholic Beverages / Q2, 6, Total, Store Cost)
		assertCell(rows, 0, 38, 1, 1, "13.88");

		// Column axis aggregation
		// (Bulk Mail, M, Food, Frozen Foods / Minimum, Unit Sales)
		assertCell(rows, 7, 39, 1, 1, "8");
	}

	/**
	 * @param cell
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
