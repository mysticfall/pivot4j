/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.mdx;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.olap4j.Axis;
import org.pivot4j.mdx.CompoundId;
import org.pivot4j.mdx.Exp;
import org.pivot4j.mdx.ExpressionParameter;
import org.pivot4j.mdx.Formula;
import org.pivot4j.mdx.FunCall;
import org.pivot4j.mdx.MdxParser;
import org.pivot4j.mdx.MdxStatement;
import org.pivot4j.mdx.QueryAxis;
import org.pivot4j.mdx.SapVariable;
import org.pivot4j.mdx.Syntax;
import org.pivot4j.mdx.ValueParameter;
import org.pivot4j.mdx.impl.MdxParserImpl;

public class MdxParserTest {

	/**
	 * @param mdxQuery
	 * @return
	 * @throws Exception
	 */
	protected MdxStatement parseQuery(String mdxQuery) throws Exception {
		MdxParser parser = new MdxParserImpl();

		return parser.parse(mdxQuery);
	}

	@Test
	public void testParseEmptyAxis() throws Exception {
		String mdx = "SELECT FROM DummyCube";

		MdxStatement query = parseQuery(mdx);

		assertThat("Failed to parse : " + mdx, query, is(notNullValue()));
		assertThat("Axes should not be null.", query.getAxes(),
				is(notNullValue()));
		assertThat("Number of axes should be 0.", query.getAxes().isEmpty(),
				is(true));
	}

	@Test
	public void testGenerateEmptyAxis() throws Exception {
		String mdx = "SELECT FROM DummyCube";

		MdxStatement query = new MdxStatement();
		query.setCube(new CompoundId("DummyCube"));

		assertThat("Unexpected MDX query.", query.toMdx(), is(equalTo(mdx)));
	}

	@Test
	public void testParseSingleAxis() throws Exception {
		String mdx = "SELECT [AAA] ON COLUMNS FROM DummyCube";

		MdxStatement query = parseQuery(mdx);

		assertThat("Failed to parse : " + mdx, query, is(notNullValue()));
		assertThat("Axes should not be null.", query.getAxes(),
				is(notNullValue()));

		assertThat("Number of axes should be 1.", query.getAxes().size(),
				is(equalTo(1)));

		Exp columnExp = query.getAxes().get(0).getExp();

		assertThat("Exp object for columns axis is null.", columnExp,
				is(notNullValue()));

		assertThat("Exp object should be instance of CompoundId.", columnExp,
				is(instanceOf(CompoundId.class)));

		mdx = "SELECT [AAA] ON ROWS FROM DummyCube";

		Exp rowExp = query.getAxes().get(0).getExp();

		assertThat("Exp object for rows axis is null.", rowExp,
				is(notNullValue()));

		assertThat("Exp object should be instance of CompoundId.", rowExp,
				is(instanceOf(CompoundId.class)));
	}

	@Test
	public void testGenerateSingleAxis() throws Exception {
		String mdx = "SELECT [AAA] ON COLUMNS FROM [DummyCube]";

		MdxStatement query = new MdxStatement();

		QueryAxis axis = new QueryAxis(Axis.COLUMNS, new CompoundId("[AAA]"));
		query.setAxis(axis);
		query.setCube(new CompoundId("[DummyCube]"));

		assertThat("Unexpected MDX query.", query.toMdx(), is(equalTo(mdx)));
	}

	@Test
	public void testParseKeyIdentifier() throws Exception {
		String mdx = "SELECT [AAA].&[BBB] ON COLUMNS, [CCC].&[DDD].[EEE] ON ROWS FROM DummyCube";

		MdxStatement query = parseQuery(mdx);

		assertThat("Failed to parse : " + mdx, query, is(notNullValue()));
		assertThat("Axes should not be null.", query.getAxes(),
				is(notNullValue()));

		assertThat("Number of axes should be 2.", query.getAxes().size(),
				is(equalTo(2)));

		Exp columnExp = query.getAxes().get(0).getExp();

		assertThat("Exp object for columns axis is null.", columnExp,
				is(notNullValue()));

		assertThat("Exp object should be instance of CompoundId.", columnExp,
				is(instanceOf(CompoundId.class)));

		CompoundId columnId = (CompoundId) columnExp;
		assertThat("Wrong number of name parts on column axis member.",
				columnId.getNames().size(), is(equalTo(2)));

		assertThat("First name part has wrong identifier.", columnId.getNames()
				.get(0).getName(), is(equalTo("[AAA]")));
		assertThat("Second name part has wrong identifier.", columnId
				.getNames().get(1).getName(), is(equalTo("[BBB]")));
		assertThat("First name part cannot be a key identifier.", columnId
				.getNames().get(0).isKey(), is(false));
		assertThat("Second name part is a key identifier.", columnId.getNames()
				.get(1).isKey(), is(true));

		Exp rowExp = query.getAxes().get(1).getExp();

		assertThat("Exp object for rows axis is null.", rowExp,
				is(notNullValue()));

		assertThat("Exp object should be instance of CompoundId.", rowExp,
				is(instanceOf(CompoundId.class)));

		CompoundId rowId = (CompoundId) rowExp;
		assertThat("Wrong number of name parts on row axis member.", rowId
				.getNames().size(), is(equalTo(3)));

		assertThat("First name part has wrong identifier.", rowId.getNames()
				.get(0).getName(), is(equalTo("[CCC]")));
		assertThat("Second name part has wrong identifier.", rowId.getNames()
				.get(1).getName(), is(equalTo("[DDD]")));
		assertThat("Third name part has wrong identifier.", rowId.getNames()
				.get(2).getName(), is(equalTo("[EEE]")));
		assertThat("First name part cannot be a key identifier.", rowId
				.getNames().get(0).isKey(), is(false));
		assertThat("Second name part is a key identifier.", rowId.getNames()
				.get(1).isKey(), is(true));
		assertThat("Third name part is not a key identifier.", rowId.getNames()
				.get(2).isKey(), is(false));
	}

	@Test
	public void testGenerateKeyIdentifier() throws Exception {
		String mdx = "SELECT [AAA].&[BBB] ON COLUMNS, [CCC].&[DDD].[EEE] ON ROWS FROM DummyCube";

		MdxStatement query = new MdxStatement();

		CompoundId columnId = new CompoundId().append("[AAA]").append("[BBB]",
				true);
		CompoundId rowId = new CompoundId().append("[CCC]")
				.append("[DDD]", true).append("[EEE]");

		query.setAxis(new QueryAxis(Axis.COLUMNS, columnId));
		query.setAxis(new QueryAxis(Axis.ROWS, rowId));

		query.setCube(new CompoundId("DummyCube"));

		assertThat("Unexpected MDX query.", query.toMdx(), is(equalTo(mdx)));
	}

	@Test
	public void testParseSapVariables() throws Exception {
		String mdx = "SELECT [Measures].members ON COLUMNS, NON EMPTY [ODB_CUST].members ON ROWS "
				+ "FROM [ODBOSCEN1/MKTBRANCH] SAP VARIABLES [ODBBRANC] INCLUDING [ODB_BRANC].[CHEM]";

		MdxStatement query = parseQuery(mdx);

		assertThat("Failed to parse : " + mdx, query, is(notNullValue()));
		assertThat("SAP varaible list should not be null.",
				query.getSapVariables(), is(notNullValue()));

		assertThat("Number of SAP variables should be 1.", query
				.getSapVariables().size(), is(equalTo(1)));

		SapVariable variable = query.getSapVariables().get(0);

		assertThat("SAP varaible should not be null.", variable,
				is(notNullValue()));

		CompoundId name = variable.getName();
		assertThat("Name of the SAP varaible should not be null.", name,
				is(notNullValue()));

		List<SapVariable.Value> values = variable.getValues();
		assertThat("VALUE list of the SAP varaible should not be null.",
				values, is(notNullValue()));
		assertThat("Number of the SAP variable value should be 1.",
				values.size(), is(equalTo(1)));

		SapVariable.Value value = values.get(0);
		assertThat("The SAP variable is not an interval value.",
				value.isInterval(), is(false));
		assertThat("The SAP variable has specified INCLUDING option.",
				value.isIncluding(), is(true));

		assertThat("Option value of the SAP varaible should not be null.",
				value.getValue(), is(notNullValue()));

		assertThat(
				"Option value of the SAP variable should be instance of CompoundId.",
				value.getValue(), is(instanceOf(CompoundId.class)));

		assertThat("Incorrect option value of the SAP variable.", value
				.getValue().toMdx(), is(equalTo("[ODB_BRANC].[CHEM]")));
	}

	@Test
	public void testParseOpeningBracket() throws Exception {
		String mdx = "SELECT [AA[BB].[CC] ON COLUMNS, [DD].[[AAB] ON ROWS FROM DummyCube";

		MdxStatement query = parseQuery(mdx);

		assertThat("Failed to parse : " + mdx, query, is(notNullValue()));
		assertThat("Axes should not be null.", query.getAxes(),
				is(notNullValue()));

		assertThat("Number of axes should be 2.", query.getAxes().size(),
				is(equalTo(2)));

		Exp columnExp = query.getAxes().get(0).getExp();

		assertThat("Exp object for columns axis is null.", columnExp,
				is(notNullValue()));

		assertThat("Exp object should be instance of CompoundId.", columnExp,
				is(instanceOf(CompoundId.class)));

		CompoundId columnMember = (CompoundId) columnExp;
		String[] columnNames = columnMember.toStringArray();

		assertThat("Segments of column member id is null.", columnNames,
				is(notNullValue()));
		assertThat("Column member id should contain 2 segment parts.",
				columnNames.length, is(equalTo(2)));
		assertThat("First segment of the column member element is incorrect.",
				columnNames[0], is(equalTo("[AA[BB]")));
		assertThat("Second segment of the column member element is incorrect.",
				columnNames[1], is(equalTo("[CC]")));

		Exp rowExp = query.getAxes().get(1).getExp();

		assertThat("Exp object for rows axis is null.", rowExp,
				is(notNullValue()));

		assertThat("Exp object should be instance of CompoundId.", rowExp,
				is(instanceOf(CompoundId.class)));

		CompoundId rowMember = (CompoundId) rowExp;
		String[] rowNames = rowMember.toStringArray();

		assertThat("Segments of row member id is null.", rowNames,
				is(notNullValue()));
		assertThat("Row member id should contain 2 segment parts.",
				rowNames.length, is(equalTo(2)));
		assertThat("First segment of the row member element is incorrect.",
				rowNames[0], is(equalTo("[DD]")));
		assertThat("Second segment of the row member element is incorrect.",
				rowNames[1], is(equalTo("[[AAB]")));
	}

	@Test
	public void testParseBracketEscape() throws Exception {
		String mdx = "SELECT [AA[BB]]].[CC] ON COLUMNS, [DD].[AA]]B] ON ROWS FROM DummyCube";

		MdxStatement query = parseQuery(mdx);

		assertThat("Failed to parse : " + mdx, query, is(notNullValue()));
		assertThat("Axes should not be null.", query.getAxes(),
				is(notNullValue()));

		assertThat("Number of axes should be 2.", query.getAxes().size(),
				is(equalTo(2)));

		Exp columnExp = query.getAxes().get(0).getExp();

		assertThat("Exp object for columns axis is null.", columnExp,
				is(notNullValue()));

		assertThat("Exp object should be instance of CompoundId.", columnExp,
				is(instanceOf(CompoundId.class)));

		CompoundId columnMember = (CompoundId) columnExp;
		String[] columnNames = columnMember.toStringArray();

		assertThat("Segments of column member id is null.", columnNames,
				is(notNullValue()));
		assertThat("Column member id should contain 2 segment parts.",
				columnNames.length, is(equalTo(2)));
		assertThat("First segment of the column member element is incorrect.",
				columnNames[0], is(equalTo("[AA[BB]]]")));
		assertThat("Second segment of the column member element is incorrect.",
				columnNames[1], is(equalTo("[CC]")));

		Exp rowExp = query.getAxes().get(1).getExp();

		assertThat("Exp object for rows axis is null.", rowExp,
				is(notNullValue()));

		assertThat("Exp object should be instance of CompoundId.", rowExp,
				is(instanceOf(CompoundId.class)));

		CompoundId rowMember = (CompoundId) rowExp;
		String[] rowNames = rowMember.toStringArray();

		assertThat("Segments of row member id is null.", rowNames,
				is(notNullValue()));
		assertThat("Row member id should contain 2 segment parts.",
				rowNames.length, is(equalTo(2)));
		assertThat("First segment of the row member element is incorrect.",
				rowNames[0], is(equalTo("[DD]")));
		assertThat("Second segment of the row member element is incorrect.",
				rowNames[1], is(equalTo("[AA]]B]")));
	}

	@Test
	public void testGenerateBracketEscape() throws Exception {
		String mdx = "SELECT [AA[BB]]].[CC] ON COLUMNS, [DD].[AA]]B] ON ROWS FROM DummyCube";

		MdxStatement query = new MdxStatement();

		CompoundId columnId = new CompoundId().append("[AA[BB]]]").append(
				"[CC]");
		CompoundId rowId = new CompoundId().append("[DD]").append("[AA]]B]");

		query.setAxis(new QueryAxis(Axis.COLUMNS, columnId));
		query.setAxis(new QueryAxis(Axis.ROWS, rowId));

		query.setCube(new CompoundId("DummyCube"));

		assertThat("Unexpected MDX query.", query.toMdx(), is(equalTo(mdx)));
	}

	@Test
	public void testParseWithMember() throws Exception {
		String mdx = "WITH MEMBER [Measures].[Special Discount] AS [Measures].[Discount Amount] * 1.5 "
				+ "MEMBER [Measures].[Premium Discount] AS [Measures].[Discount Amount] * 2.0 "
				+ "SELECT [Measures].[Special Discount] on COLUMNS, NON EMPTY [Product].[Product].MEMBERS ON ROWS "
				+ "FROM [Adventure Works] WHERE [Product].[Category].[Bikes]";

		MdxStatement query = parseQuery(mdx);
		assertThat("Failed to parse : " + mdx, query, is(notNullValue()));

		List<Formula> formulas = query.getFormulas();

		assertThat("Formula list not be null.", formulas, is(notNullValue()));
		assertThat("Number of formula should be 2.", formulas.size(),
				is(equalTo(2)));

		Formula formula = formulas.get(0);

		assertThat("Wrong formula type.", formula.getType(),
				is(equalTo(Formula.Type.MEMBER)));
		assertThat("Wrong calculated member name.", formula.getName().toMdx(),
				is(equalTo("[Measures].[Special Discount]")));

		formula = formulas.get(1);

		Exp arg = formula.getExp();

		assertThat("Wrong argument type.", arg, is(instanceOf(FunCall.class)));

		FunCall func = (FunCall) arg;

		assertThat("Wrong function type.", func.getType(),
				is(equalTo(Syntax.Infix)));
		assertThat("Wrong number of function arguments.",
				func.getArgs().size(), is(equalTo(2)));
	}

	@Test
	public void testParseMemberExpression() throws Exception {
		String mdx = "SELECT [Measures].[Store Sales] ON COLUMNS, [Product].[All Products] ON ROWS FROM [Sales] "
				+ "WHERE $[parameter]";

		MdxStatement query = parseQuery(mdx);
		assertThat("Failed to parse : " + mdx, query, is(notNullValue()));

		ExpressionParameter parameter = (ExpressionParameter) query.getSlicer();
		assertThat("Slicer exp is null.", parameter, is(notNullValue()));

		assertThat("Wrong member expression returned.",
				parameter.getExpression(), is(equalTo("parameter")));
	}

	@Test
	public void testParseMemberExpressionWithCalculation() throws Exception {
		String mdx = "SELECT [Measures].[Store Sales] ON COLUMNS, [Product].[All Products] ON ROWS FROM [Sales] "
				+ "WHERE $['aaa' + 'bbb']";

		MdxStatement query = parseQuery(mdx);
		assertThat("Failed to parse : " + mdx, query, is(notNullValue()));

		ExpressionParameter parameter = (ExpressionParameter) query.getSlicer();
		assertThat("Slicer exp is null.", parameter, is(notNullValue()));

		assertThat("Wrong member expression returned.",
				parameter.getExpression(), is(equalTo("'aaa' + 'bbb'")));
	}

	@Test
	public void testParseMemberExpressionInWithMember() throws Exception {
		String mdx = "WITH MEMBER [EXPR_MEMBER] AS '$[parameter]' SELECT [Measures].[Store Sales] ON COLUMNS, "
				+ "[Product].[All Products] ON ROWS FROM [Sales]";

		MdxStatement query = parseQuery(mdx);

		assertThat("Failed to parse : " + mdx, query, is(notNullValue()));

		List<Formula> formulas = query.getFormulas();

		assertThat("Formula list is null.", formulas, is(notNullValue()));
		assertThat("Wrong number of formula returned.", formulas.size(),
				is(equalTo(1)));

		Formula formula = formulas.get(0);

		Exp exp = formula.getExp();

		assertThat("Formula expression is null.", formulas, is(notNullValue()));
		assertThat("Parameter expression type is wrong.", exp,
				is(instanceOf(ExpressionParameter.class)));

		ExpressionParameter parameter = (ExpressionParameter) exp;

		assertThat("Wrong parameter expression.", parameter.getExpression(),
				is(equalTo("parameter")));
	}

	@Test
	public void testParseMemberExpressionInWithSet() throws Exception {
		String mdx = "WITH SET [EXPR_SET] AS {$[parameter1], [AAA].[BBB], $[parameter2]} "
				+ "SELECT [Measures].[Store Sales] ON COLUMNS, "
				+ "[Product].[All Products] ON ROWS FROM [Sales]";

		MdxStatement query = parseQuery(mdx);
		assertThat("Failed to parse : " + mdx, query, is(notNullValue()));

		List<Formula> formulas = query.getFormulas();

		assertThat("Formula list is null.", formulas, is(notNullValue()));
		assertThat("Wrong number of formula returned.", formulas.size(),
				is(equalTo(1)));

		Formula formula = formulas.get(0);

		Exp exp = formula.getExp();

		assertThat("Formula expression is null.", formulas, is(notNullValue()));
		assertThat("Parameter expression type is wrong.", exp,
				is(instanceOf(FunCall.class)));

		FunCall func = (FunCall) exp;

		assertThat("Set member list is null.", func.getArgs(),
				is(notNullValue()));
		assertThat("Wrong number of set children returned.", func.getArgs()
				.size(), is(equalTo(3)));

		assertThat("First parameter expression type is wrong.", func.getArgs()
				.get(0), is(instanceOf(ExpressionParameter.class)));
		assertThat("Third parameter expression type is wrong.", func.getArgs()
				.get(2), is(instanceOf(ExpressionParameter.class)));

		ExpressionParameter parameter1 = (ExpressionParameter) func.getArgs()
				.get(0);
		ExpressionParameter parameter3 = (ExpressionParameter) func.getArgs()
				.get(2);

		assertThat("Wrong parameter expression.", parameter1.getExpression(),
				is(equalTo("parameter1")));
		assertThat("Wrong parameter expression.", parameter3.getExpression(),
				is(equalTo("parameter2")));
	}

	@Test
	public void testParseMemberExpressionEscape() throws Exception {
		String mdx = "SELECT [Measures].[Store Sales] ON COLUMNS, [Product].[All Products] ON ROWS FROM [Sales] "
				+ "WHERE $['#aaa' + '[1, 2, 3]]']";

		MdxStatement query = parseQuery(mdx);
		assertThat("Failed to parse : " + mdx, query, is(notNullValue()));

		ExpressionParameter parameter = (ExpressionParameter) query.getSlicer();
		assertThat("Slicer exp is null.", parameter, is(notNullValue()));

		assertThat("Wrong member expression returned.",
				parameter.getExpression(), is(equalTo("'#aaa' + '[1, 2, 3]'")));
	}

	@Test
	public void testParseValueExpression() throws Exception {
		String mdx = "WITH MEMBER [Measures].[Calc Cost] AS '[Measures].[Store Cost] * ${ratio}' "
				+ "SELECT [Measures].[Calc Cost] ON COLUMNS FROM [Sales]";

		MdxStatement query = parseQuery(mdx);
		assertThat("Failed to parse : " + mdx, query, is(notNullValue()));

		List<Formula> formulas = query.getFormulas();

		assertThat("Formula list not be null.", formulas, is(notNullValue()));
		assertThat("Number of formula should be 1.", formulas.size(),
				is(equalTo(1)));

		Formula formula = formulas.get(0);

		Exp arg = formula.getExp();

		assertThat("Wrong argument type.", arg, is(instanceOf(FunCall.class)));

		FunCall func = (FunCall) arg;

		assertThat("Wrong number of function arguments.",
				func.getArgs().size(), is(equalTo(2)));

		ValueParameter parameter = (ValueParameter) func.getArgs().get(1);
		assertThat("Wrong value parameter expression.",
				parameter.getExpression(), is(equalTo("ratio")));
	}

	@Test
	public void testParseWithMemberProperties() throws Exception {
		String mdx = "WITH MEMBER [Measures].[address] AS "
				+ "'[Store].CurrentMember.Properties(\"Street address\")' "
				+ "SELECT NON EMPTY {[Measures].[address]} ON COLUMNS, "
				+ "NON EMPTY [Store].[USA].[CA].[Beverly Hills].Children ON ROWS from [Sales]";

		MdxStatement query = parseQuery(mdx);
		assertThat("Failed to parse : " + mdx, query, is(notNullValue()));
	}
}
