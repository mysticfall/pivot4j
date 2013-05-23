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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.olap4j.Axis;
import org.olap4j.OlapException;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.transform.PlaceLevelsOnAxes;

public class PlaceLevelsOnAxesImplIT extends
		AbstractTransformTestCase<PlaceLevelsOnAxes> {

	private String initialQuery = "select {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS,"
			+ " Hierarchize(Union(Union(Union(Crossjoin({[Promotion Media].[All Media]}, {[Product].[All Products]}), "
			+ "Crossjoin({[Promotion Media].[All Media]}, [Product].[All Products].Children)), Crossjoin({[Promotion Media].[All Media]}, "
			+ "[Product].[Drink].Children)), Crossjoin({[Promotion Media].[All Media]}, [Product].[Drink].[Beverages].Children))) ON ROWS "
			+ "from [Sales] where [Time].[1997]";

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
	protected Class<PlaceLevelsOnAxes> getType() {
		return PlaceLevelsOnAxes.class;
	}

	@Test
	public void testFindVisibleLevels() throws OlapException {
		PlaceLevelsOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy promotionMedia = cube.getHierarchies().get("Promotion Media");
		Hierarchy product = cube.getHierarchies().get("Product");

		List<Level> firstLevels = transform.findVisibleLevels(promotionMedia);
		assertThat(
				"The number of visible levels in [Promotion Media] hierarchy should be 1",
				firstLevels.size(), is(equalTo(1)));
		assertThat(
				"The first level of the hierarchy should be [Promotion Media].[(All)]",
				promotionMedia.getLevels().get(0).getUniqueName(),
				is(equalTo(firstLevels.get(0).getUniqueName())));

		List<Level> allLevels = transform.findVisibleLevels(Axis.ROWS);

		assertThat("Visible levels should not be empty", allLevels.isEmpty(),
				is(false));
		assertThat("The number of visible levels should be 5",
				allLevels.size(), is(equalTo(5)));
		assertThat(
				"The first level of the axis should be [Promotion Media].[(All)]",
				promotionMedia.getLevels().get(0).getUniqueName(),
				is(equalTo(allLevels.get(0).getUniqueName())));

		assertThat("The second level of the axis should be [Product].[(All)]",
				product.getLevels().get(0).getUniqueName(),
				is(equalTo(allLevels.get(1).getUniqueName())));
		assertThat(
				"The third level of the axis should be [Product].[Product Family]",
				product.getLevels().get(1).getUniqueName(),
				is(equalTo(allLevels.get(2).getUniqueName())));
		assertThat(
				"The fourth level of the axis should be [Product].[Product Department]",
				product.getLevels().get(2).getUniqueName(),
				is(equalTo(allLevels.get(3).getUniqueName())));
		assertThat(
				"The fourth level of the axis should be [Product].[Product Category]",
				product.getLevels().get(3).getUniqueName(),
				is(equalTo(allLevels.get(4).getUniqueName())));
	}

	@Test
	public void testAddLevelOnExisiingHierarchy() throws OlapException {
		PlaceLevelsOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy promotionMedia = cube.getHierarchies().get("Promotion Media");
		Level level = promotionMedia.getLevels().get(1);

		transform.addLevel(Axis.ROWS, level, -1);

		assertThat(
				"Unexpected MDX query",
				getPivotModel().getCurrentMdx(),
				is(equalTo("SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "CrossJoin({[Promotion Media].[All Media], [Promotion Media].[Bulk Mail], [Promotion Media].[Cash Register Handout], "
						+ "[Promotion Media].[Daily Paper], [Promotion Media].[Daily Paper, Radio], "
						+ "[Promotion Media].[Daily Paper, Radio, TV], [Promotion Media].[In-Store Coupon], [Promotion Media].[No Media], "
						+ "[Promotion Media].[Product Attachment], [Promotion Media].[Radio], [Promotion Media].[Street Handout], "
						+ "[Promotion Media].[Sunday Paper], [Promotion Media].[Sunday Paper, Radio], "
						+ "[Promotion Media].[Sunday Paper, Radio, TV], [Promotion Media].[TV]}, {[Product].[All Products], "
						+ "[Product].[Drink], [Product].[Drink].[Alcoholic Beverages], [Product].[Drink].[Beverages], "
						+ "[Product].[Drink].[Beverages].[Carbonated Beverages], [Product].[Drink].[Beverages].[Drinks], "
						+ "[Product].[Drink].[Beverages].[Hot Beverages], [Product].[Drink].[Beverages].[Pure Juice Beverages], "
						+ "[Product].[Drink].[Dairy], [Product].[Food], [Product].[Non-Consumable]}) ON ROWS "
						+ "FROM [Sales] WHERE [Time].[1997]")));
	}

	@Test
	public void testAddLevelAtTheBeginning() throws OlapException {
		PlaceLevelsOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy gender = cube.getHierarchies().get("Gender");
		Level level = gender.getLevels().get(1);

		transform.addLevel(Axis.ROWS, level, 0);

		assertThat(
				"Unexpected MDX query",
				getPivotModel().getCurrentMdx(),
				is(equalTo("SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "CrossJoin({[Gender].[F], [Gender].[M]}, CrossJoin({[Promotion Media].[All Media]}, {[Product].[All Products], "
						+ "[Product].[Drink], [Product].[Drink].[Alcoholic Beverages], [Product].[Drink].[Beverages], "
						+ "[Product].[Drink].[Beverages].[Carbonated Beverages], [Product].[Drink].[Beverages].[Drinks], "
						+ "[Product].[Drink].[Beverages].[Hot Beverages], [Product].[Drink].[Beverages].[Pure Juice Beverages], "
						+ "[Product].[Drink].[Dairy], [Product].[Food], [Product].[Non-Consumable]})) ON ROWS "
						+ "FROM [Sales] WHERE [Time].[1997]")));
	}

	@Test
	public void testPlaceLevels() throws OlapException {
		PlaceLevelsOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy gender = cube.getHierarchies().get("Gender");
		Hierarchy store = cube.getHierarchies().get("Store");

		List<Level> levels = new ArrayList<Level>();
		levels.add(gender.getLevels().get(0));
		levels.add(store.getLevels().get(1));

		transform.placeLevels(Axis.ROWS, levels);

		assertThat(
				"Unexpected MDX query",
				getPivotModel().getCurrentMdx(),
				is(equalTo("SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "CrossJoin({[Gender].[All Gender]}, {[Store].[Canada], [Store].[Mexico], [Store].[USA]}) ON ROWS "
						+ "FROM [Sales] WHERE [Time].[1997]")));
	}

	@Test
	public void testAddLevelAtTheMiddle() throws OlapException {
		PlaceLevelsOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy gender = cube.getHierarchies().get("Gender");
		Level level = gender.getLevels().get(1);

		transform.addLevel(Axis.ROWS, level, 1);

		assertThat(
				"Unexpected MDX query",
				getPivotModel().getCurrentMdx(),
				is(equalTo("SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "CrossJoin({[Promotion Media].[All Media]}, CrossJoin({[Gender].[F], [Gender].[M]}, "
						+ "{[Product].[All Products], [Product].[Drink], [Product].[Drink].[Alcoholic Beverages], "
						+ "[Product].[Drink].[Beverages], [Product].[Drink].[Beverages].[Carbonated Beverages], "
						+ "[Product].[Drink].[Beverages].[Drinks], [Product].[Drink].[Beverages].[Hot Beverages], "
						+ "[Product].[Drink].[Beverages].[Pure Juice Beverages], [Product].[Drink].[Dairy], "
						+ "[Product].[Food], [Product].[Non-Consumable]})) ON ROWS FROM [Sales] WHERE [Time].[1997]")));
	}

	@Test
	public void testAddLevelAtTheEnd() throws OlapException {
		PlaceLevelsOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy gender = cube.getHierarchies().get("Gender");
		Level level = gender.getLevels().get(1);

		transform.addLevel(Axis.ROWS, level, 2);

		assertThat(
				"Unexpected MDX query",
				getPivotModel().getCurrentMdx(),
				is(equalTo("SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "CrossJoin({[Promotion Media].[All Media]}, CrossJoin({[Product].[All Products], [Product].[Drink], "
						+ "[Product].[Drink].[Alcoholic Beverages], [Product].[Drink].[Beverages], [Product].[Drink].[Beverages].[Carbonated Beverages], "
						+ "[Product].[Drink].[Beverages].[Drinks], [Product].[Drink].[Beverages].[Hot Beverages], "
						+ "[Product].[Drink].[Beverages].[Pure Juice Beverages], [Product].[Drink].[Dairy], [Product].[Food], "
						+ "[Product].[Non-Consumable]}, {[Gender].[F], [Gender].[M]})) ON ROWS FROM [Sales] WHERE [Time].[1997]")));
	}

	@Test
	public void testRemoveLevel() throws OlapException {
		PlaceLevelsOnAxes transform = getTransform();

		Cube cube = getPivotModel().getCube();

		Hierarchy product = cube.getHierarchies().get("Product");

		Level level = product.getLevels().get("Product Department");

		transform.removeLevel(Axis.ROWS, level);

		assertThat(
				"Unexpected MDX query",
				getPivotModel().getCurrentMdx(),
				is(equalTo("SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
						+ "CrossJoin({[Promotion Media].[All Media]}, {[Product].[All Products], [Product].[Drink], "
						+ "[Product].[Drink].[Beverages].[Carbonated Beverages], [Product].[Drink].[Beverages].[Drinks], "
						+ "[Product].[Drink].[Beverages].[Hot Beverages], [Product].[Drink].[Beverages].[Pure Juice Beverages], "
						+ "[Product].[Food], [Product].[Non-Consumable]}) ON ROWS FROM [Sales] WHERE [Time].[1997]")));
	}

	@Test
	public void testRemoveLevelFromCrossJoinedAxis() throws OlapException {
		PlaceLevelsOnAxes transform = getTransform();

		PivotModel model = getPivotModel();
		model.setMdx("SELECT {[Measures].[Unit Sales]} ON COLUMNS, Union(Union({([Product].[All Products], [Gender].[All Gender])}, "
				+ "Union(Union(CrossJoin({[Product].[Food]}, {[Gender].[All Gender]}), CrossJoin({[Product].[Food]}, {[Gender].[F]})), "
				+ "CrossJoin({[Product].[Food]}, {[Gender].[M]}))), Union(Union(CrossJoin({[Product].[Non-Consumable]}, "
				+ "{[Gender].[All Gender]}), CrossJoin({[Product].[Non-Consumable]}, {[Gender].[F]})), "
				+ "CrossJoin({[Product].[Non-Consumable]}, {[Gender].[M]}))) ON ROWS FROM [Sales]");

		Cube cube = getPivotModel().getCube();

		Hierarchy product = cube.getHierarchies().get("Product");

		Level level = product.getLevels().get("(All)");

		transform.removeLevel(Axis.ROWS, level);

		assertThat(
				"Unexpected MDX query",
				getPivotModel().getCurrentMdx(),
				is(equalTo("SELECT {[Measures].[Unit Sales]} ON COLUMNS, CrossJoin({[Product].[Food], [Product].[Non-Consumable]}, "
						+ "{[Gender].[All Gender], [Gender].[F], [Gender].[M]}) ON ROWS FROM [Sales]")));
	}
}
