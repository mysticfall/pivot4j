/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.el;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.AbstractIntegrationTestCase;
import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.transform.ChangeSlicer;
import com.eyeq.pivot4j.transform.DrillExpandMember;
import com.eyeq.pivot4j.transform.PlaceHierarchiesOnAxes;
import com.eyeq.pivot4j.util.OlapUtils;

public class ExpressionEvaluatorIT extends AbstractIntegrationTestCase {

	@Test
	public void testSimpleExpression() {
		String query = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
				+ "{([Promotion Media].[All Media], "
				+ "[Product].[All Products])} ON ROWS FROM [Sales] WHERE $[year]";

		PivotModel model = getPivotModel();

		model.setMdx(query);
		model.initialize();

		model.getExpressionContext().put("year", "[Time].[1997]");

		ChangeSlicer transform = model.getTransform(ChangeSlicer.class);

		List<Member> members = transform.getSlicer();

		assertThat("Slicer axis should contain one member.", members.size(),
				is(1));
		assertThat("Wrong member found on the slicer axis.", members.get(0)
				.getUniqueName(), equalTo("[Time].[1997]"));
	}

	@Test
	public void testFreeMarkerExpression() {
		String query = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
				+ "{([Promotion Media].[All Media], "
				+ "$[cube.dimensions.get(\"Product\").defaultHierarchy.defaultMember.uniqueName]"
				+ ")} ON ROWS FROM [Sales] WHERE [Time].[1997]";

		PivotModel model = getPivotModel();

		model.setMdx(query);
		model.initialize();

		CellSet cellSet = model.getCellSet();

		List<Position> positions = cellSet.getAxes()
				.get(Axis.ROWS.axisOrdinal()).getPositions();
		assertThat("Row axis should contain one positions.", positions.size(),
				is(1));

		Position position = positions.get(0);

		assertThat("Row axis should contain two dimensions.", position
				.getMembers().size(), is(2));
		assertThat("Wrong member found on the row axis.", position.getMembers()
				.get(1).getUniqueName(), equalTo("[Product].[All Products]"));
	}

	@Test
	public void testPreserveParameterOnSlicerAfterDrillDown() {
		String query = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
				+ "{([Promotion Media].[All Media], "
				+ "[Product].[All Products])} ON ROWS FROM [Sales] WHERE $[year]";

		PivotModel model = getPivotModel();

		model.setMdx(query);
		model.initialize();

		model.getExpressionContext().put("year", "[Time].[1997]");

		ChangeSlicer slicerTransform = model.getTransform(ChangeSlicer.class);

		List<Member> members = slicerTransform.getSlicer();

		assertThat("Slicer axis should contain one member.", members.size(),
				is(1));

		assertThat("Wrong member found on the slicer axis.", members.get(0)
				.getUniqueName(), equalTo("[Time].[1997]"));

		Member member = OlapUtils.lookupMember(model.getCube(),
				"[Product].[All Products]");

		DrillExpandMember drillTransform = model
				.getTransform(DrillExpandMember.class);
		drillTransform.expand(member);

		model.getExpressionContext().put("year", "[Time].[1998]");
		model.getCellSet();

		members = slicerTransform.getSlicer();

		assertThat("Wrong member found on the slicer axis.", members.get(0)
				.getUniqueName(), equalTo("[Time].[1998]"));
	}

	@Test
	public void testPreserveParameterOnSlicerAfterAddingHierarchy() {
		String query = "SELECT {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ON COLUMNS, "
				+ "[Promotion Media].[All Media] ON ROWS FROM [Sales] WHERE $[year]";

		PivotModel model = getPivotModel();

		model.setMdx(query);
		model.initialize();

		model.getExpressionContext().put("year", "[Time].[1997]");

		ChangeSlicer slicerTransform = model.getTransform(ChangeSlicer.class);

		List<Member> members = slicerTransform.getSlicer();

		assertThat("Slicer axis should contain one member.", members.size(),
				is(1));

		assertThat("Wrong member found on the slicer axis.", members.get(0)
				.getUniqueName(), equalTo("[Time].[1997]"));

		PlaceHierarchiesOnAxes hierarchyTransform = model
				.getTransform(PlaceHierarchiesOnAxes.class);

		Hierarchy hierarchy = model.getCube().getHierarchies().get("Product");

		hierarchyTransform.addHierarchy(Axis.ROWS, hierarchy, true, -1);

		model.getExpressionContext().put("year", "[Time].[1998]");
		model.getCellSet();

		members = slicerTransform.getSlicer();

		assertThat("Wrong member found on the slicer axis.", members.get(0)
				.getUniqueName(), equalTo("[Time].[1998]"));
	}

	@Test
	public void testSimpleValueExpression() throws OlapException {
		String query = "WITH MEMBER [Measures].[Calc Cost] AS '[Measures].[Store Cost] * ${ratio}' "
				+ "SELECT {[Measures].[Calc Cost], [Measures].[Store Sales]} ON COLUMNS, "
				+ "{([Promotion Media].[All Media], [Product].[All Products])} ON ROWS FROM [Sales]";

		PivotModel model = getPivotModel();

		model.setMdx(query);
		model.initialize();

		model.getExpressionContext().put("ratio", "1.5");

		CellSet cellSet = model.getCellSet();

		assertThat("Wrong cell value returned(ratio = 1.5).", cellSet
				.getCell(0).getFormattedValue(), equalTo("338,440.85"));

		model.getExpressionContext().put("ratio", "2.5");
		model.refresh();

		cellSet = model.getCellSet();

		assertThat("Wrong cell value returned(ratio = 2.5).", cellSet
				.getCell(0).getFormattedValue(), equalTo("564,068.08"));
	}
}
