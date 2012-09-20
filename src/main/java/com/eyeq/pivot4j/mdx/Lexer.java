/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;

import java.io.InputStream;
import java.io.Reader;

/**
 * public Wrapper for generated Yylex
 */
public class Lexer extends Yylex {

	public Lexer(Reader reader) {
		super(reader);
	}

	public Lexer(InputStream in) {
		super(in);
	}
}
