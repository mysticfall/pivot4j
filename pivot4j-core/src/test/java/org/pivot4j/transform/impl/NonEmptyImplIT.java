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
import org.pivot4j.transform.NonEmpty;

public class NonEmptyImplIT extends AbstractTransformTestCase<NonEmpty> {

	private String initialQuery = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
			+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales]";

	/**
	 * @return the initialQuery
	 * @see org.pivot4j.transform.impl.AbstractTransformTestCase#getInitialQuery()
	 */
	protected String getInitialQuery() {
		return initialQuery;
	}

	/**
	 * @see org.pivot4j.transform.impl.AbstractTransformTestCase#getType()
	 */
	@Override
	protected Class<NonEmpty> getType() {
		return NonEmpty.class;
	}

	@Test
	public void testTransform() {
		NonEmpty transform = getTransform();

		assertThat("Initial query does not include NON EMPTY statement",
				transform.isNonEmpty(), is(false));

		transform.setNonEmpty(true);

		assertThat("Query does contain NON EMPTY statement",
				transform.isNonEmpty(), is(true));
		assertThat(
				"Unexpected MDX query after set NON EMPTY statement",
				getPivotModel().getCurrentMdx(),
				is(equalTo("SELECT NON EMPTY {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "NON EMPTY {([Promotion Media].[All Media], [Product].[All Products])} ON ROWS "
						+ "FROM [Sales]")));

		getPivotModel().getCellSet();

		transform.setNonEmpty(false);

		assertThat("Query does not contain NON EMPTY statement",
				transform.isNonEmpty(), is(false));

		assertThat("Unexpected MDX query after removing NON EMPTY statement",
				getPivotModel().getCurrentMdx(), is(equalTo(getInitialQuery())));
	}
}
