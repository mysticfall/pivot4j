/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.mdx;

import java.util.List;

/**
 * MDX query axis
 */
public class QueryAxis implements Exp {

	private static final long serialVersionUID = 9064412375948950770L;

	private String name;

	private boolean nonEmpty;

	private Exp exp;

	private List<CompoundId> dimProps; // DIMENSION PROPERTIES

	/**
	 * c'tor
	 * 
	 * @see java.lang.Object#Object()
	 */
	public QueryAxis(boolean nonEmpty, Exp exp, String name) {
		this.nonEmpty = nonEmpty;
		this.exp = exp;
		this.name = name;
	}

	/**
	 * Returns the exp.
	 * 
	 * @return Exp
	 */
	public Exp getExp() {
		return exp;
	}

	/**
	 * Returns the name.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the nonEmpty.
	 * 
	 * @return boolean
	 */
	public boolean isNonEmpty() {
		return nonEmpty;
	}

	/**
	 * Sets the exp.
	 * 
	 * @param exp
	 *            The exp to set
	 */
	public void setExp(Exp exp) {
		this.exp = exp;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the nonEmpty.
	 * 
	 * @param nonEmpty
	 *            The nonEmpty to set
	 */
	public void setNonEmpty(boolean nonEmpty) {
		this.nonEmpty = nonEmpty;
	}

	/**
	 * format to MDX
	 */
	public String toMdx() {
		StringBuffer sb = new StringBuffer();

		if (exp == null) {
			sb.append("{}");
		} else {
			if (nonEmpty) {
				sb.append("NON EMPTY ");
			}
			sb.append(exp.toMdx());
			if (dimProps != null && dimProps.size() > 0) {
				sb.append(" DIMENSION PROPERTIES ");
				for (int i = 0; i < dimProps.size(); i++) {
					if (i > 0) {
						sb.append(',');
					}
					sb.append(dimProps.get(i).toMdx());
				}
			}
		}

		sb.append(" ON ");
		sb.append(name);
		return sb.toString();
	}

	/**
	 * 
	 * @see java.lang.Object#clone()
	 */
	public QueryAxis clone() {
		QueryAxis qa = new QueryAxis(nonEmpty, (Exp) exp.clone(), name);
		qa.setDimProps(dimProps);
		return qa;
	}

	/**
	 * @return
	 */
	public List<CompoundId> getDimProps() {
		return dimProps;
	}

	/**
	 * @param dimProps
	 */
	public void setDimProps(List<CompoundId> dimProps) {
		this.dimProps = dimProps;
	}

	/**
	 * @see com.tonbeller.jpivot.olap.mdxparse.Exp#accept
	 */
	public void accept(ExpVisitor visitor) {
		visitor.visitQueryAxis(this);
	}
}