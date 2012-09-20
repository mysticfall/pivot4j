/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;

public enum Syntax {

	Function(0),

	Property(1),

	Method(2),

	Infix(3),

	Prefix(4),

	Braces(5),

	Parentheses(6),

	Case(7),

	Mask(0xFF),

	PropertyQuoted(Property.getCode() | 0x100),

	PropertyAmpQuoted(Property.getCode() | 0x200), ;

	private int code;

	/**
	 * @param code
	 */
	Syntax(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
