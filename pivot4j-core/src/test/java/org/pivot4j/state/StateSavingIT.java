/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.state;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.pivot4j.AbstractIntegrationTestCase;
import org.pivot4j.PivotModel;
import org.pivot4j.impl.PivotModelImpl;
import org.pivot4j.sort.SortCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateSavingIT extends AbstractIntegrationTestCase {

    private String testQuery = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
            + "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales] WHERE [Time].[1997]";

    /**
     * @return the testQuery
     */
    public String getTestQuery() {
        return testQuery;
    }

    /**
     * @param testQuery the testQuery to set
     */
    public void setTestQuery(String testQuery) {
        this.testQuery = testQuery;
    }

    @Test
    public void testBookmarkModelState() {
        PivotModel model = getPivotModel();
        model.setMdx(getTestQuery());
        model.initialize();

        model.setSorting(true);
        model.setTopBottomCount(3);
        model.setSortCriteria(SortCriteria.BOTTOMCOUNT);

        CellSet cellSet = model.getCellSet();
        CellSetAxis axis = cellSet.getAxes().get(Axis.COLUMNS.axisOrdinal());

        model.sort(axis, axis.getPositions().get(0));

        String mdx = model.getCurrentMdx();

        Serializable bookmark = model.saveState();

        assertThat("Bookmarked state should not be null", bookmark,
                is(notNullValue()));

        PivotModel newModel = new PivotModelImpl(getDataSource());
        newModel.restoreState(bookmark);

        newModel.getCellSet();

        String newMdx = newModel.getCurrentMdx();
        if (newMdx != null) {
            // Currently the parser treats every number as double value.
            // It's inevitable now and does not impact the result.
            newMdx = newMdx.replaceAll("3\\.0", "3");
        }

        assertThat("MDX has been changed after the state restoration", newMdx,
                is(equalTo(mdx)));
        assertThat(
                "RenderProperty 'sorting' has been changed after the state restoration",
                newModel.isSorting(), is(true));
        assertThat(
                "RenderProperty 'topBottomCount' has been changed after the state restoration",
                newModel.getTopBottomCount(), is(equalTo(3)));
        assertThat(
                "RenderProperty 'sortMode' has been changed after the state restoration",
                newModel.getSortCriteria(),
                is(equalTo(SortCriteria.BOTTOMCOUNT)));
    }

    @Test
    public void testSaveModelSettings() throws ConfigurationException,
            IOException {
        PivotModel model = getPivotModel();
        model.setMdx(getTestQuery());
        model.initialize();

        model.setSorting(true);
        model.setTopBottomCount(3);
        model.setSortCriteria(SortCriteria.BOTTOMCOUNT);

        CellSet cellSet = model.getCellSet();
        CellSetAxis axis = cellSet.getAxes().get(Axis.COLUMNS.axisOrdinal());

        model.sort(axis, axis.getPositions().get(0));

        String mdx = model.getCurrentMdx();

        XMLConfiguration configuration = new XMLConfiguration();
        configuration.setDelimiterParsingDisabled(true);

        model.saveSettings(configuration);

        Logger logger = LoggerFactory.getLogger(getClass());
        if (logger.isDebugEnabled()) {
            StringWriter writer = new StringWriter();
            configuration.save(writer);
            writer.flush();
            writer.close();

            logger.debug("Loading report content :"
                    + System.getProperty("line.separator"));
            logger.debug(writer.getBuffer().toString());
        }

        PivotModel newModel = new PivotModelImpl(getDataSource());
        newModel.restoreSettings(configuration);

        newModel.getCellSet();

        String newMdx = newModel.getCurrentMdx();
        if (newMdx != null) {
            // Currently the parser treats every number as double value.
            // It's inevitable now and does not impact the result.
            newMdx = newMdx.replaceAll("3\\.0", "3");
        }

        assertThat("MDX has been changed after the state restoration", newMdx,
                is(equalTo(mdx)));
        assertThat(
                "RenderProperty 'sorting' has been changed after the state restoration",
                newModel.isSorting(), is(true));
        assertThat(
                "RenderProperty 'topBottomCount' has been changed after the state restoration",
                newModel.getTopBottomCount(), is(equalTo(3)));
        assertThat(
                "RenderProperty 'sortMode' has been changed after the state restoration",
                newModel.getSortCriteria(),
                is(equalTo(SortCriteria.BOTTOMCOUNT)));
    }
}
