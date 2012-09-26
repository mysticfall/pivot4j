/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.transform.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.olap4j.OlapException;
import org.olap4j.mdx.IdentifierNode;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.transform.ChangeSlicer;

public class ChangeSlicerImplIT extends AbstractTransformTestCase<ChangeSlicer> {

	private String initialQuery = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
			+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales] WHERE [Time].[1997]";

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
	protected Class<ChangeSlicer> getType() {
		return ChangeSlicer.class;
	}

	@Test
	public void testGetSlicers() {
		ChangeSlicer transform = getTransform();

		List<Member> members = transform.getSlicer();

		assertNotNull("Failed to retrieve slicer members", members);
		assertFalse("Failed to retrieve slicer members", members.isEmpty());
	}

	@Test
	public void testSetSlicerWithSingleMember() throws OlapException {
		ChangeSlicer transform = getTransform();

		List<Member> members = new ArrayList<Member>(1);

		Cube cube = getPivotModel().getCube();

		Member year1998 = cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Time].[1998]").getSegmentList());

		assertNotNull("Cannot look up member [Time].[1998]", year1998);

		members.add(year1998);

		transform.setSlicer(members);

		getPivotModel().getCellSet();

		assertEquals(
				"",
				"SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS "
						+ "FROM [Sales] WHERE [Time].[1998]", getPivotModel()
						.getCurrentMdx());
	}

	@Test
	public void testSetSlicerWithSet() throws OlapException {
		ChangeSlicer transform = getTransform();

		List<Member> members = new ArrayList<Member>(2);

		Cube cube = getPivotModel().getCube();

		Member year1997 = cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Time].[1997]").getSegmentList());
		Member year1998 = cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Time].[1998]").getSegmentList());

		assertNotNull("Cannot look up member [Time].[1997]", year1997);
		assertNotNull("Cannot look up member [Time].[1998]", year1998);

		members.add(year1997);
		members.add(year1998);

		transform.setSlicer(members);

		getPivotModel().getCellSet();

		assertEquals(
				"",
				"SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales] "
						+ "WHERE {[Time].[1997], [Time].[1998]}",
				getPivotModel().getCurrentMdx());
	}

	@Test
	public void testSetSlicerWithTuple() throws OlapException {
		ChangeSlicer transform = getTransform();

		List<Member> members = new ArrayList<Member>(2);

		Cube cube = getPivotModel().getCube();

		Member year1997 = cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Time].[1997]").getSegmentList());
		assertNotNull("Cannot look up member [Time].[1997]", year1997);

		Member genderM = cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Gender].[M]").getSegmentList());

		assertNotNull("Cannot look up member [Gender].[M]", genderM);

		members.add(year1997);
		members.add(genderM);

		transform.setSlicer(members);

		getPivotModel().getCellSet();

		assertEquals(
				"",
				"SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales] "
						+ "WHERE Crossjoin({[Time].[1997]}, {[Gender].[M]})",
				getPivotModel().getCurrentMdx());
	}
}
