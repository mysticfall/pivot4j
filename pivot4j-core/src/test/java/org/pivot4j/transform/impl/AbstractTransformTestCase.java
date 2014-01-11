/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.transform.impl;

import static org.junit.Assert.assertNotNull;

import org.pivot4j.AbstractIntegrationTestCase;
import org.pivot4j.PivotModel;
import org.pivot4j.transform.Transform;

public abstract class AbstractTransformTestCase<T extends Transform> extends
		AbstractIntegrationTestCase {

	private T transform;

	/**
	 * @see org.pivot4j.AbstractIntegrationTestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();

		PivotModel model = getPivotModel();
		model.setMdx(getInitialQuery());
		model.initialize();

		this.transform = model.getTransform(getType());

		assertNotNull("No suitable transform found for " + getType(), transform);
	}

	/**
	 * @throws Exception 
	 * @see org.pivot4j.AbstractIntegrationTestCase#tearDown()
	 */
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		this.transform = null;
	}

	protected abstract Class<T> getType();

	protected abstract String getInitialQuery();

	protected T getTransform() {
		return transform;
	}
}
