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
 * Formula representing a WITH MEMBER ... or a WITH SET ...
 */
public class Formula extends AbstractExp {

	private static final long serialVersionUID = 8119836944328850979L;

	private boolean isMember;

	private CompoundId name;

	private Exp exp;

	private MemberProperty[] memberProperties;

	/** Construct formula specifying a set. */
	public Formula(CompoundId name, Exp exp) {
		this(false, name, exp, new MemberProperty[0]);
	}

	/** Construct a formula specifying a member. */
	public Formula(CompoundId name, Exp exp, MemberProperty[] memberProperties) {
		this(true, name, exp, memberProperties);
	}

	private Formula(boolean isMember, CompoundId name, Exp exp,
			MemberProperty[] memberProperties) {
		this.isMember = isMember;
		this.name = name;
		this.exp = exp;
		this.memberProperties = memberProperties;
	}

	/**
	 * Returns the isMember.
	 * 
	 * @return boolean
	 */
	public boolean isMember() {
		return isMember;
	}

	/**
	 * @return target element(member or set) for the formula
	 */
	public CompoundId getName() {
		return name;
	}

	/**
	 * @return Exp for formula
	 */
	public Exp getExp() {
		return exp;
	}

	/**
	 * format to MDX
	 */
	public String toMdx() {
		StringBuffer sb = new StringBuffer();

		if (isMember) {
			sb.append("MEMBER ");
		} else {
			sb.append("SET ");
		}

		sb.append(name.toMdx());
		sb.append(" AS '");
		sb.append(exp.toMdx());
		sb.append('\'');

		for (int i = 0; i < memberProperties.length; i++) {
			sb.append(',');
			sb.append(memberProperties[i].toMdx());
		}

		return sb.toString();
	}

	/**
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Formula clone() {
		MemberProperty[] cloneMemberProperties = new MemberProperty[memberProperties.length];
		for (int i = 0; i < cloneMemberProperties.length; i++) {
			cloneMemberProperties[i] = (MemberProperty) memberProperties[i]
					.clone();
		}

		return new Formula(isMember, name.clone(), exp.clone(),
				cloneMemberProperties);
	}

	/**
	 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#accept
	 */
	public void accept(ExpVisitor visitor) {
		visitor.visitFormula(this);
	}
}
