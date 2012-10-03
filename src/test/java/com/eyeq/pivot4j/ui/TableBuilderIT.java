/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.Test;

import com.eyeq.pivot4j.AbstractIntegrationTestCase;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.ui.html.HtmlTableBuilder;
import com.eyeq.pivot4j.ui.html.HtmlTableModel;

public class TableBuilderIT extends AbstractIntegrationTestCase {

	@Test
	public void testBuild() throws IOException {
		PivotModel model = getPivotModel();
//		model.setMdx("SELECT CrossJoin(Hierarchize(Union({[Gender].[All Gender]}, [Gender].[All Gender].Children)), "
//				+ "{[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]}) ON COLUMNS,"
//				+ "Hierarchize(Union(Union(CrossJoin({[Promotion Media].[All Media]}, {[Product].[All Products]}), "
//				+ "CrossJoin({[Promotion Media].[All Media]}, [Product].[All Products].Children)), "
//				+ "CrossJoin({[Promotion Media].[All Media]}, [Product].[Drink].Children))) ON ROWS FROM [Sales]");
//		model.setMdx("select Union(Union(Crossjoin({[Gender].[All Gender]}, {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]}), Crossjoin({[Gender].[F]}, {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]})), Crossjoin({[Gender].[M]}, {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]})) ON COLUMNS, Hierarchize(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Union(Crossjoin({[Promotion Media].[All Media]}, {([Product].[All Products], [Marital Status].[All Marital Status])}), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Drink], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Baked Goods], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Baking Goods], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, Union(Crossjoin({[Product].[Food].[Baking Goods].[Baking Goods]}, {[Marital Status].[All Marital Status]}), Crossjoin({[Product].[Food].[Baking Goods].[Baking Goods]}, [Marital Status].[All Marital Status].Children)))), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Baking Goods].[Jams and Jellies], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Breakfast Foods], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Canned Foods], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Canned Products], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Dairy], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Deli], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Eggs], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Frozen Foods], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Meat], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Produce], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Seafood], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Snack Foods], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Snacks], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Food].[Starchy Foods], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Non-Consumable], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Non-Consumable].[Carousel], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Non-Consumable].[Checkout], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Non-Consumable].[Health and Hygiene], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Non-Consumable].[Household], [Marital Status].[All Marital Status])})), Crossjoin({[Promotion Media].[All Media]}, {([Product].[Non-Consumable].[Periodicals], [Marital Status].[All Marital Status])}))) ON ROWS from [Sales]");
//		model.setMdx("select Union(Union(Crossjoin({[Gender].[All Gender]}, {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]}), Crossjoin({[Gender].[F]}, {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]})), Crossjoin({[Gender].[M]}, {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]})) ON COLUMNS, Crossjoin(Hierarchize(Union({[Promotion Media].[All Media]}, [Promotion Media].[All Media].Children)), {[Product].[All Products], [Product].[Drink], [Product].[Food], [Product].[Food].[Baked Goods], [Product].[Food].[Baking Goods], [Product].[Food].[Baking Goods].[Baking Goods], [Product].[Food].[Baking Goods].[Jams and Jellies], [Product].[Food].[Breakfast Foods], [Product].[Food].[Canned Foods], [Product].[Food].[Canned Products], [Product].[Food].[Dairy], [Product].[Food].[Deli], [Product].[Food].[Eggs], [Product].[Food].[Frozen Foods], [Product].[Food].[Meat], [Product].[Food].[Produce], [Product].[Food].[Seafood], [Product].[Food].[Snack Foods], [Product].[Food].[Snacks], [Product].[Food].[Starchy Foods], [Product].[Non-Consumable], [Product].[Non-Consumable].[Carousel], [Product].[Non-Consumable].[Checkout], [Product].[Non-Consumable].[Health and Hygiene], [Product].[Non-Consumable].[Household], [Product].[Non-Consumable].[Periodicals]}) ON ROWS from [Sales]");
		model.setMdx("select Hierarchize(Union(Union(Union(Union(Crossjoin({[Gender].[All Gender]}, {([Measures].[Unit Sales], [Product].[All Products])}), Crossjoin({[Gender].[All Gender]}, Union(Crossjoin({[Measures].[Store Cost]}, {[Product].[All Products]}), Crossjoin({[Measures].[Store Cost]}, [Product].[All Products].Children)))), Crossjoin({[Gender].[All Gender]}, {([Measures].[Store Sales], [Product].[All Products])})), Union(Union(Crossjoin({[Gender].[F]}, {([Measures].[Unit Sales], [Product].[All Products])}), Crossjoin({[Gender].[F]}, {([Measures].[Store Cost], [Product].[All Products])})), Crossjoin({[Gender].[F]}, Union(Union(Crossjoin({[Measures].[Store Sales]}, {[Product].[All Products]}), Crossjoin({[Measures].[Store Sales]}, [Product].[All Products].Children)), Crossjoin({[Measures].[Store Sales]}, [Product].[Non-Consumable].Children))))), Union(Union(Crossjoin({[Gender].[M]}, {([Measures].[Unit Sales], [Product].[All Products])}), Crossjoin({[Gender].[M]}, {([Measures].[Store Cost], [Product].[All Products])})), Crossjoin({[Gender].[M]}, {([Measures].[Store Sales], [Product].[All Products])})))) ON COLUMNS,  Hierarchize({[Promotion Media].[All Media]}) ON ROWS from [Sales]");

		model.initialize();

		HtmlTableBuilder builder = new HtmlTableBuilder();
		builder.setShowParentMembers(true);

		HtmlTableModel table = builder.build(model);
		table.setBorder(1);

		FileWriter writer = new FileWriter("/home/fender/임시/test.html");
		table.writeHtml(new PrintWriter(writer), 0);
		writer.flush();
		writer.close();
	}
}
