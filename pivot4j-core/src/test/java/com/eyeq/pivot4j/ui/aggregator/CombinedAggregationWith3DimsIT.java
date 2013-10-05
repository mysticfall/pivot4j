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
import com.eyeq.pivot4j.ui.table.TableRenderer;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

public class CombinedAggregationWith3DimsIT extends AbstractHtmlTableTestCase {

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractHtmlTableTestCase#getQueryName()
	 */
	@Override
	protected String getQueryName() {
		return "3-dims-combined";
	}

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractHtmlTableTestCase#configureRenderer(com.eyeq.pivot4j.ui.table.TableRenderer)
	 */
	@Override
	protected void configureRenderer(TableRenderer renderer) {
		super.configureRenderer(renderer);

		renderer.addAggregator(Axis.ROWS, AggregatorPosition.Hierarchy,
				AverageAggregator.NAME);
		renderer.addAggregator(Axis.ROWS, AggregatorPosition.Member,
				TotalAggregator.NAME);
	}

	@Test
	public void testAggregation() throws IOException {
		HtmlTable table = getTable();

		// Table content
		List<HtmlTableBody> bodies = table.getBodies();

		assertThat("Table body is missing.", bodies, is(notNullValue()));
		assertThat("Table body is missing.", bodies.size(), is(equalTo(1)));

		List<HtmlTableRow> rows = bodies.get(0).getRows();

		assertThat("Table content is missing.", rows, is(notNullValue()));
		assertThat("Not enough content rows.", rows.size(), is(equalTo(25)));

		// All Products / All Marital Status / All Gender
		assertCell(rows, 1, 0, 1, 2, "Average");
		assertCell(rows, 1, 1, 1, 1, "266,773");

		// All Products / M / All Gender
		assertCell(rows, 5, 0, 1, 1, "Total");
		assertCell(rows, 5, 1, 1, 1, "131,796");

		assertCell(rows, 6, 0, 1, 2, "Average");
		assertCell(rows, 6, 1, 1, 1, "65,898");

		// All Products / S / All Gender
		assertCell(rows, 10, 0, 1, 1, "Total");
		assertCell(rows, 10, 1, 1, 1, "134,977");

		assertCell(rows, 11, 0, 1, 2, "Average");
		assertCell(rows, 11, 1, 1, 1, "67,488.5");

		assertCell(rows, 12, 0, 1, 3, "Total");
		assertCell(rows, 12, 1, 1, 1, "266,773");

		// All Products / All Marital Status / All Gender
		assertCell(rows, 13, 0, 1, 4, "Average");
		assertCell(rows, 13, 1, 1, 1, "66,693.25");

		// Drinks / All Marital Status / All Gender
		assertCell(rows, 15, 0, 1, 2, "Average");
		assertCell(rows, 15, 1, 1, 1, "24,597");

		// Drinks / All Marital Status
		assertCell(rows, 16, 0, 1, 4, "Average");
		assertCell(rows, 16, 1, 1, 1, "24,597");

		// Food / All Marital Status / All Gender
		assertCell(rows, 18, 0, 1, 2, "Average");
		assertCell(rows, 18, 1, 1, 1, "191,940");

		// Food / All Marital Status
		assertCell(rows, 19, 0, 1, 4, "Average");
		assertCell(rows, 19, 1, 1, 1, "191,940");

		// Non-Consumable / All Marital Status / All Gender
		assertCell(rows, 21, 0, 1, 2, "Average");
		assertCell(rows, 21, 1, 1, 1, "50,236");

		// Non-Consumable / All Marital Status
		assertCell(rows, 22, 0, 1, 4, "Average");
		assertCell(rows, 22, 1, 1, 1, "50,236");

		// All Products
		assertCell(rows, 23, 0, 1, 5, "Total");
		assertCell(rows, 23, 1, 1, 1, "266,773");

		// Grand
		assertCell(rows, 24, 0, 1, 6, "Average");
		assertCell(rows, 24, 1, 1, 1, "88,924.333");
	}
}
