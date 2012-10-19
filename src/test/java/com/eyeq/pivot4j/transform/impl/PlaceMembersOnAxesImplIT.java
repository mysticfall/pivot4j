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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.olap4j.Axis;
import org.olap4j.OlapException;
import org.olap4j.mdx.IdentifierNode;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.transform.PlaceMembersOnAxes;

public class PlaceMembersOnAxesImplIT extends
		AbstractTransformTestCase<PlaceMembersOnAxes> {

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
	protected Class<PlaceMembersOnAxes> getType() {
		return PlaceMembersOnAxes.class;
	}

	@Test
	public void testFindVisibleMembers() throws OlapException {
		PlaceMembersOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy promotionMedia = cube.getHierarchies().get("Promotion Media");

		List<Member> mediaMembers = transform
				.findVisibleMembers(promotionMedia);

		assertNotNull("[Promotion Media].[All Media] member should be visible",
				mediaMembers);
		assertFalse("[Promotion Media].[All Media] member should be visible",
				mediaMembers.isEmpty());
		assertEquals("Only [Promotion Media].[All Media] member is visible", 1,
				mediaMembers.size());
	}

	@Test
	public void testPlaceMembers() throws OlapException {
		PlaceMembersOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy promotionMedia = cube.getHierarchies().get("Promotion Media");
		Hierarchy product = cube.getHierarchies().get("Product");

		List<Member> members = new ArrayList<Member>();

		Member allMedia = promotionMedia.getDefaultMember();
		Member allProducts = product.getDefaultMember();

		members.add(allMedia);
		members.add(allMedia.getChildMembers().get("Bulk Mail"));
		members.add(allMedia.getChildMembers().get("Daily Paper"));

		members.add(allProducts.getChildMembers().get("Food"));
		members.add(allProducts.getChildMembers().get("Drink"));

		transform.placeMembers(Axis.ROWS, members);

		assertEquals(
				"Unexpected MDX query",
				"SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "CrossJoin({[Promotion Media].[All Media], [Promotion Media].[Bulk Mail], [Promotion Media].[Daily Paper]}, "
						+ "{[Product].[Food], [Product].[Drink]}) ON ROWS FROM [Sales]",
				getPivotModel().getCurrentMdx());

		getPivotModel().getCellSet();
	}

	@Test
	public void testAddMemberAtIndexMinusOne() throws OlapException {
		PlaceMembersOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Member member = cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Measures].[Profit]").getSegmentList());

		transform.addMember(member, -1);

		assertEquals(
				"Unexpected MDX query",
				"SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales], [Measures].[Profit]} ON COLUMNS, "
						+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales]",
				getPivotModel().getCurrentMdx());

		getPivotModel().getCellSet();
	}

	@Test
	public void testAddMemberAtIndexZero() throws OlapException {
		PlaceMembersOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Member member = cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Measures].[Profit]").getSegmentList());

		transform.addMember(member, 0);

		assertEquals(
				"Unexpected MDX query",
				"SELECT {[Measures].[Profit], [Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales]",
				getPivotModel().getCurrentMdx());

		getPivotModel().getCellSet();
	}

	@Test
	public void testAddMemberAtIndexOne() throws OlapException {
		PlaceMembersOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Member member = cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Measures].[Profit]").getSegmentList());

		transform.addMember(member, 1);

		assertEquals(
				"Unexpected MDX query",
				"SELECT {[Measures].[Unit Sales], [Measures].[Profit], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales]",
				getPivotModel().getCurrentMdx());

		getPivotModel().getCellSet();
	}

	@Test
	public void testAddMembers() throws OlapException {
		PlaceMembersOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		List<Member> members = new ArrayList<Member>(2);
		members.add(cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Measures].[Profit]").getSegmentList()));
		members.add(cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Measures].[Sales Count]").getSegmentList()));

		transform.addMembers(cube.getHierarchies().get("Measures"), members);

		assertEquals(
				"Unexpected MDX query",
				"SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales], "
						+ "[Measures].[Profit], [Measures].[Sales Count]} ON COLUMNS, "
						+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales]",
				getPivotModel().getCurrentMdx());

		getPivotModel().getCellSet();
	}

	@Test
	public void testMoveMember() throws OlapException {
		PlaceMembersOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Member member = cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Measures].[Unit Sales]").getSegmentList());

		transform.moveMember(member, 2);

		assertEquals(
				"Unexpected MDX query",
				"SELECT {[Measures].[Store Cost], [Measures].[Unit Sales], [Measures].[Store Sales]} ON COLUMNS, "
						+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales]",
				getPivotModel().getCurrentMdx());

		getPivotModel().getCellSet();
	}

	@Test
	public void testRemoveMember() throws OlapException {
		PlaceMembersOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Member member = cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Measures].[Unit Sales]").getSegmentList());

		transform.removeMember(member);

		assertEquals(
				"Unexpected MDX query",
				"SELECT {[Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales]",
				getPivotModel().getCurrentMdx());

		getPivotModel().getCellSet();
	}

	@Test
	public void testRemoveMembers() throws OlapException {
		PlaceMembersOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		List<Member> members = new ArrayList<Member>(2);
		members.add(cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Measures].[Unit Sales]").getSegmentList()));
		members.add(cube.lookupMember(IdentifierNode.parseIdentifier(
				"[Measures].[Store Sales]").getSegmentList()));

		transform.removeMembers(cube.getHierarchies().get("Measures"), members);

		assertEquals(
				"Unexpected MDX query",
				"SELECT {[Measures].[Store Cost]} ON COLUMNS, "
						+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales]",
				getPivotModel().getCurrentMdx());

		getPivotModel().getCellSet();
	}
}
