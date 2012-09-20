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
 * member property implementation
 */
public class MemberProperty implements Exp {

	private String name;

	private Exp exp;

	public MemberProperty(String name, Exp exp) {
		this.name = name;
		this.exp = exp;
	}

	/**
	 * @return The expression that makes up the value of the member property
	 */
	public Exp getExp() {
		return exp;
	}

	/**
	 * format to MDX
	 */
	public String toMdx() {
		String str = name;
		str += " = ";
		str += exp.toMdx();
		return str;
	}

	/**
	 * 
	 * @see java.lang.Object#clone()
	 */
	public MemberProperty clone() {
		return new MemberProperty(name, (Exp) exp.clone());
	}

	/**
	 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#accept
	 */
	public void accept(ExpVisitor visitor) {
		visitor.visitMemberProperty(this);
	}
}
