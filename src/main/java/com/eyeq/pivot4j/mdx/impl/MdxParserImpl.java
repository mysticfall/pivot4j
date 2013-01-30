/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx.impl;

import java.io.StringReader;

import java_cup.runtime.Symbol;

import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.mdx.MdxParser;
import com.eyeq.pivot4j.mdx.ParseException;
import com.eyeq.pivot4j.mdx.MdxQuery;

public class MdxParserImpl implements MdxParser {

	/**
	 * @see com.eyeq.pivot4j.mdx.MdxParser#parse(java.lang.String)
	 */
	@Override
	public MdxQuery parse(String mdx) {
		if (mdx == null) {
			throw new IllegalArgumentException(
					"MDX query argument cannot be null.");
		}

		MdxQuery query;

		try {
			CupParser parser = new CupParser(new StringReader(mdx));
			Symbol parseTree = parser.parse();

			query = (MdxQuery) parseTree.value;
		} catch (PivotException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Failed to parse MDX query : " + mdx;
			throw new ParseException(msg, e);
		}

		return query;
	}
}
