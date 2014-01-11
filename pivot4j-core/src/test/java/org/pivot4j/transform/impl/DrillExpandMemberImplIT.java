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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.Position;
import org.olap4j.metadata.Member;
import org.pivot4j.PivotModel;
import org.pivot4j.transform.DrillExpandMember;

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
	 * @see org.pivot4j.transform.impl.AbstractTransformTestCase#getInitialQuery()
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
	 * @see org.pivot4j.transform.impl.AbstractTransformTestCase#getType()
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
		assertThat("Unable to execute MDX query : " + getInitialQuery(),
				cellSet, is(notNullValue()));

		CellSetAxis axis = cellSet.getAxes().get(1);
		Position position = axis.getPositions().get(0);

		Member allMedia = position.getMembers().get(1);

		assertThat(
				"Unexpected member at drill position : "
						+ allMedia.getCaption(), allMedia.getCaption(),
				is(equalTo("All Media")));

		assertThat("[All Media] should not be collapsible initially",
				transform.canCollapse(allMedia), is(false));
		assertThat("[All Media] should be expandable initially",
				transform.canExpand(allMedia), is(true));

		transform.expand(allMedia);

		assertThat("Unexpected MDX after drill down : ", model.getCurrentMdx(),
				is(equalTo(getTransformedQuery())));

		assertThat("[All Media] should be collapsible after drill down",
				transform.canCollapse(allMedia), is(true));
		assertThat("[All Media] should not be expandable after drill down",
				transform.canExpand(allMedia), is(false));

		transform.collapse(allMedia);

		assertThat("Unexpected MDX after collapse : ", model.getCurrentMdx(),
				is(equalTo(getInitialQuery())));

		assertThat("[All Media] should not be collapsible after collapse",
				transform.canCollapse(allMedia), is(false));
		assertThat("[All Media] should be expandable after collapse",
				transform.canExpand(allMedia), is(true));
	}
}
