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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;

import java_cup.runtime.Symbol;

import org.junit.Test;

public class ParserTest {

	/**
	 * @param mdxQuery
	 * @return
	 * @throws Exception
	 */
	protected ParsedQuery parseQuery(String mdxQuery) throws Exception {
		Reader reader = new StringReader(mdxQuery);
		Parser parser = new Parser(new Lexer(reader));

		Symbol parseTree = parser.parse();

		ParsedQuery parsedQuery = (ParsedQuery) parseTree.value;
		parsedQuery.afterParse();

		return parsedQuery;
	}

	@Test
	public void testParseOpeningBracket() throws Exception {
		String mdx = "SELECT [AA[BB].[CC] ON COLUMNS, [DD].[[AAB] ON ROWS FROM DummyCube";

		ParsedQuery query = parseQuery(mdx);

		assertNotNull("Failed to parse : " + mdx, query);
		assertNotNull("Axes should not be null.", query.getAxes());

		assertEquals("Number of axes should be 2.", 2, query.getAxes().length);

		Exp columnExp = query.getAxes()[0].getExp();

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

		Exp rowExp = query.getAxes()[1].getExp();

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
	public void testBracketEscape() throws Exception {
		String mdx = "SELECT [AA[BB]]].[CC] ON COLUMNS, [DD].[AA]]B] ON ROWS FROM DummyCube";

		ParsedQuery query = parseQuery(mdx);
		assertNotNull("Failed to parse : " + mdx, query);

		assertNotNull("Failed to parse : " + mdx, query);
		assertNotNull("Axes should not be null.", query.getAxes());

		assertEquals("Number of axes should be 2.", 2, query.getAxes().length);

		Exp columnExp = query.getAxes()[0].getExp();

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

		Exp rowExp = query.getAxes()[1].getExp();

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
}
