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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

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

		assertThat("Failed to retrieve slicer members", members,
				is(notNullValue()));
		assertThat("Failed to retrieve slicer members", members.isEmpty(),
				is(false));
	}

	@Test
	public void testSetSlicerWithSingleMember() throws OlapException {
		ChangeSlicer transform = getTransform();

		List<Member> members = new ArrayList<Member>(1);

		Cube cube = getPivotModel().getCube();

		Member year1998 = cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Time].[1998]").getSegmentList());

		assertThat("Cannot look up member [Time].[1998]", year1998,
				is(notNullValue()));

		members.add(year1998);

		transform.setSlicer(members);

		getPivotModel().getCellSet();

		assertThat(
				getPivotModel().getCurrentMdx(),
				is(equalTo("SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS "
						+ "FROM [Sales] WHERE [Time].[1998]")));
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

		assertThat("Cannot look up member [Time].[1997]", year1997,
				is(notNullValue()));
		assertThat("Cannot look up member [Time].[1998]", year1998,
				is(notNullValue()));

		members.add(year1997);
		members.add(year1998);

		transform.setSlicer(members);

		getPivotModel().getCellSet();

		assertThat(
				getPivotModel().getCurrentMdx(),
				is(equalTo("SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales] "
						+ "WHERE {[Time].[1997], [Time].[1998]}")));
	}

	@Test
	public void testSetSlicerWithTuple() throws OlapException {
		ChangeSlicer transform = getTransform();

		List<Member> members = new ArrayList<Member>(2);

		Cube cube = getPivotModel().getCube();

		Member year1997 = cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Time].[1997]").getSegmentList());
		assertThat("Cannot look up member [Time].[1997]", year1997,
				is(notNullValue()));

		Member year1998 = cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Time].[1998]").getSegmentList());
		assertThat("Cannot look up member [Time].[1998]", year1998,
				is(notNullValue()));

		Member genderM = cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Gender].[M]").getSegmentList());

		assertThat("Cannot look up member [Gender].[M]", genderM,
				is(notNullValue()));

		members.add(year1997);
		members.add(year1998);
		members.add(genderM);

		transform.setSlicer(members);

		getPivotModel().getCellSet();

		assertThat(
				getPivotModel().getCurrentMdx(),
				is(equalTo("SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales] "
						+ "WHERE CrossJoin({[Time].[1997], [Time].[1998]}, [Gender].[M])")));
	}

	@Test
	public void testSetSlicerWithHierarchy() throws OlapException {
		ChangeSlicer transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Member year1997 = cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Time].[1997]").getSegmentList());
		assertThat("Cannot look up member [Time].[1997]", year1997,
				is(notNullValue()));

		Member genderM = cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Gender].[M]").getSegmentList());

		assertThat("Cannot look up member [Gender].[M]", genderM,
				is(notNullValue()));

		List<Member> timeMembers = new ArrayList<Member>(1);
		timeMembers.add(year1997);

		List<Member> genderMembers = new ArrayList<Member>(1);
		genderMembers.add(genderM);

		transform.setSlicer(year1997.getHierarchy(), timeMembers);
		transform.setSlicer(genderM.getHierarchy(), genderMembers);

		getPivotModel().getCellSet();

		assertThat(
				getPivotModel().getCurrentMdx(),
				is(equalTo("SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales] "
						+ "WHERE CrossJoin([Time].[1997], [Gender].[M])")));
	}
}
