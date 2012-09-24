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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.AbstractIntegrationTestCase;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.transform.DrillExpandMember;

public class DrillExpandMemberImplIT extends AbstractIntegrationTestCase {

	private String initialQuery = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
			+ "Hierarchize({([Time].[1997], [Promotion Media].[All Media]), ([Time].[1998], [Promotion Media].[All Media])}) ON ROWS FROM [Sales]";

	private String transformedQuery = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
			+ "Hierarchize(Union(Union(CrossJoin({[Time].[1997]}, {[Promotion Media].[All Media]}), CrossJoin({[Time].[1997]}, "
			+ "[Promotion Media].[All Media].Children)), Union(CrossJoin({[Time].[1998]}, {[Promotion Media].[All Media]}), "
			+ "CrossJoin({[Time].[1998]}, [Promotion Media].[All Media].Children)))) ON ROWS FROM [Sales]";

	/**
	 * @return the initialQuery
	 */
	public String getInitialQuery() {
		return initialQuery;
	}

	/**
	 * @param initialQuery
	 *            the initialQuery to set
	 */
	public void setInitialQuery(String testQuery) {
		this.initialQuery = testQuery;
	}

	/**
	 * @return the transformedQuery
	 */
	public String getTransformedQuery() {
		return transformedQuery;
	}

	/**
	 * @param transformedQuery
	 *            the transformedQuery to set
	 */
	public void setTransformedQuery(String transformedQuery) {
		this.transformedQuery = transformedQuery;
	}

	@Test
	public void testTransform() {
		PivotModel model = getPivotModel();
		model.setMdx(getInitialQuery());
		model.initialize();

		DrillExpandMember transform = model
				.getTransform(DrillExpandMember.class);

		assertNotNull("No suitable transform found for "
				+ DrillExpandMember.class, transform);

		CellSet cellSet = model.getCellSet();
		assertNotNull("Unable to execute MDX query : " + getInitialQuery(),
				cellSet);

		CellSetAxis axis = cellSet.getAxes().get(1);
		Position position = axis.getPositions().get(0);

		Member allMedia = position.getMembers().get(1);

		assertEquals(
				"Unexpected member at drill position : "
						+ allMedia.getCaption(), "All Media",
				allMedia.getCaption());

		assertFalse("[All Media] should not be collapsible initially",
				transform.canCollapse(allMedia));
		assertTrue("[All Media] should be expandable initially",
				transform.canExpand(allMedia));

		transform.expand(allMedia);

		assertEquals("Unexpected MDX after drill down : ",
				getTransformedQuery(), model.getCurrentMdx());

		assertTrue("[All Media] should be collapsible after drill down",
				transform.canCollapse(allMedia));
		assertFalse("[All Media] should not be expandable after drill down",
				transform.canExpand(allMedia));

		transform.collapse(allMedia);

		assertEquals("Unexpected MDX after collapse : ",
				getInitialQuery(), model.getCurrentMdx());

		assertFalse("[All Media] should not be collapsible after collapse",
				transform.canCollapse(allMedia));
		assertTrue("[All Media] should be expandable after collapse",
				transform.canExpand(allMedia));
	}
}
