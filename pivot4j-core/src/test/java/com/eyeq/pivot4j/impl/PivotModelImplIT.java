/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;

import com.eyeq.pivot4j.AbstractIntegrationTestCase;
import com.eyeq.pivot4j.NotInitializedException;
import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.PivotModel;

public class PivotModelImplIT extends AbstractIntegrationTestCase {

	private String testQuery = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
			+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales] WHERE [Time].[1997]";

	/**
	 * @return the testQuery
	 */
	protected String getTestQuery() {
		return testQuery;
	}

	@Test
	public void testInitialize() {
		PivotModel model = getPivotModel();
		model.setMdx(getTestQuery());

		assertThat("Model is already initialized.", model.isInitialized(),
				is(false));

		model.initialize();

		assertThat("Model is not initialized.", model.isInitialized(), is(true));
	}

	@Test(expected = PivotException.class)
	public void testInitializeWithNoInitialMdx() {
		PivotModel model = getPivotModel();
		model.initialize();
	}

	@Test
	public void testDestroy() {
		PivotModel model = getPivotModel();
		model.setMdx(getTestQuery());
		model.initialize();

		assertThat("Model is not initialized.", model.isInitialized(), is(true));

		model.destroy();

		assertThat("Model is not destroyed.", model.isInitialized(), is(false));
	}

	@Test
	public void testGetCellSet() {
		PivotModel model = getPivotModel();
		model.setMdx(getTestQuery());
		model.initialize();

		CellSet cellSet = model.getCellSet();

		assertThat("CellSet is null.", cellSet, is(notNullValue()));

		List<CellSetAxis> axes = cellSet.getAxes();

		assertThat("CELL axes list is null.", axes, is(notNullValue()));
		assertThat("Invalid cell axes size.", axes.size(), is(equalTo(2)));
	}

	@Test
	public void testGetMdx() {
		PivotModel model = getPivotModel();
		model.setMdx(getTestQuery());
		model.initialize();

		assertThat("MDX has been modified unexpectedly.", model.getMdx(),
				is(equalTo(getTestQuery())));
	}

	@Test
	public void testGetCurrentMdx() {
		PivotModel model = getPivotModel();
		model.setMdx(getTestQuery());
		model.initialize();

		String currentMdx = model.getCurrentMdx();

		assertThat("MDX has been modified unexpectedly.", currentMdx,
				is(equalTo(getTestQuery())));
	}

	@Test(expected = NotInitializedException.class)
	public void testGetCellSetBeforeInitialize() {
		PivotModel model = getPivotModel();

		assertThat("Model is already initialized.", model.isInitialized(),
				is(false));

		model.getCellSet();
	}
}
