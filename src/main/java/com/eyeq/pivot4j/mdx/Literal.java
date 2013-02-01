/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;

/**
 * MDX parser Literal Expressions
 */
public class Literal extends AbstractExp {

	private static final long serialVersionUID = 3137892085820716628L;

	public int type;

	private Object o;

	public static final Literal emptyString = new Literal("", false);

	public static final Literal zero = new Literal(new Integer(0));

	public static final Literal one = new Literal(new Integer(1));

	public static final Literal doubleZero = new Literal(new Double(0.0));

	public static final Literal doubleOne = new Literal(new Double(1.0));

	public static final int TYPE_SYMBOL = 1;

	public static final int TYPE_STRING = 2;

	public static final int TYPE_NUMERIC = 3;

	private Literal(String s, boolean isSymbol) {
		this.o = s;
		this.type = isSymbol ? TYPE_SYMBOL : TYPE_STRING;
	}

	public static Literal createString(String s) {
		if (s.equals("")) {
			return emptyString;
		} else {
			return new Literal(s, false);
		}
	}

	public static Literal createSymbol(String s) {
		return new Literal(s, true);
	}

	private Literal(Double d) {
		this.o = d;
		this.type = TYPE_NUMERIC;
	}

	public static Literal create(Double d) {
		if (d.doubleValue() == 0.0) {
			return doubleZero;
		} else if (d.doubleValue() == 1.0) {
			return doubleOne;
		} else {
			return new Literal(d);
		}
	}

	private Literal(Integer i) {
		this.o = i;
		this.type = TYPE_NUMERIC;
	}

	public static Literal create(Integer i) {
		if (i.intValue() == 0) {
			return zero;
		} else if (i.intValue() == 1) {
			return one;
		} else {
			return new Literal(i);
		}
	}

	/**
	 * format to MDX
	 */
	public String toMdx() {
		return o.toString();
	}

	/**
	 * Literal is immutable
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Literal clone() {
		return this;
	}

	/**
   */
	public String stringValue() {
		if (type == TYPE_STRING) {
			// must remove enclosing double quotes
			String str = (String) o;
			return str.substring(1, str.length() - 1);
		} else
			return o.toString();
	}

	/**
	 * @return
	 */
	public Object getValueObject() {
		return o;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#accept
	 */
	public void accept(ExpVisitor visitor) {
		visitor.visitLiteral(this);
	}
}
