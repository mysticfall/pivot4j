/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eyeq.pivot4j.AbstractIntegrationTestCase;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.impl.PivotModelImpl;
import com.eyeq.pivot4j.sort.SortCriteria;

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
	 * @param testQuery
	 *            the testQuery to set
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

		assertNotNull("Bookmarked state should not be null", bookmark);

		PivotModel newModel = new PivotModelImpl(getDataSource());
		newModel.restoreState(bookmark);

		newModel.getCellSet();

		String newMdx = newModel.getCurrentMdx();
		if (newMdx != null) {
			// Currently the parser treats every number as double value.
			// It's inevitable now and does not impact the result.
			newMdx = newMdx.replaceAll("3\\.0", "3");
		}

		assertEquals("MDX has been changed after the state restoration", mdx,
				newMdx);
		assertTrue(
				"Property 'sorting' has been changed after the state restoration",
				newModel.isSorting());
		assertEquals(
				"Property 'topBottomCount' has been changed after the state restoration",
				3, newModel.getTopBottomCount());
		assertEquals(
				"Property 'sortMode' has been changed after the state restoration",
				SortCriteria.BOTTOMCOUNT, newModel.getSortCriteria());
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

		assertEquals("MDX has been changed after the state restoration", mdx,
				newMdx);
		assertTrue(
				"Property 'sorting' has been changed after the state restoration",
				newModel.isSorting());
		assertEquals(
				"Property 'topBottomCount' has been changed after the state restoration",
				3, newModel.getTopBottomCount());
		assertEquals(
				"Property 'sortMode' has been changed after the state restoration",
				SortCriteria.BOTTOMCOUNT, newModel.getSortCriteria());
	}
}
