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

import com.eyeq.pivot4j.ui.AbstractHtmlTableTestCase;
import com.eyeq.pivot4j.ui.PivotRenderer;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableHeader;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

public class ComplexAggregationIT extends AbstractHtmlTableTestCase {

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractHtmlTableTestCase#getQueryName()
	 */
	@Override
	protected String getQueryName() {
		return "complex";
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractHtmlTableTestCase#configureRenderer(com.eyeq.pivot4j.ui.PivotRenderer)
	 */
	@Override
	protected void configureRenderer(PivotRenderer renderer) {
		super.configureRenderer(renderer);

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
				is(equalTo(9)));

		// Column headers
		assertCell(rows, 0, 0, 7, 7, null);
		assertCell(rows, 0, 1, 1, 45, "Time");
		assertCell(rows, 1, 0, 1, 39, "1997");
		assertCell(rows, 1, 1, 6, 3, "Total");
		assertCell(rows, 1, 2, 6, 3, "Minimum");
		assertCell(rows, 2, 0, 2, 9, "Q1");
		assertCell(rows, 2, 1, 2, 9, "Q2");
		assertCell(rows, 2, 2, 1, 21, "Q2");

		assertCell(rows, 3, 0, 1, 9, "5");
		assertCell(rows, 3, 1, 1, 9, "6");

		for (int i = 0; i < 3; i++) {
			assertCell(rows, 4, i, 1, 9, "Gender");
		}

		assertCell(rows, 5, 0, 1, 6, "All Gender");
		assertCell(rows, 5, 1, 2, 3, "Total");

		for (int i = 0; i < 3; i++) {
			assertCell(rows, 5, (i * 2) + 2, 1, 6, "All Gender");
			assertCell(rows, 5, (i * 2) + 3, 2, 3, "Total");
		}

		for (int i = 0; i < 4; i++) {
			assertCell(rows, 6, (i * 2), 1, 3, "F");
			assertCell(rows, 6, (i * 2) + 1, 1, 3, "M");
		}

		assertCell(rows, 7, 0, 1, 2, "Promotion Media");
		assertCell(rows, 7, 1, 1, 2, "Marital Status");
		assertCell(rows, 7, 2, 1, 3, "Product");

		for (int i = 3; i < 16; i++) {
			assertCell(rows, 7, i, 1, 3, "Measures");
		}

		assertCell(rows, 8, 0, 1, 1, "(All)");
		assertCell(rows, 8, 1, 1, 1, "Media Type");
		assertCell(rows, 8, 2, 1, 1, "(All)");
		assertCell(rows, 8, 3, 1, 1, "Marital Status");
		assertCell(rows, 8, 4, 1, 1, "(All)");
		assertCell(rows, 8, 5, 1, 1, "Product Family");
		assertCell(rows, 8, 6, 1, 1, "Product Department");

		for (int i = 0; i < 13; i++) {
			assertCell(rows, 8, (i * 3) + 7, 1, 1, "Store Sales");
			assertCell(rows, 8, (i * 3) + 8, 1, 1, "Store Cost");
			assertCell(rows, 8, (i * 3) + 9, 1, 1, "Unit Sales");
		}

		// Table content
		List<HtmlTableBody> bodies = table.getBodies();

		assertThat("Table body is missing.", bodies, is(notNullValue()));
		assertThat("Table body is missing.", bodies.size(), is(equalTo(1)));

		rows = bodies.get(0).getRows();

		assertThat("Table content is missing.", rows, is(notNullValue()));
		assertThat("Not enough content rows.", rows.size(), is(equalTo(51)));

		assertCell(rows, 0, 0, 49, 1, "All Media");
		assertCell(rows, 0, 1, 25, 1, "Bulk Mail");
		assertCell(rows, 0, 2, 24, 1, "All Marital Status");
		assertCell(rows, 0, 3, 12, 1, "M");
		assertCell(rows, 0, 4, 11, 1, "All Products");
		assertCell(rows, 0, 5, 4, 1, "Drink");
		assertCell(rows, 0, 6, 1, 1, "Alcoholic Beverages");

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
		assertCell(rows, 10, 0, 1, 1, "Drink");
		assertCell(rows, 10, 1, 1, 1, "Average");
		assertCell(rows, 11, 0, 1, 3, "Total");

		// Hierarchy total title
		assertCell(rows, 23, 0, 1, 3, "Total");

		// Axis total title
		assertCell(rows, 50, 0, 1, 7, "Minimum");

		// Row axis member aggregation
		// (Bulk Mail, M, Food, Average / Q1, F, Store Sales)
		assertCell(rows, 8, 7, 1, 1, "26.00");

		// Row axis hierarchy aggregation
		// (Daily Paper, S, Total / Q1, F, Store Sales)
		assertCell(rows, 47, 1, 1, 1, "887.15");

		// Row axis hierarchy aggregation
		// (Daily Paper, Total / Q1, F, Store Sales)
		assertCell(rows, 48, 1, 1, 1, "2,052.52");

		// Row axis aggregation
		// (Minimum / Q1, F, Store Sales)
		assertCell(rows, 50, 1, 1, 1, "2.26");

		// Column axis member aggregation
		// (Bulk Mail, M, Food, Frozen Foods / Q2, 6, Average, Unit Sales)
		assertCell(rows, 7, 39, 1, 1, "11");

		// Column axis hierarchy aggregation
		// (Daily Paper, S, Drink, Average / Q2, Total, Store Cost)
		assertCell(rows, 40, 17, 1, 1, "7.94");

		// Column axis hierarchy aggregation
		// (Bulk Mail, M, Drink, Alcoholic Beverages / Q2, 6, Total, Store Cost)
		assertCell(rows, 0, 41, 1, 1, "13.88");

		// Column axis aggregation
		// (Bulk Mail, M, Food, Frozen Foods / Minimum, Unit Sales)
		assertCell(rows, 7, 45, 1, 1, "8");
	}
}
