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
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.olap4j.Axis;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Hierarchy;

import com.eyeq.pivot4j.transform.PlaceHierarchiesOnAxes;

public class PlaceHierarchiesOnAxesImplIT extends
		AbstractTransformTestCase<PlaceHierarchiesOnAxes> {

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
	protected Class<PlaceHierarchiesOnAxes> getType() {
		return PlaceHierarchiesOnAxes.class;
	}

	@Test
	public void testTransform() {
		PlaceHierarchiesOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy promotionMedia = cube.getHierarchies().get("Promotion Media");
		Hierarchy product = cube.getHierarchies().get("Product");

		List<Hierarchy> hierarchies = new ArrayList<Hierarchy>(2);
		hierarchies.add(promotionMedia);
		hierarchies.add(product);

		transform.placeHierarchies(Axis.ROWS, hierarchies, false);

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
		PlaceHierarchiesOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy promotionMedia = cube.getHierarchies().get("Promotion Media");
		Hierarchy product = cube.getHierarchies().get("Product");

		List<Hierarchy> hierarchies = new ArrayList<Hierarchy>(2);
		hierarchies.add(promotionMedia);
		hierarchies.add(product);

		transform.placeHierarchies(Axis.ROWS, hierarchies, true);

		assertEquals(
				"Unexpected MDX query after axes have been swapped",
				"SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "CrossJoin(Union({[Promotion Media].[All Media]}, [Promotion Media].[All Media].Children), "
						+ "{[Product].[All Products], [Product].[Drink], [Product].[Food], [Product].[Non-Consumable]}) ON ROWS FROM [Sales]",
				getPivotModel().getCurrentMdx());

		getPivotModel().getCellSet();
	}

	@Test
	public void testFindVisibleHierarchies() {
		String query = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
				+ "CrossJoin(Union({[Promotion Media].[All Media]}, [Promotion Media].[All Media].Children), "
				+ "{[Product].[All Products], [Product].[Drink], [Product].[Food], [Product].[Non-Consumable]}) ON ROWS FROM [Sales]";

		getPivotModel().setMdx(query);

		PlaceHierarchiesOnAxes transform = getTransform();

		List<Hierarchy> columnHierarhies = transform
				.findVisibleHierarchies(Axis.COLUMNS);
		List<Hierarchy> rowHierarhies = transform
				.findVisibleHierarchies(Axis.ROWS);

		assertNotNull("Hierarchy list on the column axis should not be null",
				columnHierarhies);
		assertEquals("Number of hierarchy on the column axis should be 1", 1,
				columnHierarhies.size());

		assertEquals("Wrong name for the first hierarchy on the column axis",
				"Measures", columnHierarhies.get(0).getName());

		assertNotNull("Hierarchy list on the row axis should not be null",
				rowHierarhies);
		assertEquals("Number of hierarchies on the column axis should be 2", 2,
				rowHierarhies.size());

		assertEquals("Wrong name for the first hierarchy on the row axis",
				"Promotion Media", rowHierarhies.get(0).getName());
		assertEquals("Wrong name for the seconde hierarchy on the row axis",
				"Product", rowHierarhies.get(1).getName());
	}
}
