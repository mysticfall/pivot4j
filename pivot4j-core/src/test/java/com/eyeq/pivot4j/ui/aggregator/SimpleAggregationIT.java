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
import com.gargoylesoftware.htmlunit.html.HtmlTableHeader;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

public class SimpleAggregationIT extends AbstractHtmlTableTestCase {

	/**
	 * @see com.eyeq.pivot4j.ui.AbstractHtmlTableTestCase#getQueryName()
	 */
	@Override
	protected String getQueryName() {
		return "simple";
	}

	/**
	 * @param renderer
	 * @see com.eyeq.pivot4j.ui.AbstractHtmlTableTestCase#configureRenderer(com.eyeq.pivot4j.ui.table.TableRenderer)
	 */
	@Override
	protected void configureRenderer(TableRenderer renderer) {
		super.configureRenderer(renderer);

		renderer.setShowParentMembers(false);

		renderer.addAggregator(Axis.ROWS, AggregatorPosition.Grand,
				TotalAggregator.NAME);
		renderer.addAggregator(Axis.COLUMNS, AggregatorPosition.Grand,
				TotalAggregator.NAME);
	}

	@Test
	public void testAggregation() throws IOException {
		HtmlTable table = getTable();

		HtmlTableHeader header = table.getHeader();

		List<HtmlTableRow> rows = header.getRows();

		assertThat("Table header is missing.", rows, is(notNullValue()));
		assertThat("Not enough column header rows.", rows.size(),
				is(equalTo(3)));

		assertCell(rows, 0, 0, 2, 1, null);
		assertCell(rows, 0, 1, 1, 4, "Measures");

		assertCell(rows, 1, 0, 2, 1, "Store Cost");
		assertCell(rows, 1, 1, 2, 1, "Unit Sales");
		assertCell(rows, 1, 2, 1, 2, "Total");

		assertCell(rows, 2, 0, 1, 1, "Product");
		assertCell(rows, 2, 1, 1, 1, "Store Cost");
		assertCell(rows, 2, 2, 1, 1, "Unit Sales");

		List<HtmlTableBody> bodies = table.getBodies();

		assertThat("Table body is missing.", bodies, is(notNullValue()));
		assertThat("Table body is missing.", bodies.size(), is(equalTo(1)));

		rows = bodies.get(0).getRows();

		assertThat("Table content is missing.", rows, is(notNullValue()));
		assertThat("Not enough content rows.", rows.size(), is(equalTo(2)));

		assertCell(rows, 0, 0, 1, 1, "All Products");
		assertCell(rows, 0, 1, 1, 1, "225,627.23");
		assertCell(rows, 0, 2, 1, 1, "266,773");
		assertCell(rows, 0, 3, 1, 1, "225,627.23");
		assertCell(rows, 0, 4, 1, 1, "266,773");

		assertCell(rows, 1, 0, 1, 1, "Total");
		assertCell(rows, 1, 1, 1, 1, "225,627.23");
		assertCell(rows, 1, 2, 1, 1, "266,773");
		assertCell(rows, 1, 3, 1, 1, "225,627.23");
		assertCell(rows, 1, 4, 1, 1, "266,773");
	}
}
