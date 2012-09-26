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

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.transform.DrillExpandMember;

public class DrillExpandMemberImplIT extends
		AbstractTransformTestCase<DrillExpandMember> {

	private String initialQuery = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
			+ "Hierarchize({([Time].[1997], [Promotion Media].[All Media]), ([Time].[1998], [Promotion Media].[All Media])}) ON ROWS FROM [Sales]";

	private String transformedQuery = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
			+ "Hierarchize(Union(Union(CrossJoin({[Time].[1997]}, {[Promotion Media].[All Media]}), CrossJoin({[Time].[1997]}, "
			+ "[Promotion Media].[All Media].Children)), Union(CrossJoin({[Time].[1998]}, {[Promotion Media].[All Media]}), "
			+ "CrossJoin({[Time].[1998]}, [Promotion Media].[All Media].Children)))) ON ROWS FROM [Sales]";

	/**
	 * @return the initialQuery
	 * @see com.eyeq.pivot4j.transform.impl.AbstractTransformTestCase#getInitialQuery()
	 */
	protected String getInitialQuery() {
		return initialQuery;
	}

	/**
	 * @return the transformedQuery
	 */
	protected String getTransformedQuery() {
		return transformedQuery;
	}

	/**
	 * @see com.eyeq.pivot4j.transform.impl.AbstractTransformTestCase#getType()
	 */
	@Override
	protected Class<DrillExpandMember> getType() {
		return DrillExpandMember.class;
	}

	@Test
	public void testTransform() {
		DrillExpandMember transform = getTransform();

		PivotModel model = getPivotModel();

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

		assertEquals("Unexpected MDX after collapse : ", getInitialQuery(),
				model.getCurrentMdx());

		assertFalse("[All Media] should not be collapsible after collapse",
				transform.canCollapse(allMedia));
		assertTrue("[All Media] should be expandable after collapse",
				transform.canExpand(allMedia));
	}
}
