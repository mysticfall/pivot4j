/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.mdx.impl;

import java.io.StringReader;

import java_cup.runtime.Symbol;

import org.apache.commons.lang.NullArgumentException;
import org.pivot4j.PivotException;
import org.pivot4j.mdx.MdxParser;
import org.pivot4j.mdx.MdxStatement;
import org.pivot4j.mdx.ParseException;

import org.pivot4j.mdx.impl.CupParser;

public class MdxParserImpl implements MdxParser {

    /**
     * @see org.pivot4j.mdx.MdxParser#parse(java.lang.String)
     */
    @Override
    public MdxStatement parse(String mdx) {
        if (mdx == null) {
            throw new NullArgumentException("mdx");
        }

        MdxStatement query;

        try {
            CupParser parser = new CupParser(new StringReader(mdx));
            Symbol parseTree = parser.parse();

            query = (MdxStatement) parseTree.value;
        } catch (PivotException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Failed to parse MDX query : " + mdx;
            throw new ParseException(msg, e);
        }

        return query;
    }
}
