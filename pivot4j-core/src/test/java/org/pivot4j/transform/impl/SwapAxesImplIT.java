/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.transform.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.pivot4j.transform.SwapAxes;

public class SwapAxesImplIT extends AbstractTransformTestCase<SwapAxes> {

    private String initialQuery = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
            + "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales]";

    /**
     * @return the initialQuery
     * @see
     * org.pivot4j.transform.impl.AbstractTransformTestCase#getInitialQuery()
     */
    protected String getInitialQuery() {
        return initialQuery;
    }

    /**
     * @see org.pivot4j.transform.impl.AbstractTransformTestCase#getType()
     */
    @Override
    protected Class<SwapAxes> getType() {
        return SwapAxes.class;
    }

    @Test
    public void testTransform() {
        SwapAxes transform = getTransform();

        assertThat("Initial query axes are not swapped",
                transform.isSwapAxes(), is(false));
        assertThat("Should be able to swap axes on initial query",
                transform.canSwapAxes(), is(true));

        transform.setSwapAxes(true);

        assertThat("Query axes have been swapped", transform.isSwapAxes(),
                is(true));

        assertThat(
                "Unexpected MDX query after axes have been swapped",
                getPivotModel().getCurrentMdx(),
                is(equalTo("SELECT {([Promotion Media].[All Media], [Product].[All Products])} ON COLUMNS, "
                        + "{[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON ROWS "
                        + "FROM [Sales]")));

        getPivotModel().getCellSet();

        transform.setSwapAxes(false);

        assertThat("Unexpected MDX query after axes have been restored",
                getPivotModel().getCurrentMdx(), is(equalTo(getInitialQuery())));

        getPivotModel().getCellSet();

        getPivotModel()
                .setMdx("SELECT {([Promotion Media].[All Media], [Product].[All Products])} ON COLUMNS FROM [Sales]");

        assertThat("Single query axis cannot be swapped with itself",
                transform.canSwapAxes(), is(false));
    }
}
