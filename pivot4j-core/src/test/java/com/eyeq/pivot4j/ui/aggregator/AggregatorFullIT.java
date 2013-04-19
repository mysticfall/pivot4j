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
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.olap4j.Axis;

import com.eyeq.pivot4j.ui.PivotRenderer;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableHeader;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

public class AggregatorFullIT extends AbstractAggregatorTestCase {

	/**
	 * @see com.eyeq.pivot4j.ui.aggregator.AbstractAggregatorTestCase#getQueryName()
	 */
	@Override
	protected String getQueryName() {
		return "full";
	}

	/**
	 * @param renderer
	 * @see com.eyeq.pivot4j.ui.aggregator.AbstractAggregatorTestCase#configureAggregators(com.eyeq.pivot4j.ui.PivotRenderer)
	 */
	@Override
	protected void configureAggregators(PivotRenderer renderer) {
		renderer.addAggregator(Axis.ROWS, AggregatorPosition.Grand,
				MinimumAggregator.NAME);
		renderer.addAggregator(Axis.ROWS, AggregatorPosition.Hierarchy,
				TotalAggregator.NAME);
		renderer.addAggregator(Axis.ROWS, AggregatorPosition.Member,
				AverageAggregator.NAME);

		renderer.addAggregator(Axis.COLUMNS, AggregatorPosition.Grand,
				MinimumAggregator.NAME);
		renderer.addAggregator(Axis.COLUMNS, AggregatorPosition.Hierarchy,
				TotalAggregator.NAME);
		renderer.addAggregator(Axis.COLUMNS, AggregatorPosition.Member,
				AverageAggregator.NAME);
	}

	@Test
	public void testAggregation() throws IOException {
		HtmlTable table = getTable();

		HtmlTableHeader header = table.getHeader();

		List<HtmlTableRow> rows = header.getRows();

		assertThat("Table header is missing.", rows, is(notNullValue()));
		assertThat("Not enough column header rows.", rows.size(),
				is(equalTo(7)));

		// Column headers
		assertCell(rows, 0, 0, 5, 4, null);
		assertCell(rows, 0, 1, 1, 45, "Time");

		assertCell(rows, 1, 0, 2, 9, "Q1");
		assertCell(rows, 1, 1, 2, 9, "Q2");
		assertCell(rows, 1, 2, 1, 21, "Q2");
		assertCell(rows, 1, 3, 4, 3, "Total");
		assertCell(rows, 1, 4, 4, 3, "Minimum");

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
		assertThat("Not enough content rows.", rows.size(), is(equalTo(48)));

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
		assertCell(rows, 47, 0, 1, 4, "Minimum");

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
		assertCell(rows, 47, 1, 1, 1, "2.26");

		// Column axis member aggregation
		// (Bulk Mail, M, Food, Frozen Foods / Q2, 6, Average, Unit Sales)
		assertCell(rows, 7, 39, 1, 1, "16.5");

		// Column axis hierarchy aggregation
		// (Daily Paper, S, Drink, Average / Q2, Total, Store Cost)
		assertCell(rows, 37, 17, 1, 1, "7.94");

		// Column axis hierarchy aggregation
		// (Bulk Mail, M, Drink, Alcoholic Beverages / Q2, 6, Total, Store Cost)
		assertCell(rows, 0, 38, 1, 1, "13.88");

		// Column axis aggregation
		// (Bulk Mail, M, Food, Frozen Foods / Minimum, Unit Sales)
		assertCell(rows, 7, 45, 1, 1, "8");
	}
}
