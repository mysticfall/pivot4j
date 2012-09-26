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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Hierarchy;

public class PlaceHierarchiesOnAxesImplIT extends
		AbstractTransformTestCase<PlaceHierarchiesOnAxesImpl> {

	private String initialQuery = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
			+ "Hierarchize(Union({[Product].[All Products]}, [Product].[All Products].Children)) ON ROWS FROM [Sales]";

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
	protected Class<PlaceHierarchiesOnAxesImpl> getType() {
		return PlaceHierarchiesOnAxesImpl.class;
	}

	@Test
	public void testTransform() {
		PlaceHierarchiesOnAxesImpl transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy promotionMedia = cube.getHierarchies().get("Promotion Media");
		Hierarchy product = cube.getHierarchies().get("Product");

		List<Hierarchy> hierarchies = new ArrayList<Hierarchy>(2);
		hierarchies.add(promotionMedia);
		hierarchies.add(product);

		transform.placeHierarchies(1, hierarchies, false);

		assertEquals(
				"Unexpected MDX query",
				"SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "CrossJoin({[Promotion Media].[All Media]}, {[Product].[All Products], [Product].[Drink], "
						+ "[Product].[Food], [Product].[Non-Consumable]}) ON ROWS FROM [Sales]",
				getPivotModel().getCurrentMdx());

		getPivotModel().getCellSet();
	}

	@Test
	public void testTransformExpandAllMembers() {
		PlaceHierarchiesOnAxesImpl transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy promotionMedia = cube.getHierarchies().get("Promotion Media");
		Hierarchy product = cube.getHierarchies().get("Product");

		List<Hierarchy> hierarchies = new ArrayList<Hierarchy>(2);
		hierarchies.add(promotionMedia);
		hierarchies.add(product);

		transform.placeHierarchies(1, hierarchies, true);

		assertEquals(
				"Unexpected MDX query after axes have been swapped",
				"SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "CrossJoin(Union({[Promotion Media].[All Media]}, [Promotion Media].[All Media].Children), "
						+ "{[Product].[All Products], [Product].[Drink], [Product].[Food], [Product].[Non-Consumable]}) ON ROWS FROM [Sales]",
				getPivotModel().getCurrentMdx());

		getPivotModel().getCellSet();
	}
}
