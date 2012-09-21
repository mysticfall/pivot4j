/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.eyeq.pivot4j.AbstractIntegrationTestCase;
import com.eyeq.pivot4j.NotInitializedException;
import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.PivotModel;

public class PivotModelImplIT extends AbstractIntegrationTestCase {

	@Test(expected = PivotException.class)
	public void testNoInitialMDX() {
		PivotModel model = getPivotModel();

		assertNotNull(model);

		assertFalse(model.isInitialized());
		model.initialize();
	}

	@Test(expected = NotInitializedException.class)
	public void testOperationBeforeInitialize() {
		PivotModel model = getPivotModel();

		assertNotNull(model);

		assertFalse(model.isInitialized());

		model.getCellSet();
	}

	@Test
	public void testInitialize() {
		PivotModel model = getPivotModel();

		assertNotNull(model);

		model.setMdx("select {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
				+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS from [Sales] where [Time].[1997]");

		assertFalse(model.isInitialized());
		model.initialize();

		assertTrue(model.isInitialized());
	}
}
