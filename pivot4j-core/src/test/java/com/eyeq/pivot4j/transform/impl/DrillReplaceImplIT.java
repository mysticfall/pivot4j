/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.transform.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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

		assertThat(
				"Should be able to drill down on [Promotion Media].[All Media]",
				transform.canDrillDown(allMedia), is(true));
		assertThat(
				"Drill up on [Promotion Media] hierarchy should not be possible",
				transform.canDrillUp(hierarchy), is(false));

		transform.drillDown(allMedia);

		assertThat(
				"Unexpected MDX query after drill down on "
						+ allMedia.getUniqueName(),
				getPivotModel().getCurrentMdx(),
				is(equalTo("SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "CrossJoin([Promotion Media].[All Media].Children, {[Product].[All Products]}) ON ROWS "
						+ "FROM [Sales]")));

		cellSet = getPivotModel().getCellSet();

		axis = cellSet.getAxes().get(1);

		position = axis.getPositions().get(0);
		members = position.getMembers();

		Member bulkMail = members.get(0);

		assertThat(
				"Drill down on [Promotion Media].[Bulk Mail] member should not be possible",
				transform.canDrillDown(bulkMail), is(false));
		assertThat("Should be able to drill up on [Promotion Media] hierarchy",
				transform.canDrillUp(hierarchy), is(true));

		transform.drillUp(hierarchy);

		assertThat(
				"Unexpected MDX query after drill down up "
						+ hierarchy.getUniqueName(), getPivotModel()
						.getCurrentMdx(), is(equalTo(getInitialQuery())));

		cellSet = getPivotModel().getCellSet();
	}
}
