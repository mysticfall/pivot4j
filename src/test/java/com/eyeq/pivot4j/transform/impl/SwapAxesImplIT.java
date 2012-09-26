/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.transform.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.eyeq.pivot4j.transform.SwapAxes;

public class SwapAxesImplIT extends AbstractTransformTestCase<SwapAxes> {

	private String initialQuery = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
			+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales]";

	/**
	 * @return the initialQuery
	 * @see com.eyeq.pivot4j.transform.impl.AbstractTransformTestCase#getInitialQuery()
	 */
	protected String getInitialQuery() {
		return initialQuery;
	}

	/**
	 * @see com.eyeq.pivot4j.transform.impl.AbstractTransformTestCase#getType()
	 */
	@Override
	protected Class<SwapAxes> getType() {
		return SwapAxes.class;
	}

	@Test
	public void testTransform() {
		SwapAxes transform = getTransform();

		assertFalse("Initial query axes are not swapped",
				transform.isSwapAxes());
		assertTrue("Should be able to swap axes on initial query",
				transform.canSwapAxes());

		transform.setSwapAxes(true);

		assertTrue("Query axes have been swapped", transform.isSwapAxes());

		assertEquals(
				"Unexpected MDX query after axes have been swapped",
				"SELECT {([Promotion Media].[All Media], [Product].[All Products])} ON COLUMNS, "
						+ "{[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON ROWS "
						+ "FROM [Sales]", getPivotModel().getCurrentMdx());

		getPivotModel().getCellSet();

		transform.setSwapAxes(false);

		assertEquals("Unexpected MDX query after axes have been restored",
				getInitialQuery(), getPivotModel().getCurrentMdx());

		getPivotModel().getCellSet();

		getPivotModel()
				.setMdx("SELECT {([Promotion Media].[All Media], [Product].[All Products])} ON COLUMNS FROM [Sales]");

		assertFalse("Single query axis cannot be swapped with itself",
				transform.canSwapAxes());
	}
}
