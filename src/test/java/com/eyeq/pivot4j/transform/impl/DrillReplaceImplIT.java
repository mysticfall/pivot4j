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

import java.util.List;

import org.junit.Test;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.transform.DrillReplace;

public class DrillReplaceImplIT extends AbstractTransformTestCase<DrillReplace> {

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
	protected Class<DrillReplace> getType() {
		return DrillReplace.class;
	}

	@Test
	public void testTransform() {
		DrillReplace transform = getTransform();

		CellSet cellSet = getPivotModel().getCellSet();
		CellSetAxis axis = cellSet.getAxes().get(1);

		Position position = axis.getPositions().get(0);
		List<Member> members = position.getMembers();

		Member allMedia = members.get(0);
		Hierarchy hierarchy = allMedia.getHierarchy();

		assertTrue(
				"Should be able to drill down on [Promotion Media].[All Media]",
				transform.canDrillDown(allMedia));
		assertFalse(
				"Drill up on [Promotion Media] hierarchy should not be possible",
				transform.canDrillUp(hierarchy));

		transform.drillDown(allMedia);

		assertEquals(
				"Unexpected MDX query after drill down on "
						+ allMedia.getUniqueName(),
				"SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "CrossJoin([Promotion Media].[All Media].Children, {[Product].[All Products]}) ON ROWS "
						+ "FROM [Sales]", getPivotModel().getCurrentMdx());

		cellSet = getPivotModel().getCellSet();

		axis = cellSet.getAxes().get(1);

		position = axis.getPositions().get(0);
		members = position.getMembers();

		Member bulkMail = members.get(0);

		assertFalse(
				"Drill down on [Promotion Media].[Bulk Mail] member should not be possible",
				transform.canDrillDown(bulkMail));
		assertTrue("Should be able to drill up on [Promotion Media] hierarchy",
				transform.canDrillUp(hierarchy));

		transform.drillUp(hierarchy);

		assertEquals(
				"Unexpected MDX query after drill down up "
						+ hierarchy.getUniqueName(), getInitialQuery(),
				getPivotModel().getCurrentMdx());

		cellSet = getPivotModel().getCellSet();
	}
}
