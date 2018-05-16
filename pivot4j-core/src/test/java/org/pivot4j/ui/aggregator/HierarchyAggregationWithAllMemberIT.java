/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.aggregator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.olap4j.Axis;
import org.pivot4j.ui.AbstractHtmlTableTestCase;
import org.pivot4j.ui.aggregator.AggregatorPosition;
import org.pivot4j.ui.aggregator.TotalAggregator;
import org.pivot4j.ui.table.TableRenderer;

import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

public class HierarchyAggregationWithAllMemberIT extends
        AbstractHtmlTableTestCase {

    /**
     * @see org.pivot4j.ui.AbstractHtmlTableTestCase#getQueryName()
     */
    @Override
    protected String getQueryName() {
        return "hierarchy-with-all";
    }

    /**
     * @see
     * org.pivot4j.ui.AbstractHtmlTableTestCase#configureRenderer(org.pivot4j.ui.table.TableRenderer)
     */
    @Override
    protected void configureRenderer(TableRenderer renderer) {
        super.configureRenderer(renderer);

        renderer.addAggregator(Axis.ROWS, AggregatorPosition.Hierarchy,
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
        assertThat("Not enough content rows.", rows.size(), is(equalTo(15)));

        // All Products
        assertCell(rows, 3, 0, 1, 2, "Total");
        assertCell(rows, 3, 1, 1, 1, "266,773");

        // All Products / Drinks
        assertCell(rows, 5, 0, 1, 2, "Total");
        assertCell(rows, 5, 1, 1, 1, "24,597");

        // All Products / Drinks
        assertCell(rows, 5, 0, 1, 2, "Total");
        assertCell(rows, 5, 1, 1, 1, "24,597");

        // All Products / Food
        assertCell(rows, 9, 0, 1, 2, "Total");
        assertCell(rows, 9, 1, 1, 1, "191,940");

        // All Products / Non-Consumable
        assertCell(rows, 13, 0, 1, 2, "Total");
        assertCell(rows, 13, 1, 1, 1, "50,236");

        // All Products / Non-Consumable
        assertCell(rows, 13, 0, 1, 2, "Total");
        assertCell(rows, 13, 1, 1, 1, "50,236");

        // Grand
        assertCell(rows, 14, 0, 1, 4, "Total");
        assertCell(rows, 14, 1, 1, 1, "266,773");
    }
}
