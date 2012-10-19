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
	public void testPlaceHierarchies() {
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
	public void testExpandAllMembers() {
		PlaceHierarchiesOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy promotionMedia = cube.getHierarchies().get("Promotion Media");
		Hierarchy product = cube.getHierarchies().get("Product");

		List<Hierarchy> hierarchies = new ArrayList<Hierarchy>(2);
		hierarchies.add(promotionMedia);
		hierarchies.add(product);

		transform.placeHierarchies(Axis.ROWS, hierarchies, true);

		assertEquals(
				"Unexpected MDX query after set hierarchies on axis",
				"SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "CrossJoin(Union({[Promotion Media].[All Media]}, [Promotion Media].[All Media].Children), "
						+ "{[Product].[All Products], [Product].[Drink], [Product].[Food], [Product].[Non-Consumable]}) ON ROWS FROM [Sales]",
				getPivotModel().getCurrentMdx());

		getPivotModel().getCellSet();
	}

	@Test
	public void testAddHierarchyAtIndexMinusOne() {
		PlaceHierarchiesOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy gender = cube.getHierarchies().get("Gender");

		transform.addHierarchy(Axis.ROWS, gender, false, -1);

		assertEquals(
				"Unexpected MDX query after adding a new hierarchy",
				"SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS"
						+ ", CrossJoin({[Product].[All Products], [Product].[Drink], [Product].[Food], [Product].[Non-Consumable]}, "
						+ "{[Gender].[All Gender]}) ON ROWS FROM [Sales]",
				getPivotModel().getCurrentMdx());

		getPivotModel().getCellSet();
	}

	@Test
	public void testAddHierarchyAtIndexTwo() {
		PlaceHierarchiesOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy gender = cube.getHierarchies().get("Gender");

		transform.addHierarchy(Axis.ROWS, gender, false, 2);

		assertEquals(
				"Unexpected MDX query after adding a new hierarchy",
				"SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS"
						+ ", CrossJoin({[Product].[All Products], [Product].[Drink], [Product].[Food], [Product].[Non-Consumable]}, "
						+ "{[Gender].[All Gender]}) ON ROWS FROM [Sales]",
				getPivotModel().getCurrentMdx());

		getPivotModel().getCellSet();
	}

	@Test
	public void testAddHierarchyAtIndexZero() {
		PlaceHierarchiesOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy gender = cube.getHierarchies().get("Gender");

		transform.addHierarchy(Axis.ROWS, gender, false, 0);

		assertEquals(
				"Unexpected MDX query after adding a new hierarchy",
				"SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "CrossJoin({[Gender].[All Gender]}, {[Product].[All Products], [Product].[Drink], [Product].[Food], "
						+ "[Product].[Non-Consumable]}) ON ROWS FROM [Sales]",
				getPivotModel().getCurrentMdx());

		getPivotModel().getCellSet();
	}

	@Test
	public void testMoveHierarchy() {
		String query = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
				+ "CrossJoin(Union({[Promotion Media].[All Media]}, [Promotion Media].[All Media].Children), "
				+ "{[Product].[All Products], [Product].[Drink], [Product].[Food], [Product].[Non-Consumable]}) ON ROWS FROM [Sales]";

		getPivotModel().setMdx(query);

		PlaceHierarchiesOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy product = cube.getHierarchies().get("Product");

		transform.moveHierarchy(Axis.ROWS, product, 0);

		assertEquals(
				"SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "CrossJoin({[Product].[All Products], [Product].[Drink], [Product].[Food], [Product].[Non-Consumable]}, "
						+ "{[Promotion Media].[All Media], [Promotion Media].[Bulk Mail], [Promotion Media].[Cash Register Handout], "
						+ "[Promotion Media].[Daily Paper], [Promotion Media].[Daily Paper, Radio], [Promotion Media].[Daily Paper, Radio, TV], "
						+ "[Promotion Media].[In-Store Coupon], [Promotion Media].[No Media], [Promotion Media].[Product Attachment], "
						+ "[Promotion Media].[Radio], [Promotion Media].[Street Handout], [Promotion Media].[Sunday Paper], "
						+ "[Promotion Media].[Sunday Paper, Radio], [Promotion Media].[Sunday Paper, Radio, TV], "
						+ "[Promotion Media].[TV]}) ON ROWS FROM [Sales]",
				getPivotModel().getCurrentMdx());

		getPivotModel().getCellSet();
	}

	@Test
	public void testRemoveHierarchy() {
		String query = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
				+ "CrossJoin(Union({[Promotion Media].[All Media]}, [Promotion Media].[All Media].Children), "
				+ "{[Product].[All Products], [Product].[Drink], [Product].[Food], [Product].[Non-Consumable]}) ON ROWS FROM [Sales]";

		getPivotModel().setMdx(query);

		PlaceHierarchiesOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy product = cube.getHierarchies().get("Product");

		transform.removeHierarchy(Axis.ROWS, product);

		assertEquals(
				"SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "{[Promotion Media].[All Media], [Promotion Media].[Bulk Mail], [Promotion Media].[Cash Register Handout], "
						+ "[Promotion Media].[Daily Paper], [Promotion Media].[Daily Paper, Radio], [Promotion Media].[Daily Paper, Radio, TV], "
						+ "[Promotion Media].[In-Store Coupon], [Promotion Media].[No Media], [Promotion Media].[Product Attachment], "
						+ "[Promotion Media].[Radio], [Promotion Media].[Street Handout], [Promotion Media].[Sunday Paper], "
						+ "[Promotion Media].[Sunday Paper, Radio], [Promotion Media].[Sunday Paper, Radio, TV], "
						+ "[Promotion Media].[TV]} ON ROWS FROM [Sales]",
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
