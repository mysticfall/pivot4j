/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.olap4j.Axis;

import com.eyeq.pivot4j.mdx.impl.MdxParserImpl;

public class MdxParserTest {

	/**
	 * @param mdxQuery
	 * @return
	 * @throws Exception
	 */
	protected MdxQuery parseQuery(String mdxQuery) throws Exception {
		MdxParser parser = new MdxParserImpl();

		return parser.parse(mdxQuery);
	}

	@Test
	public void testParseEmptyAxis() throws Exception {
		String mdx = "SELECT FROM DummyCube";

		MdxQuery query = parseQuery(mdx);

		assertNotNull("Failed to parse : " + mdx, query);
		assertNotNull("Axes should not be null.", query.getAxes());
		assertTrue("Number of axes should be 0.", query.getAxes().isEmpty());
	}

	@Test
	public void testGenerateEmptyAxis() throws Exception {
		String mdx = "SELECT FROM DummyCube";

		MdxQuery query = new MdxQuery();
		query.setCube("DummyCube");

		assertEquals("Unexpected MDX query.", mdx, query.toMdx());
	}

	@Test
	public void testParseSingleAxis() throws Exception {
		String mdx = "SELECT [AAA] ON COLUMNS FROM DummyCube";

		MdxQuery query = parseQuery(mdx);

		assertNotNull("Failed to parse : " + mdx, query);
		assertNotNull("Axes should not be null.", query.getAxes());

		assertEquals("Number of axes should be 1.", 1, query.getAxes().size());

		Exp columnExp = query.getAxes().get(0).getExp();

		assertNotNull("Exp object for columns axis is null.", columnExp);

		assertTrue("Exp object should be instance of CompoundId.",
				columnExp instanceof CompoundId);

		mdx = "SELECT [AAA] ON ROWS FROM DummyCube";

		Exp rowExp = query.getAxes().get(0).getExp();

		assertNotNull("Exp object for rows axis is null.", rowExp);

		assertTrue("Exp object should be instance of CompoundId.",
				rowExp instanceof CompoundId);
	}

	@Test
	public void testGenerateSingleAxis() throws Exception {
		String mdx = "SELECT [AAA] ON COLUMNS FROM [DummyCube]";

		MdxQuery query = new MdxQuery();

		QueryAxis axis = new QueryAxis(Axis.COLUMNS, new CompoundId("[AAA]"));
		query.setAxis(axis);
		query.setCube("[DummyCube]");

		assertEquals("Unexpected MDX query.", mdx, query.toMdx());
	}

	@Test
	public void testParseKeyIdentifier() throws Exception {
		String mdx = "SELECT [AAA].&[BBB] ON COLUMNS, [CCC].&[DDD].[EEE] ON ROWS FROM DummyCube";

		MdxQuery query = parseQuery(mdx);

		assertNotNull("Failed to parse : " + mdx, query);
		assertNotNull("Axes should not be null.", query.getAxes());

		assertEquals("Number of axes should be 2.", 2, query.getAxes().size());

		Exp columnExp = query.getAxes().get(0).getExp();

		assertNotNull("Exp object for columns axis is null.", columnExp);

		assertTrue("Exp object should be instance of CompoundId.",
				columnExp instanceof CompoundId);

		CompoundId columnId = (CompoundId) columnExp;
		assertEquals("Wrong number of name parts on column axis member.", 2,
				columnId.getNames().size());

		assertEquals("First name part has wrong identifier.", "[AAA]", columnId
				.getNames().get(0).getName());
		assertEquals("Second name part has wrong identifier.", "[BBB]",
				columnId.getNames().get(1).getName());
		assertFalse("First name part cannot be a key identifier.", columnId
				.getNames().get(0).isKey());
		assertTrue("Second name part is a key identifier.", columnId.getNames()
				.get(1).isKey());

		Exp rowExp = query.getAxes().get(1).getExp();

		assertNotNull("Exp object for rows axis is null.", rowExp);

		assertTrue("Exp object should be instance of CompoundId.",
				rowExp instanceof CompoundId);

		CompoundId rowId = (CompoundId) rowExp;
		assertEquals("Wrong number of name parts on row axis member.", 3, rowId
				.getNames().size());

		assertEquals("First name part has wrong identifier.", "[CCC]", rowId
				.getNames().get(0).getName());
		assertEquals("Second name part has wrong identifier.", "[DDD]", rowId
				.getNames().get(1).getName());
		assertEquals("Third name part has wrong identifier.", "[EEE]", rowId
				.getNames().get(2).getName());
		assertFalse("First name part cannot be a key identifier.", rowId
				.getNames().get(0).isKey());
		assertTrue("Second name part is a key identifier.", rowId.getNames()
				.get(1).isKey());
		assertFalse("Third name part is not a key identifier.", rowId
				.getNames().get(2).isKey());
	}

	@Test
	public void testGenerateKeyIdentifier() throws Exception {
		String mdx = "SELECT [AAA].&[BBB] ON COLUMNS, [CCC].&[DDD].[EEE] ON ROWS FROM DummyCube";

		MdxQuery query = new MdxQuery();

		CompoundId columnId = new CompoundId().append("[AAA]").append("[BBB]",
				true);
		CompoundId rowId = new CompoundId().append("[CCC]")
				.append("[DDD]", true).append("[EEE]");

		query.setAxis(new QueryAxis(Axis.COLUMNS, columnId));
		query.setAxis(new QueryAxis(Axis.ROWS, rowId));

		query.setCube("DummyCube");

		assertEquals("Unexpected MDX query.", mdx, query.toMdx());
	}

	@Test
	public void testParseSapVariables() throws Exception {
		String mdx = "SELECT [Measures].members ON COLUMNS, NON EMPTY [ODB_CUST].members ON ROWS "
				+ "FROM [ODBOSCEN1/MKTBRANCH] SAP VARIABLES [ODBBRANC] INCLUDING [ODB_BRANC].[CHEM]";

		MdxQuery query = parseQuery(mdx);

		assertNotNull("Failed to parse : " + mdx, query);
		assertNotNull("SAP varaible list should not be null.",
				query.getSapVariables());

		assertEquals("Number of SAP variables should be 1.", 1, query
				.getSapVariables().size());

		SapVariable variable = query.getSapVariables().get(0);

		assertNotNull("SAP varaible should not be null.", variable);

		CompoundId name = variable.getName();
		assertNotNull("Name of the SAP varaible should not be null.", name);

		List<SapVariable.Value> values = variable.getValues();
		assertNotNull("Value list of the SAP varaible should not be null.",
				values);
		assertEquals("Number of the SAP variable value should be 1.", 1,
				values.size());

		SapVariable.Value value = values.get(0);
		assertFalse("The SAP variable is not an interval value.",
				value.isInterval());
		assertTrue("The SAP variable has specified INCLUDING option.",
				value.isIncluding());

		assertNotNull("Option value of the SAP varaible should not be null.",
				value.getValue());

		assertTrue(
				"Option value of the SAP variable should be instance of CompoundId.",
				value.getValue() instanceof CompoundId);

		assertEquals("Incorrect option value of the SAP variable.",
				"[ODB_BRANC].[CHEM]", value.getValue().toMdx());
	}

	@Test
	public void testParseOpeningBracket() throws Exception {
		String mdx = "SELECT [AA[BB].[CC] ON COLUMNS, [DD].[[AAB] ON ROWS FROM DummyCube";

		MdxQuery query = parseQuery(mdx);

		assertNotNull("Failed to parse : " + mdx, query);
		assertNotNull("Axes should not be null.", query.getAxes());

		assertEquals("Number of axes should be 2.", 2, query.getAxes().size());

		Exp columnExp = query.getAxes().get(0).getExp();

		assertNotNull("Exp object for columns axis is null.", columnExp);

		assertTrue("Exp object should be instance of CompoundId.",
				columnExp instanceof CompoundId);

		CompoundId columnMember = (CompoundId) columnExp;
		String[] columnNames = columnMember.toStringArray();

		assertNotNull("Segments of column member id is null.", columnNames);
		assertEquals("Column member id should contain 2 segment parts.", 2,
				columnNames.length);
		assertEquals(
				"First segment of the column member element is incorrect.",
				"[AA[BB]", columnNames[0]);
		assertEquals(
				"Second segment of the column member element is incorrect.",
				"[CC]", columnNames[1]);

		Exp rowExp = query.getAxes().get(1).getExp();

		assertNotNull("Exp object for rows axis is null.", rowExp);

		assertTrue("Exp object should be instance of CompoundId.",
				rowExp instanceof CompoundId);

		CompoundId rowMember = (CompoundId) rowExp;
		String[] rowNames = rowMember.toStringArray();

		assertNotNull("Segments of row member id is null.", rowNames);
		assertEquals("Row member id should contain 2 segment parts.", 2,
				rowNames.length);
		assertEquals("First segment of the row member element is incorrect.",
				"[DD]", rowNames[0]);
		assertEquals("Second segment of the row member element is incorrect.",
				"[[AAB]", rowNames[1]);
	}

	@Test
	public void testParseBracketEscape() throws Exception {
		String mdx = "SELECT [AA[BB]]].[CC] ON COLUMNS, [DD].[AA]]B] ON ROWS FROM DummyCube";

		MdxQuery query = parseQuery(mdx);
		assertNotNull("Failed to parse : " + mdx, query);

		assertNotNull("Failed to parse : " + mdx, query);
		assertNotNull("Axes should not be null.", query.getAxes());

		assertEquals("Number of axes should be 2.", 2, query.getAxes().size());

		Exp columnExp = query.getAxes().get(0).getExp();

		assertNotNull("Exp object for columns axis is null.", columnExp);

		assertTrue("Exp object should be instance of CompoundId.",
				columnExp instanceof CompoundId);

		CompoundId columnMember = (CompoundId) columnExp;
		String[] columnNames = columnMember.toStringArray();

		assertNotNull("Segments of column member id is null.", columnNames);
		assertEquals("Column member id should contain 2 segment parts.", 2,
				columnNames.length);
		assertEquals(
				"First segment of the column member element is incorrect.",
				"[AA[BB]]]", columnNames[0]);
		assertEquals(
				"Second segment of the column member element is incorrect.",
				"[CC]", columnNames[1]);

		Exp rowExp = query.getAxes().get(1).getExp();

		assertNotNull("Exp object for rows axis is null.", rowExp);

		assertTrue("Exp object should be instance of CompoundId.",
				rowExp instanceof CompoundId);

		CompoundId rowMember = (CompoundId) rowExp;
		String[] rowNames = rowMember.toStringArray();

		assertNotNull("Segments of row member id is null.", rowNames);
		assertEquals("Row member id should contain 2 segment parts.", 2,
				rowNames.length);
		assertEquals("First segment of the row member element is incorrect.",
				"[DD]", rowNames[0]);
		assertEquals("Second segment of the row member element is incorrect.",
				"[AA]]B]", rowNames[1]);
	}

	@Test
	public void testGenerateBracketEscape() throws Exception {
		String mdx = "SELECT [AA[BB]]].[CC] ON COLUMNS, [DD].[AA]]B] ON ROWS FROM DummyCube";

		MdxQuery query = new MdxQuery();

		CompoundId columnId = new CompoundId().append("[AA[BB]]]").append(
				"[CC]");
		CompoundId rowId = new CompoundId().append("[DD]").append("[AA]]B]");

		query.setAxis(new QueryAxis(Axis.COLUMNS, columnId));
		query.setAxis(new QueryAxis(Axis.ROWS, rowId));

		query.setCube("DummyCube");

		assertEquals("Unexpected MDX query.", mdx, query.toMdx());
	}
}
